package fr.overridescala.vps.ftp.api.task.tasks

import fr.overridescala.vps.ftp.api.packet.PacketChannel
import fr.overridescala.vps.ftp.api.task.{Task, TaskExecutor, TasksHandler}
import fr.overridescala.vps.ftp.api.utils.Constants

class StressTestTask(private val channel: PacketChannel,
                     private val handler: TasksHandler,
                     private val totalDataLength: Long) extends Task[Unit](handler, Constants.SERVER_ID) {

    override def sendTaskInfo(): Unit = {
        channel.sendPacket("STRSS", s"$totalDataLength")
    }

    override def execute(): Unit = {
        var totalSent: Float = 0
        val capacity = Constants.MAX_PACKET_LENGTH - 256
        var bytes = new Array[Byte](capacity)
        while (totalSent < totalDataLength) {
            if (totalDataLength - totalSent < capacity)
                bytes = new Array[Byte]((totalDataLength - totalSent).asInstanceOf[Int])

            val t0 = System.currentTimeMillis()
            channel.sendPacket("PCKT", bytes)
            //channel.nextPacket()
            val t1 = System.currentTimeMillis()
            val time: Float = t1 - t0

            totalSent += capacity

            val percentage = totalSent / totalDataLength * 100
            print(s"\rjust sent ${capacity} in $time ms ", s"${capacity / (time / 1000)} bytes/s", s"$totalSent / $totalDataLength, $percentage%")
        }
        channel.sendPacket("END")
    }


}

object StressTestTask {

    class StressTestCompleter(private val channel: PacketChannel,
                              private val totalDataLength: Long) extends TaskExecutor {
        override def execute(): Unit = {
            var packet = channel.nextPacket()
            var totalReceived: Float = 0
            while (packet.header.equals("PCKT")) {
                val t0 = System.currentTimeMillis()
                packet = channel.nextPacket()
                val dataLength = packet.content.length
                val t1 = System.currentTimeMillis()
                val time:Float = t1 - t0

                totalReceived += dataLength

                val percentage = totalReceived / totalDataLength * 100
                print(s"\rjust received ${dataLength} in $time s", (dataLength / time) * 1000, "bytes/s ", s"$totalReceived / $totalDataLength, $percentage%")
            }
        }
    }

}
