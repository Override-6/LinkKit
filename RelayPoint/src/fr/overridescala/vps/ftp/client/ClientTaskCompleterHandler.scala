package fr.overridescala.vps.ftp.client

import fr.overridescala.vps.ftp.api.Relay
import fr.overridescala.vps.ftp.api.exceptions.TaskException
import fr.overridescala.vps.ftp.api.packet.{DataPacket, TaskInitPacket}
import fr.overridescala.vps.ftp.api.task.tasks._
import fr.overridescala.vps.ftp.api.task.{TaskCompleterHandler, TasksHandler}
import fr.overridescala.vps.ftp.api.utils.Utils
import fr.overridescala.vps.ftp.client.tasks.InitTaskCompleter

import scala.collection.mutable

class ClientTaskCompleterHandler(private val relay: Relay)
        extends TaskCompleterHandler {

    private lazy val completers: mutable.Map[String, (TaskInitPacket, TasksHandler, String) => Unit] =
        new mutable.HashMap[String, (TaskInitPacket, TasksHandler, String) => Unit]()

    override def handleCompleter(initPacket: TaskInitPacket, ownerID: String, tasksHandler: TasksHandler): Unit = {
        val taskType = initPacket.taskType
        val content = initPacket.content
        val taskID = initPacket.taskID
        val contentString = new String(content)
        val task = taskType match {
            case UploadTask.TYPE => DownloadTask(Utils.deserialize(content))
            case DownloadTask.TYPE => UploadTask(Utils.deserialize(content))
            case FileInfoTask.TYPE => new FileInfoTask.FileInfoCompleter(contentString)
            case CreateFileTask.TYPE => new CreateFileTask.CreateFileCompleter(new String(content.slice(1, content.length)), content(0) == 1)
            case InitTaskCompleter.TYPE => new InitTaskCompleter(relay)
            case PingTask.TYPE => new PingTask.PingCompleter()
            //reverse the boolean for completer
            //(down <-> up & up <-> down)
            case StressTestTask.TYPE => new StressTestTask.StressTestCompleter(new String(content.slice(2, content.length)).toLong, content(0) != 1, content(1) == 1)

            case _ => null
        }
        if (task != null) {
            tasksHandler.registerTask(task, taskID, false)
            return
        }

        val completerSupplier = completers(taskType)
        if (completerSupplier == null)
            throw new TaskException("could not find completer for task " + taskType)
        completerSupplier(initPacket, tasksHandler, ownerID)
    }

    override def putCompleter(taskType: String, supplier: (TaskInitPacket, TasksHandler, String) => Unit): Unit =
        completers.put(taskType, supplier)

}