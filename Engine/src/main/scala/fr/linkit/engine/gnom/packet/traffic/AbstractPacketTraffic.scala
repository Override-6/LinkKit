/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.gnom.packet.traffic

import fr.linkit.api.gnom.packet.channel.ChannelScope
import fr.linkit.api.gnom.packet.channel.ChannelScope.ScopeFactory
import fr.linkit.api.gnom.packet.traffic._
import fr.linkit.api.gnom.packet.traffic.injection.PacketInjectionControl
import fr.linkit.api.gnom.packet.{DedicatedPacketCoordinates, Packet, PacketAttributes, PacketBundle}
import fr.linkit.api.gnom.persistence.context.PersistenceConfig
import fr.linkit.api.gnom.persistence.obj.TrafficNetworkPresenceReference
import fr.linkit.api.gnom.reference.NetworkObjectLinker
import fr.linkit.api.gnom.reference.traffic.{ObjectManagementChannel, TrafficInterestedNPH}
import fr.linkit.api.internal.system.{ClosedException, Reason}
import fr.linkit.engine.gnom.packet.SimplePacketBundle
import fr.linkit.engine.gnom.packet.traffic.channel.DefaultObjectManagementChannel
import fr.linkit.engine.gnom.packet.traffic.injection.ParallelInjectionContainer
import fr.linkit.engine.gnom.persistence.context.{ImmutablePersistenceContext, PersistenceConfigBuilder, SimplePersistenceConfig}
import fr.linkit.engine.gnom.reference.ObjectChannelContextObjectLinker
import fr.linkit.engine.internal.utils.ClassMap

import java.net.URL
import scala.reflect.ClassTag

abstract class AbstractPacketTraffic(override val currentIdentifier: String,
                                     defaultPersistenceConfigUrl: Option[URL]) extends PacketTraffic {

    val context: ImmutablePersistenceContext = ImmutablePersistenceContext(this, new ClassMap(), new ClassMap())
    private val minimalConfigBuilder = PersistenceConfigBuilder.fromScript(getClass.getResource("/default_scripts/persistence_minimal.sc"), this)
    private val objectChannelConfig  = {
        val linker = new ObjectChannelContextObjectLinker(minimalConfigBuilder)
        new SimplePersistenceConfig(context, new ClassMap(), linker, false, true, false)
    }

    private val objectChannel = {
        val scope = ChannelScopes.BroadcastScope(newWriter(Array.empty, objectChannelConfig), Array.empty)
        new DefaultObjectManagementChannel(null, scope)
    }

    override val defaultPersistenceConfig: PersistenceConfig = {
        defaultPersistenceConfigUrl
                .fold(new PersistenceConfigBuilder())(PersistenceConfigBuilder.fromScript(_, this))
                .transfer(minimalConfigBuilder)
                .build(context, null, objectChannel)
    }

    @volatile private var closed = false

    override  val trafficPath: Array[Int] = Array.empty
    private   val injectionContainer      = new ParallelInjectionContainer()
    private   val linker                  = new TrafficNetworkObjectLinker(objectChannel, this)
    protected val rootStore               = new SimplePacketInjectableStore(this, linker, defaultPersistenceConfig, trafficPath)

    override def getTrafficObjectLinker: NetworkObjectLinker[TrafficNetworkPresenceReference] with TrafficInterestedNPH = {
        linker
    }

    override def getInjectable[C <: PacketInjectable : ClassTag](injectableID: Int, config: PersistenceConfig, factory: PacketInjectableFactory[C], scopeFactory: ScopeFactory[_ <: ChannelScope]): C = {
        rootStore.getInjectable[C](injectableID, config, factory, scopeFactory)
    }

    override def getPersistenceConfig(path: Array[Int]): PersistenceConfig = {
        if (path.isEmpty)
            objectChannelConfig
        else
            rootStore.getPersistenceConfig(path)
    }

    override def close(reason: Reason): Unit = {
        rootStore.close()
        closed = true
    }

    override def isClosed: Boolean = closed

    @inline
    override def processInjection(packet: Packet, attr: PacketAttributes, coordinates: DedicatedPacketCoordinates): Unit = {
        processInjection(SimplePacketBundle(packet, attr, coordinates))
    }

    override def processInjection(bundle: PacketBundle): Unit = {
        val injection = injectionContainer.makeInjection(bundle)
        processInjection0(injection)
    }

    override def newWriter(path: Array[Int]): PacketWriter = {
        newWriter(path, defaultPersistenceConfig)
    }

    override def findStore(id: Int): Option[PacketInjectableStore] = rootStore.findStore(id)

    override def findInjectable[C <: PacketInjectable : ClassTag](id: Int): Option[C] = rootStore.findInjectable(id)

    override def createStore(id: Int, persistenceConfig: PersistenceConfig): PacketInjectableStore = rootStore.createStore(id, persistenceConfig)

    def getObjectManagementChannel: ObjectManagementChannel = objectChannel

    def findPresence(reference: TrafficNetworkPresenceReference): Option[TrafficPresence[TrafficNetworkPresenceReference]] = {
        rootStore.findPresence(reference.trafficPath)
    }

    protected def ensureOpen(): Unit = {
        if (closed)
            throw new ClosedException("This Traffic handler is closed")
    }

    private def processInjection0(injection: PacketInjectionControl): Unit = {
        injection.synchronized {
            if (injection.isProcessing) {
                //If a thread is already processing the injection, don't do it with this thread.
                return
            }
            injection.markAsProcessing()
        }
        //AppLogger.debug(s"Injection is not processing : injection.isProcessing = ${injection.isProcessing}")
        makeProcess(injection)
    }

    private def makeProcess(injection: PacketInjectionControl): Unit = {
        //AppLogger.debug(s"Processing injection $injection...")
        ensureOpen()
        if (injection.haveMoreIdentifier) {
            rootStore.inject(injection)
        } else {
            injection.process(objectChannel)
        }
        injectionContainer.removeInjection(injection)
    }
}