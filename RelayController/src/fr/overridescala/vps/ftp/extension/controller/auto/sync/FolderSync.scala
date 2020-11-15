package fr.overridescala.vps.ftp.`extension`.controller.auto.sync

import java.io.File
import java.nio.file._
import java.util.concurrent.ThreadLocalRandom

import fr.overridescala.vps.ftp.api.Relay
import fr.overridescala.vps.ftp.api.packet.fundamental.DataPacket
import fr.overridescala.vps.ftp.api.packet.{Packet, PacketChannel}
import fr.overridescala.vps.ftp.api.utils.Utils
import fr.overridescala.vps.ftp.`extension`.controller.auto.Automation

import scala.collection.mutable.ListBuffer

class FolderSync(relay: Relay,
                 targetRelay: String,
                 localPath: String,
                 targetPath: String,
                 channelID: Int) extends Automation with PathEventListener {

    implicit private val channel: PacketChannel.Async = relay.createAsyncChannel(targetRelay, channelID)
    private val ignoredPaths = ListBuffer.empty[String]
    private val random = ThreadLocalRandom.current()
    private val folderWatcher = new FolderWatcher(Paths.get(localPath))
    folderWatcher.register(this)

    channel.setOnPacketReceived(p => {
        handlePacket(p)
    })

    override def start(): Unit = folderWatcher.start(1500)

    override def onCreate(affected: Path): Unit = {
        if (Files.isDirectory(affected)) {
            channel.sendPacket(DataPacket("mkdirs", affected.toString))
            return
        }
        onModify(affected)
    }

    override def onDelete(affected: Path): Unit = {
        val affectedPath = affected.toString
        if (ignoredPaths.contains(affectedPath)) {
            ignoredPaths -= affectedPath
            return
        }
        channel.sendPacket(DataPacket("remove", affected.toString))
    }

    override def onModify(affected: Path): Unit = {
        val affectedPath = affected.toString
        if (ignoredPaths.contains(affectedPath)) {
            ignoredPaths -= affectedPath
            return
        }
        val in = Files.newInputStream(affected)
        val buff = new Array[Byte](Files.size(affected).toInt)
        val read = in.read(buff)
        val bytes = buff.slice(0, read)
        channel.sendPacket(DataPacket(s"SUPLOAD:$affected", bytes))
        in.close()
    }

    override def onRename(affected: Path, newName: String): Unit = {
        println(s"RENAME : $newName")
        val affectedPath = affected.toString
        if (ignoredPaths.contains(affectedPath)) {
            ignoredPaths -= affectedPath
            return
        }
        channel.sendPacket(DataPacket(s"rename>$newName", affected.toString))
    }

    private def deletePath(path: Path): Unit = {
        if (Files.notExists(path))
            return
        deleteRecursively(path)

        def deleteRecursively(path: Path): Unit = {
            if (Files.notExists(path))
                return
            if (!Files.isDirectory(path)) {
                Files.delete(path)
            } else {
                Files.list(path).forEach(deleteRecursively)
            }
        }
    }

    private def handlePacket(packet: Packet): Unit = {
        val data = packet.asInstanceOf[DataPacket]
        val order = data.header
        if (order.contains(':')) {
            handleComplexOrder(data)
            return
        }
        val path = toLocal(new String(data.content))

        if (order.startsWith("rename>") && Files.exists(path)) {
            val newName = order.split(">").last
            val renamed = path.resolveSibling(newName)
            ignoredPaths += renamed.toString
            Files.move(path, renamed, StandardCopyOption.ATOMIC_MOVE)
            return
        }

        ignoredPaths += path.toString
        order match {
            case "delete" => deletePath(path)
            case "mkdirs" if Files.notExists(path) => Files.createDirectories(path)
            case _ =>
        }
    }

    private def handleComplexOrder(data: DataPacket): Unit = {
        val order = data.header
        val affectedPath = order.split(":").last
        val path = toLocal(affectedPath)
        ignoredPaths += path.toString

        if (order.startsWith("SUPLOAD")) {
            val out = Files.newOutputStream(path, StandardOpenOption.CREATE)
            out.write(data.content)
            out.flush()
            out.close()
        }
    }

    private def toLocal(nonLocalPath: String): Path = {
        val relativePath = Utils.subPathOfUnknownFile(nonLocalPath, targetNameCount)
        Utils.formatPath(localPath + relativePath)
    }

    private val targetNameCount = targetPath.count(_ == File.separatorChar)

}
