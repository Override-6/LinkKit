package fr.overridescala.vps.ftp.api.system

import fr.overridescala.vps.ftp.api.packet.{PacketChannelsHandler, SyncPacketChannel}
import fr.overridescala.vps.ftp.api.system.SystemPacketChannel.SystemChannelID

class SystemPacketChannel(connectedID: String,
                          ownerID: String,
                          handler: PacketChannelsHandler) extends SyncPacketChannel(connectedID, ownerID, SystemChannelID, handler) {

    private val notifier = handler.notifier

    def sendOrder(systemOrder: SystemOrder, reason: Reason): Unit = {
        handler.sendPacket(SystemPacket(systemOrder, reason)(this))
        notifier.onSystemOrderSent(systemOrder)
    }

}

object SystemPacketChannel {
    val SystemChannelID = 6
}
