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

package fr.linkit.server.config.schematic

import fr.linkit.api.internal.system.AppLogger
import fr.linkit.api.application.config.ApplicationInstantiationException
import fr.linkit.api.application.config.schematic.AppSchematic
import fr.linkit.server.ServerApplication
import fr.linkit.server.config.ServerConnectionConfiguration

import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

abstract class ServerAppSchematic extends AppSchematic[ServerApplication] {

    protected[schematic] val serverConfigs: ListBuffer[ServerConnectionConfiguration] =
        ListBuffer.empty[ServerConnectionConfiguration]

    @throws[ApplicationInstantiationException]
    override def setup(a: ServerApplication): Unit = {
        for (configuration <- serverConfigs) {
            try {
                AppLogger.debug(s"Loading configuration ${configuration.configName}")
                a.openServerConnection(configuration)
            } catch {
                case NonFatal(e) =>
                    val name: String = configuration.configName
                    throw new ApplicationInstantiationException(s"Failed to load configuration '$name'", e)
            }
        }
    }
}