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

package fr.linkit.engine.connection.packet.persistence.v3.deserialisation

import fr.linkit.api.connection.packet.PacketCoordinates
import fr.linkit.api.connection.packet.persistence.v3.deserialisation.node.DeserializerNode
import fr.linkit.api.connection.packet.persistence.v3.deserialisation.{DeserializationInputStream, DeserializationObjectPool, DeserializationProgression}
import fr.linkit.api.connection.packet.persistence.v3.{ObjectPersistor, PacketPersistenceContext}
import fr.linkit.engine.connection.packet.persistence.MalFormedPacketException
import fr.linkit.engine.connection.packet.persistence.v3.NoSuchPersistorException
import fr.linkit.engine.connection.packet.persistence.v3.deserialisation.node.{NonObjectDeserializerNode, RawObjectNode}
import fr.linkit.engine.connection.packet.persistence.v3.helper.ArrayPersistence
import fr.linkit.engine.connection.packet.persistence.v3.serialisation.SerializerNodeFlags._
import fr.linkit.engine.connection.packet.persistence.v3.serialisation.node.NullInstanceNode.NoneFlag
import fr.linkit.engine.local.mapping.{ClassMappings, ClassNotMappedException}
import fr.linkit.engine.local.utils.NumberSerializer

import java.nio.ByteBuffer
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class DefaultDeserializationProgression(in: DeserializationInputStream,
                                        override val pool: DeserializationObjectPool,
                                        context: PacketPersistenceContext,
                                        override val coordinates: PacketCoordinates) extends DeserializationProgression {

    private val registeredObjects = mutable.HashMap.empty[ObjectPersistor[_], ListBuffer[Any]]

    def concludeDeserialization(): Unit = {
        registeredObjects.foreachEntry((persistor, deserializedObjects) => {
            persistor.sortedDeserializedObjects(deserializedObjects.toArray.reverse)
        })
    }

    override def getNextDeserializationNode: DeserializerNode = {
        val buff     = in.buff
        val startPos = buff.position()
        buff.get match {
            case b if b >= ByteFlag && b <= BooleanFlag =>
                buff.position(startPos)
                NonObjectDeserializerNode(_.readPrimitive())
            case StringFlag                             =>
                buff.position(startPos)
                NonObjectDeserializerNode(_.readString())
            case ArrayFlag                              => ArrayPersistence.deserialize(in)
            case HeadedValueFlag                        => pool.getHeaderValueNode(NumberSerializer.deserializeFlaggedNumber[Int](in))
            case NullFlag                               => RawObjectNode(if (buff.limit() > buff.position() && buff.get(buff.position()) == NoneFlag) None else null)
            case NoneFlag                               => RawObjectNode(None)
            case ClassFlag                              => in => in.readClass()
            case ObjectFlag                             => handleObjectFlag(buff, startPos)
            case MappedObjectFlag                       => handleMappedObjectFlag(buff, startPos)
            case flag                                   => throw new MalFormedPacketException(buff, s"Unknown flag '$flag' at start of node expression.")
        }
    }

    private def handleMappedObjectFlag(buffer: ByteBuffer, i: Int): DeserializerNode = {
        getObjectNode(buffer, i) { clazz => {
            val miniPersistor = context.getDescription[Any](clazz).miniPersistor.getOrElse {
                throw new NoSuchPersistorException(s"Could not find mini persistor for clazz $clazz. The packet deserialization requires it.") //Enhance the message by saying "a mini persistor that deserializes 'B' to 'class name'
            }
            val node          = getNextDeserializationNode
            in => miniPersistor.deserialize(cast(node.deserialize(in)))
        }
        }
    }

    private def cast[A](obj: Any): A = obj.asInstanceOf[A]

    private def handleObjectFlag(buff: ByteBuffer, startPos: Int): DeserializerNode = {
        getObjectNode(buff, startPos)(handlePersistor)
    }

    private def getObjectNode(buff: ByteBuffer, startPos: Int)(action: Class[_] => DeserializerNode): DeserializerNode = {
        if (buff.get(startPos + 1) == HeadedValueFlag) {
            in.position(startPos + 2)
            return pool.getHeaderValueNode(NumberSerializer.deserializeFlaggedNumber[Int](in))
        }
        val classCode   = buff.getInt
        val objectClass = ClassMappings.getClass(classCode)
        if (objectClass == null)
            throw new ClassNotMappedException(s"classCode $classCode is not mapped.")
        if (objectClass.isEnum)
            NonObjectDeserializerNode(_.readEnum(hint = objectClass))
        else {
            action(objectClass)
        }
    }

    private def handlePersistor(clazz: Class[_]): DeserializerNode = {
        val persistor = context.getDescription(clazz).deserialPersistor
        val node      = persistor.getDeserialNode(context.getDescription(clazz), context, this)
        if (!persistor.useSortedDeserializedObjects)
            return node

        node.addOnReferenceAvailable { ref =>
            registeredObjects.getOrElseUpdate(persistor, ListBuffer.empty) += ref
        }
        node
    }

}