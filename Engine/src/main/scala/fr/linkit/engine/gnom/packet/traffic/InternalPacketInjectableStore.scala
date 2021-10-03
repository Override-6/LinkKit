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

import fr.linkit.api.gnom.persistence.context.PersistenceConfig
import fr.linkit.api.gnom.packet.traffic.injection.PacketInjectionController

trait InternalPacketInjectableStore {

    def getPersistenceConfig(path: Array[Int]): PersistenceConfig

    protected def getPersistenceConfig(path: Array[Int], pos: Int): PersistenceConfig

    def inject(injection: PacketInjectionController): Unit
}
