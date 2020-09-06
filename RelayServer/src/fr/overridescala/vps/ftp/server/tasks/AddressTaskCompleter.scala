package fr.overridescala.vps.ftp.server.tasks

import java.net.InetSocketAddress

import fr.overridescala.vps.ftp.api.packet.{PacketChannel, TaskPacket}
import fr.overridescala.vps.ftp.api.task.{Task, TaskAchiever, TaskType, TasksHandler}
import fr.overridescala.vps.ftp.server.RelayServer

class AddressTaskCompleter(private val packetChannel: PacketChannel,
                           private val server: RelayServer,
                           private val tasksHandler: TasksHandler,
                           private val id: String)
        extends TaskAchiever {

    override val taskType: TaskType = TaskType.ADDRESS

    override def achieve(): Unit = {
        val address = server.getAddress(id)
        if (address == null) {
            val errorMsg = "unable to retrieve the associated address from identifier"
            val errorResponse = new TaskPacket(taskType, "ERROR", errorMsg.getBytes)
            packetChannel.sendPacket(errorResponse)
            return
        }

        val response = new TaskPacket(taskType, "OK", address.toString.getBytes)
        packetChannel.sendPacket(response)
    }
}
