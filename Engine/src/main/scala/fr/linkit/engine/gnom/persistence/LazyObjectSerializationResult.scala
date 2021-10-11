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

package fr.linkit.engine.gnom.persistence

import fr.linkit.api.gnom.packet.{Packet, PacketAttributes, PacketCoordinates}
import fr.linkit.api.gnom.persistence.{ObjectPersistence, ObjectSerializationResult, TransferInfo}
import fr.linkit.engine.internal.utils.NumberSerializer

import java.nio.ByteBuffer

abstract class LazyObjectSerializationResult(info: TransferInfo,
                                             private val serializer: ObjectPersistence) extends ObjectSerializationResult {

    override val coords: PacketCoordinates = info.coords

    override val attributes: PacketAttributes = info.attributes

    override val packet: Packet = info.packet

    protected def writeCoords(buff: ByteBuffer): Unit

    override lazy val buff: ByteBuffer = {
        val buff = ByteBuffer.allocate(10000)
        buff.position(4)
        writeCoords(buff)
        info.makeSerial(serializer, buff)
        val length = NumberSerializer.serializeInt(buff.position() - 4)
        buff.put(0, length) //write the packet's length
        buff.flip()
    }

}

object LazyObjectSerializationResult {

}