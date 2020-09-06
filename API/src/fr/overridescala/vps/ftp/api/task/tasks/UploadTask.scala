package fr.overridescala.vps.ftp.api.task.tasks

import java.nio.file.{Files, Path}

import fr.overridescala.vps.ftp.api.packet.{PacketChannel, TaskPacket}
import fr.overridescala.vps.ftp.api.task.{Task, TaskAchiever, TaskType, TasksHandler}
import fr.overridescala.vps.ftp.api.transfer.TransferDescription
import fr.overridescala.vps.ftp.api.utils.{Constants, Utils}

class UploadTask(private val channel: PacketChannel,
                 private val handler: TasksHandler,
                 private val desc: TransferDescription)
        extends Task[Unit]() with TaskAchiever {


    override val taskType: TaskType = TaskType.UPLOAD

    override def enqueue(): Unit = handler.register(this, ownFreeWill = true)

    override def preAchieve(): Unit = {
        val packet = new TaskPacket(taskType, "TD", Utils.serialize(desc))
        channel.sendPacket(packet)
    }


    override def achieve(): Unit = {
        val path = Path.of(desc.source.getPath)
        if (checkPath(path))
            return
        val stream = Files.newInputStream(path)
        var totalBytesSent: Long = 0
        val totalBytes: Float = desc.transferSize
        var id = -0
        while (totalBytesSent < totalBytes) {
            try {
                val bytes = new Array[Byte](Constants.MAX_PACKET_LENGTH - 512)
                totalBytesSent += stream.read(bytes)
                id += 1
                if (makeDataTransfer(bytes, id))
                    return
                val percentage = totalBytesSent / totalBytes * 100
                print(s"sent = $totalBytesSent, total = $totalBytes, percentage = ${percentage}\r")
            } catch {
                case e: Throwable => {
                    var msg = e.getMessage
                    if (msg == null)
                        msg = "an error occured while perforing file upload task"
                    channel.sendPacket(new TaskPacket(taskType, "ERROR", msg.getBytes()))
                    return
                }
            }
        }
        val percentage = totalBytesSent / totalBytes * 100
        println(s"sent = $totalBytesSent, total = $totalBytes, percentage = $percentage\r")
        success(path)
    }

    def checkPath(path: Path): Boolean = {
        if (Files.notExists(path)) {
            val errorMsg = "could not upload invalid file path : this file does not exists"
            channel.sendPacket(new TaskPacket(taskType, "ERROR", errorMsg.getBytes()))
            error(errorMsg)
            return true
        }
        false
    }

    /**
     * makes one data transfer.
     *
     * @return true if the transfer need to be aborted, false instead
     **/
    def makeDataTransfer(bytes: Array[Byte], id: Int): Boolean = {

        channel.sendPacket(new TaskPacket(taskType, s"$id", bytes))
        val packet = channel.nextPacket()
        if (packet.header.equals("ERROR")) {
            error(new String(packet.content))
            return true
        }
        try {
            val packetId = Integer.parseInt(packet.header)
            if (packetId != id) {
                val errorMsg = new String(s"packet id was unexpected (id: $packetId, expected: $id")
                val packet = new TaskPacket(taskType, "ERROR", errorMsg.getBytes)
                channel.sendPacket(packet)
                error(errorMsg)
                return true
            }
        } catch {
            case e: NumberFormatException => {
                val packet = new TaskPacket(taskType, "ERROR", e.getMessage.getBytes)
                channel.sendPacket(packet)
                error(e.getMessage)
                return true
            }
        }

        false
    }
}
