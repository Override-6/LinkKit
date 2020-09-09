package fr.overridescala.vps.ftp.api.task.tasks

import fr.overridescala.vps.ftp.api.packet.PacketChannel
import fr.overridescala.vps.ftp.api.task.{Task, TaskExecutor, TasksHandler}
import fr.overridescala.vps.ftp.api.utils.Utils

class InitTask(private val handler: TasksHandler,
               private val channel: PacketChannel,
               private val id: String)
        extends Task[Unit](handler, channel.ownerAddress) with TaskExecutor {


    override def getInitPacket(): Unit = {
        channel.sendPacket("INIT", id.getBytes)
    }

    override def execute(): Unit = {
        val notAccepted = channel.nextPacket().header.equals("ERROR")
        if (notAccepted)
            error("no id where available.")
        else success(id)
    }

}
