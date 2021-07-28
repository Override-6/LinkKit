/*
 *  Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can only use it for personal uses, studies or documentation.
 *  You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 *  ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 *  Please contact maximebatista18@gmail.com if you need additional information or have any
 *  questions.
 */

package fr.linkit.engine.connection.cache.obj

import fr.linkit.api.connection.cache.repo._
import fr.linkit.api.connection.cache.repo.description.{PuppeteerInfo, TreeViewBehavior, WrapperBehavior}
import fr.linkit.api.connection.cache.repo.generation.PuppetWrapperGenerator
import fr.linkit.api.connection.cache.repo.tree.SyncNode
import fr.linkit.api.connection.cache.{CacheContent, InternalSharedCache, SharedCacheFactory, SharedCacheManager}
import fr.linkit.api.connection.packet.Packet
import fr.linkit.api.connection.packet.traffic.PacketInjectableContainer
import fr.linkit.api.local.concurrency.Procrastinator
import fr.linkit.api.local.system.AppLogger
import fr.linkit.engine.connection.cache.AbstractSharedCache
import fr.linkit.engine.connection.cache.obj.DefaultEngineObjectCenter.{FieldRestorer, PuppetProfile}
import fr.linkit.engine.connection.cache.obj.description.TreeViewDefaultBehavior
import fr.linkit.engine.connection.cache.obj.description.annotation.AnnotationBasedMemberBehaviorFactory
import fr.linkit.engine.connection.cache.obj.generation.{CloneHelper, PuppetWrapperClassGenerator, WrappersClassResource}
import fr.linkit.engine.connection.cache.obj.invokation.local.{ObjectChip, SimpleRMIHandler}
import fr.linkit.engine.connection.cache.obj.invokation.remote.{InstancePuppeteer, InvocationPacket}
import fr.linkit.engine.connection.cache.obj.tree.{DefaultPuppetNodeCenter, MemberSyncNode, PuppetNode}
import fr.linkit.engine.connection.packet.fundamental.RefPacket.ObjectPacket
import fr.linkit.engine.connection.packet.traffic.ChannelScopes
import fr.linkit.engine.connection.packet.traffic.channel.request.{RequestBundle, RequestPacketChannel}
import fr.linkit.engine.local.LinkitApplication
import fr.linkit.engine.local.resource.external.LocalResourceFolder._
import fr.linkit.engine.local.utils.ScalaUtils

import java.lang.reflect.Field
import java.util.concurrent.ThreadLocalRandom
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

