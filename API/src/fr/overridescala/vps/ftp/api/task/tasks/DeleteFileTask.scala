package fr.overridescala.vps.ftp.api.task.tasks

import java.io.{FileNotFoundException, IOException}
import java.nio.file.{Files, Path}

import fr.overridescala.vps.ftp.api.packet.ext.fundamental.{EmptyPacket, ErrorPacket}
import fr.overridescala.vps.ftp.api.task.tasks.DeleteFileTask.TYPE
import fr.overridescala.vps.ftp.api.task.{Task, TaskExecutor, TaskInitInfo}


case class DeleteFileTask(targetId: String, targetPath: String) extends Task[Unit](targetId) {

    override val initInfo: TaskInitInfo =
        TaskInitInfo.of(TYPE, targetID, targetPath)

    override def execute(): Unit = {
        channel.nextPacket() match {
            case errorPacket: ErrorPacket =>
                errorPacket.printError()
                error(errorPacket.errorMsg)
            case _: EmptyPacket => success()
        }
    }
}

object DeleteFileTask {
    val TYPE = "DLF"

    class Completer(pathString: String) extends TaskExecutor {

        println("instancied !")

        override def execute(): Unit = {
            println("COMPLETING")
            val path = Path.of(pathString)
            println(s"path = ${path}")
            if (Files.notExists(path)) {
                channel.sendPacket(ErrorPacket(
                    classOf[FileNotFoundException].getName,
                    s"could not find file ot folder in path $path",
                    s"$path not exists"))
                return
            }
            try {
                Files.delete(path)
                println("DELETED")
            } catch {
                case e: IOException =>
                    channel.sendPacket(ErrorPacket(
                        e.getClass.getName,
                        s"could not delete file or folder in $path",
                        e.getMessage
                    ))

            }
            channel.sendPacket(EmptyPacket())
        }

    }

}

