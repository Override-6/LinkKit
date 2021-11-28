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

import fr.linkit.api.gnom.packet.traffic.PacketInjectable

class SimpleInjection(override val injectablePath: Array[Int]) extends PacketInjection with PacketInjectionControl {

    private var processing = false

    override def isProcessing: Boolean = processing

    override def markAsProcessing(): Unit = processing = true

    override def canAcceptMoreInjection: Boolean = ???

    override def nextIdentifier: Int = ???

    override def haveMoreIdentifier: Boolean = ???

    override def process(injectable: PacketInjectable): Unit = ???
}
