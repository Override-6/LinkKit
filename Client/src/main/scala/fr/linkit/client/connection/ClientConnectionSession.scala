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

package fr.linkit.client.connection

import fr.linkit.api.gnom.persistence.PacketTranslator
import fr.linkit.api.application.packet.traffic.PacketTraffic.SystemChannelID
import fr.linkit.api.internal.system.event.EventNotifier
import fr.linkit.client.ClientApplication
import fr.linkit.client.config.ClientConnectionConfiguration
import fr.linkit.engine.application.packet.traffic.{ChannelScopes, DefaultPacketReader, DynamicSocket, SocketPacketTraffic}
import fr.linkit.engine.internal.concurrency.PacketReaderThread
import fr.linkit.engine.internal.system.SystemPacketChannel
import fr.linkit.engine.internal.system.event.DefaultEventNotifier

case class ClientConnectionSession(socket: DynamicSocket,
                                   info: ClientConnectionSessionInfo) {

    val appContext       : ClientApplication             = info.appContext
    val configuration    : ClientConnectionConfiguration = info.configuration
    val currentIdentifier: String                        = configuration.identifier
    val translator       : PacketTranslator              = info.translator
    val serverIdentifier : String                        = info.serverIdentifier
    val traffic          : SocketPacketTraffic           = new SocketPacketTraffic(socket, translator, configuration.defaultPersistenceConfigScript, appContext, currentIdentifier, serverIdentifier)
    val packetReader     : DefaultPacketReader           = new DefaultPacketReader(socket, appContext, traffic, translator)
    val readThread       : PacketReaderThread            = new PacketReaderThread(packetReader, serverIdentifier)
    val eventNotifier    : EventNotifier                 = new DefaultEventNotifier
    val systemChannel    : SystemPacketChannel           = traffic.getInjectable(SystemChannelID, SystemPacketChannel, ChannelScopes.discardCurrent)

}
