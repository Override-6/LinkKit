package fr.overridescala.vps.ftp.api.packet

trait PacketChannelManager {

    def addPacket(packet: DataPacket): Unit

    val ownerID: String

}