//TODO Redesign the NetworkSharedCacheManager and its way to handle caches
class DefaultEngineObjectCenter[A](handler: SharedCacheManager,
                                   cacheID: Int,
                                   channel: RequestPacketChannel,
                                   generator: PuppetWrapperGenerator,
                                   override val defaultTreeViewBehavior: TreeViewBehavior)
        extends AbstractSharedCache(handler, cacheID, channel) with EngineObjectCenter[A] {

    private val procrastinator: Procrastinator = handler.network.connection

    import defaultTreeViewBehavior._

    override val nodeCenter    = new DefaultPuppetNodeCenter[A]
    private  val fieldRestorer = new FieldRestorer
    private  val currentIdentifier = channel.traffic.currentIdentifier

    override def postObject[B <: A : ClassTag : TypeTag](id: Int, obj: B): B with PuppetWrapper[B] = {
        postObject[B](id, obj, defaultTreeViewBehavior.getFromClass(obj.getClass.asInstanceOf[Class[B]]))
    }

    override def postObject[B <: A : ClassTag : universe.TypeTag](id: Int, obj: B, behavior: WrapperBehavior[B]): B with PuppetWrapper[B] = {
        ensureNotWrapped(obj)
        if (isRegistered(id))
            throw new ObjectAlreadyPostException(s"Another object with id '$id' figures in the repo's list.")

        val path    = Array(id)
        val wrapper = localRegisterRemotePuppet[B](Array(id), currentIdentifier, obj, behavior)
        //Indicate that a new object has been posted.
        channel.makeRequest(ChannelScopes.discardCurrent)
                .addPacket(ObjectPacket(PuppetProfile(path, obj, currentIdentifier)))
                .putAllAttributes(this)
                .submit()

        wrapper
    }

    override def findObject[B <: A](id: Int): Option[B with PuppetWrapper[B]] = {
        nodeCenter.findNode[B](Array(id)).map(_.puppeteer.getPuppetWrapper)
    }

    override def isRegistered(id: Int): Boolean = {
        isRegistered(Array(id))
    }

    override def injectInTreeView[B <: A](puppet: B, info: PuppeteerInfo): B with PuppetWrapper[B] = {
        genSynchronizedObject[B](info.treeViewPath, puppet, info.owner, defaultTreeViewBehavior)
    }

    private def isRegistered(path: Array[Int]): Boolean = {
        nodeCenter.findNode(path).isDefined
    }

    private def ensureNotWrapped(any: Any): Unit = {
        if (any.isInstanceOf[PuppetWrapper[_]])
            throw new IllegalPuppetException("This object is already wrapped.")
    }

    private def genPuppetWrapper[B](puppeteer: Puppeteer[B], puppet: B): B with PuppetWrapper[B] = {
        val wrapperClass = generator.getPuppetClass[B](puppet.getClass.asInstanceOf[Class[B]])
        val instance     = CloneHelper.instantiateFromOrigin[B](wrapperClass, puppet)
        instance.initPuppeteer(puppeteer)
        instance
    }

    override protected def handleBundle(bundle: RequestBundle): Unit = {
        AppLogger.vDebug(s"Processing bundle : ${bundle}")
        val response = bundle.packet
        val owner    = bundle.coords.senderID
        response.nextPacket[Packet] match {
            case ip: InvocationPacket =>
                val path = ip.path
                val node = nodeCenter.findNode(path)
                node.fold(AppLogger.error(s"Could not find puppet node at path ${path.mkString("$", " -> ", "")}")) {
                    case node: MemberSyncNode[_] => node.handlePacket(ip, bundle.responseSubmitter)
                    case _                       =>
                        throw new BadRMIRequestException(s"Targeted node MUST extends ${classOf[MemberSyncNode[_]].getSimpleName} in order to handle a member rmi request.")
                }
            case ObjectPacket(profile: PuppetProfile[A]) =>
                if (!isRegistered(profile.treeViewPath)) {
                    val puppet = profile.puppet
                    fieldRestorer.restoreFields(puppet)
                    val behavior = defaultTreeViewBehavior.getFromClass[A](puppet.getClass.asInstanceOf[Class[A]])
                    localRegisterRemotePuppet[A](profile.treeViewPath, owner, puppet, behavior)
                }
        }
    }

    override def setContent(content: CacheContent): Unit = {
        content match {
            case content: CacheRepoContent[A] => setRepoContent(content)
            case _                            => throw new IllegalArgumentException(s"Received unknown content '$content'")
        }
    }

    override def snapshotContent: CacheRepoContent[A] = nodeCenter.snapshotContent

    override def genSynchronizedObject[B](treeViewPath: Array[Int],
                                          obj: B,
                                          ownerID: String,
                                          behaviors: TreeViewBehavior): B with PuppetWrapper[B] = {

        def registerObject(wrapper: PuppetWrapper[Any]): Unit = {
            val behavior = getFromClass[Any](wrapper.getWrappedClass)

            val chip      = ObjectChip[Any](ownerID, behavior, wrapper)
            val puppeteer = wrapper.getPuppeteer
            nodeCenter.addNode(treeViewPath, (id, parent: SyncNode[_]) => new PuppetNode(puppeteer, chip, behavior.treeView, this.channel.traffic.currentIdentifier, id, parent))
        }

        ensureNotWrapped(obj)

        if (treeViewPath.isEmpty)
            throw new IllegalArgumentException("Synchronized object's tree view path may not be empty.")

        if (obj == null)
            throw new NullPointerException

        val wrapperBehavior = behaviors.getFromClass[B](obj.getClass.asInstanceOf[Class[B]])
        val puppeteerDesc   = PuppeteerInfo(family, cacheID, ownerID, treeViewPath)
        val puppeteer       = new InstancePuppeteer[B](channel, procrastinator, this, puppeteerDesc, wrapperBehavior)
        val wrapper         = genPuppetWrapper[B](puppeteer, obj)
        registerObject(wrapper.asInstanceOf[PuppetWrapper[Any]])
        for (bhv <- wrapperBehavior.listField() if bhv.isSynchronized) {

            val id            = ThreadLocalRandom.current().nextInt()
            val field         = bhv.desc.javaField
            val fieldValue    = field.get(obj)
            val childTreePath = treeViewPath ++ Array(id)
            val syncValue     = genSynchronizedObject(childTreePath, fieldValue, ownerID, behaviors)
            ScalaUtils.setValue(obj, field, syncValue)
        }
        wrapper
    }

    private def setRepoContent(content: CacheRepoContent[A]): Unit = {
        if (content.array.isEmpty)
            return
        val array = content.array

        array.foreach(profile => {
            val puppet = profile.puppet
            val owner  = profile.owner
            val path   = profile.treeViewPath
            //it's an object that must be chipped by this current repo cache (owner is the same as current identifier)
            if (owner == currentIdentifier) {
                nodeCenter.findNode[A](path).fold {
                    throw new IllegalArgumentException(s"Unknown local object of path '${path.mkString("$", " -> ", "")}'")
                }(_.chip.updateObject(puppet))
            }
            //it's an object that must be remotely controlled because it is chipped by another objects cache.
            else if (!isRegistered(path)) {
                val behavior = defaultTreeViewBehavior.getFromClass[A](puppet.getClass.asInstanceOf[Class[A]])
                localRegisterRemotePuppet[A](path, owner, puppet, behavior)
            }
        })
    }

    private def localRegisterRemotePuppet[B <: A](path: Array[Int], owner: String, puppet: B, behavior: WrapperBehavior[B]): B with PuppetWrapper[B] = {
        val treeViewBehavior = behavior.treeView
        genSynchronizedObject[B](path, puppet, owner, treeViewBehavior)
    }
}

