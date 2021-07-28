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

package fr.linkit.engine.connection.packet.persistence.v3.serialisation

import fr.linkit.api.connection.packet.PacketCoordinates
import fr.linkit.api.connection.packet.persistence.v3.PacketPersistenceContext
import fr.linkit.api.connection.packet.persistence.v3.serialisation.node.SerializerNode
import fr.linkit.api.connection.packet.persistence.v3.serialisation.{SerializationObjectPool, SerialisationOutputStream, SerialisationProgression}
import fr.linkit.engine.connection.packet.persistence.v3.serialisation.node.{NullInstanceNode, SimpleObjectSerializerNode}
import fr.linkit.engine.local.utils.{JavaUtils, UnWrapper}

import java.io.NotSerializableException
import java.lang.reflect.Modifier

class DefaultSerializationProgression(override val context: PacketPersistenceContext,
                                      override val pool: SerializationObjectPool,
                                      override val coordinates: PacketCoordinates,
                                      out: SerialisationOutputStream) extends SerialisationProgression {

    override def getSerializationNode(obj: Any): SerializerNode = {
        obj match {
            case null | None                          => new NullInstanceNode(obj == None)
            case i if UnWrapper.isPrimitiveWrapper(i) => out.primitiveNode(i.asInstanceOf[AnyVal])
            case e: Enum[_]                           => SimpleObjectSerializerNode(out.enumNode(e))
            case str: String                          => out.stringNode(str)
            case array: Array[_]                      => out.arrayNode(array)
            case _                                    =>
                val clazz = obj.getClass
               // println(s"Getting node for class '${clazz.getName}...' (class code = ${clazz.getName.hashCode}")
                if (clazz.isArray) //the above match does not works for primitive arrays
                    return out.arrayNode(obj.asInstanceOf[Array[_]])
                if (clazz.isInterface || Modifier.isAbstract(clazz.getModifiers))
                    throw new NotSerializableException(s"Could not serialize interface or abstract class ${clazz.getName}.")
                val desc = context.getDescription(clazz)
                pool.checkNode(obj, out) {
                    context.getPersistenceForSerialisation(clazz).getSerialNode(obj, desc, context, this)
                }
        }
    }
}

object DefaultSerializationProgression {

    implicit class Identity(val obj: Any) {

        override def hashCode(): Int = System.identityHashCode(obj)

        override def equals(obj: Any): Boolean = obj match {
            case id: Identity => JavaUtils.sameInstance(id.obj, this.obj) //got an error "the result type of an implicit conversion must be more specific than AnyRef" if i put "obj eq this.obj"
            case _            => false
        }

        override def toString: String = obj.toString
    }

}
