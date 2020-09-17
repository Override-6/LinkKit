package fr.overridescala.vps.ftp.client

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.nio.charset.Charset

import fr.overridescala.vps.ftp.api.Relay
import fr.overridescala.vps.ftp.api.packet.{PacketLoader, Protocol, SimplePacketChannel}
import fr.overridescala.vps.ftp.api.task.tasks._
import fr.overridescala.vps.ftp.api.task.{Task, TaskAction, TasksHandler}
import fr.overridescala.vps.ftp.api.transfer.{TransferDescription, TransferableFile}
import fr.overridescala.vps.ftp.api.utils.Constants

class RelayPoint(private val serverAddress: InetSocketAddress,
                 override val identifier: String) extends Relay {


    private val socketChannel = configSocket()
    private val tasksHandler = new TasksHandler()
    private val packetChannel = new SimplePacketChannel(socketChannel, tasksHandler)
    private val completerFactory = new RelayPointTaskCompleterFactory(tasksHandler)
    private val packetLoader = new PacketLoader()

    override def doDownload(description: TransferDescription): Task[Unit] =
        new DownloadTask(packetChannel, tasksHandler, description)

    override def doUpload(description: TransferDescription): Task[Unit] =
        new UploadTask(packetChannel, tasksHandler, description)

    override def requestAddress(id: String): Task[InetSocketAddress] =
        new AddressTask(packetChannel, tasksHandler, id)

    override def requestFileInformation(owner: InetSocketAddress, path: String): Task[TransferableFile] =
        new FileInfoTask(packetChannel, tasksHandler, owner, path)

    override def requestCreateFile(owner: InetSocketAddress, path: String): TaskAction[Unit] = {
        new CreateFileTask(path, owner, packetChannel, tasksHandler)
    }

    override def start(): Unit = {
        val thread = new Thread(() => {
            println("ready !")
            println("current encoding is " + Charset.defaultCharset().name())
            val buffer = ByteBuffer.allocate(Constants.MAX_PACKET_LENGTH)
            //enable the task management
            tasksHandler.start()
            while (true) {
                try {
                    updateNetwork(buffer)
                } catch {
                    case e: Throwable => {
                        e.printStackTrace()
                        Console.err.println("suddenly disconnected from the server.")
                        return
                    }
                }
            }
        })
        thread.setName("RelayPoint")
        thread.start()
    }

    override def close(): Unit = {
        socketChannel.close()
    }

    def updateNetwork(buffer: ByteBuffer): Unit = synchronized {

        val count = socketChannel.read(buffer)
        if (count < 1)
            return

        val bytes = new Array[Byte](count)
        buffer.flip()
        buffer.get(bytes)

        packetLoader.add(bytes)

        var packet = packetLoader.nextPacket
        while (packet != null) {
            tasksHandler.handlePacket(packet, completerFactory, packetChannel)
            packet = packetLoader.nextPacket
        }

        buffer.clear()
    }

    def configSocket(): SocketChannel = {
        println("connecting to server...")
        val socket = SocketChannel.open(serverAddress)
        println("connected !")
        socket.configureBlocking(true)
        socket
    }

    //initial tasks
    Runtime.getRuntime.addShutdownHook(new Thread(() => close()))
    new InitTask(tasksHandler, packetChannel, identifier).queue(_, msg => {
        Console.err.print(s"unable to connect to the server : $msg")
        close()
    })

}