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

package fr.linkit.api.connection.packet.persistence

import fr.linkit.api.connection.packet.PacketCoordinates
import fr.linkit.api.connection.packet.persistence.PacketSerializer.PacketDeserial
import fr.linkit.api.connection.packet.persistence.context.PacketConfig

import java.nio.ByteBuffer

trait PacketSerializer extends Serializer {

    def serializePacket(objects: Array[AnyRef], coordinates: PacketCoordinates, buffer: ByteBuffer)(config: PacketConfig): Unit

    def deserializePacket(buff: ByteBuffer)(config: PacketConfig): PacketDeserial

}

object PacketSerializer {

    trait PacketDeserial {

        def getCoordinates: PacketCoordinates

        def forEachObjects(f: AnyRef => Unit): Unit
    }
}
