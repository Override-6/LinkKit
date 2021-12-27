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

package fr.linkit.engine.gnom.packet.traffic.injection

import fr.linkit.api.gnom.packet._
import fr.linkit.api.gnom.packet.traffic.PacketInjectable
import fr.linkit.api.gnom.packet.traffic.injection.InjectionProcessorUnit
import fr.linkit.api.gnom.persistence.ObjectDeserializationResult

class PerformantInjectionProcessorUnit extends InjectionProcessorUnit {

    override def post(result: ObjectDeserializationResult, injectable: PacketInjectable): Unit = {
        //performance is simplicity
        result.makeDeserialization()
        val bundle = new PacketBundle {
            override val packet    : Packet            = result.packet
            override val attributes: PacketAttributes  = result.attributes
            override val coords    : PacketCoordinates = result.coords
        }
        injectable.inject(bundle)
    }
}
