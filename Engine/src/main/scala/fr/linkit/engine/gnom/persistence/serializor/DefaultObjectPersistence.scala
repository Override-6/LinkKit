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

package fr.linkit.engine.gnom.persistence.serializor

import fr.linkit.api.gnom.cache.sync.generation.SyncClassCenter
import fr.linkit.api.gnom.persistence.obj.PoolObject
import fr.linkit.api.gnom.persistence.{ObjectPersistence, PersistenceBundle}
import fr.linkit.engine.gnom.persistence.serializor.read.PacketReader
import fr.linkit.engine.gnom.persistence.serializor.write.{PacketWriter, SerializerObjectPool}

import java.nio.ByteBuffer

class DefaultObjectPersistence(center: SyncClassCenter) extends ObjectPersistence {

    override val signature: Seq[Byte] = Seq(12)

    override def isSameSignature(buffer: ByteBuffer): Boolean = {
        val pos    = buffer.position()
        val result = signature.forall(buffer.get.equals)
        buffer.position(pos)
        result
    }

    override def serializeObjects(objects: Array[AnyRef])(bundle: PersistenceBundle): Unit = {
        val buffer = bundle.buff
        buffer.put(signature.toArray)
        val writer = new PacketWriter(bundle)
        writer.addObjects(objects)
        writer.writePool()
        val pool = writer.getPool
        writeEntries(objects, writer, pool)
    }

    private def writeEntries(objects: Array[AnyRef], writer: PacketWriter,
                             pool: SerializerObjectPool): Unit = {
        //Write the size
        writer.putRef(objects.length)
        //Write the content

        for (o <- objects) {
            val idx = pool.globalPosition(o)
            writer.putRef(idx)
        }
    }

    override def deserializeObjects(bundle: PersistenceBundle)(forEachObjects: AnyRef => Unit): Unit = {
        val buff = bundle.buff
        checkSignature(buff)

        val reader = new PacketReader(bundle, center)
        reader.initPool()
        val contentSize = buff.getChar
        val pool        = reader.getPool
        for (_ <- 0 until contentSize) {
            val obj = pool.getAny(reader.readNextRef) match {
                case o: PoolObject[AnyRef]            => o.value
                case o: AnyRef                        => o
            }
            forEachObjects(obj)
        }
    }


    private def checkSignature(buff: ByteBuffer): Unit = {
        if (!isSameSignature(buff))
            throw new IllegalArgumentException("Signature mismatches !")
        buff.position(buff.position() + signature.length)
    }

}