object DefaultEngineObjectCenter {

    private val ClassesResourceDirectory = LinkitApplication.getProperty("compilation.working_dir.classes")

    def apply[A: ClassTag](): SharedCacheFactory[EngineObjectCenter[A] with InternalSharedCache] = {
        val treeView = new TreeViewDefaultBehavior(AnnotationBasedMemberBehaviorFactory(SimpleRMIHandler))
        apply[A](treeView)
    }

    def apply[A: ClassTag](behaviors: TreeViewBehavior): SharedCacheFactory[EngineObjectCenter[A] with InternalSharedCache] = {
        (handler: SharedCacheManager, identifier: Int, container: PacketInjectableContainer) => {
            val channel   = container.getInjectable(5, ChannelScopes.discardCurrent, RequestPacketChannel)
            val context   = handler.network.connection.getApp
            val resources = context.getAppResources.getOrOpenThenRepresent[WrappersClassResource](ClassesResourceDirectory)
            val generator = new PuppetWrapperClassGenerator(context.compilerCenter, resources)

            new DefaultEngineObjectCenter[A](handler, identifier, channel, generator, behaviors)
        }
    }

    class FieldRestorer {

        private val values = mutable.HashMap.empty[Field, Any]

        def putValue(field: Field, value: Any): Unit = {
            field.setAccessible(true)
            values.put(field, value)
        }

        def restoreFields(any: Any): Unit = {
            val clazz = any.getClass

            listRestorableFields(clazz).foreach(field => {
                ScalaUtils.setValue(any, field, values(field))
            })
        }

        private def listRestorableFields(clazz: Class[_]): Array[Field] = {
            if (clazz == null)
                return Array.empty
            clazz.getDeclaredFields.filter(values.contains) ++
                    listRestorableFields(clazz.getSuperclass) ++
                    clazz.getInterfaces.flatMap(listRestorableFields)
        }
    }

    case class PuppetProfile[A](treeViewPath: Array[Int], puppet: A, owner: String) extends Serializable

}