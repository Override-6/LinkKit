package fr.`override`.linkit.api.concurency

import java.util.concurrent.{BlockingDeque, LinkedBlockingDeque}

import fr.`override`.linkit.api.concurency.PacketWorkerThread.checkNotCurrent
import fr.`override`.linkit.api.exception.IllegalPacketWorkerLockException

class RelayWorkerThread extends Thread with AutoCloseable {

    private val queue: BlockingDeque[() => Unit] = new LinkedBlockingDeque()
    private var closed = false

    start()

    def runLater(action: => Unit): ParallelAction[Unit] = {
        queue.addLast(() => action)

    }

    override def run(): Unit = while(!closed) queue.takeFirst().apply()

    override def close(): Unit = {
        closed = true
        interrupt()
    }

}

object RelayWorkerThread {

    /**
     * Packet Worker Threads have to be registered in this ThreadGroup in order to throw an exception when a relay worker thread
     * is about to be locked by a monitor, that concern packet reception (example: lockers of BlockingQueues in PacketChannels)
     *
     * @see [[IllegalPacketWorkerLockException]]
     * */
    val packetReaderThreadGroup: ThreadGroup = new ThreadGroup("Relay Packet Worker")

    def checkCurrentIsWorker(): Unit = {
        if (!isCurrentWorkerThread)
            throw new IllegalStateException("This action must be performed in a Packet Worker thread !")
    }

    def checkCurrentIsNotWorker(): Unit = {
        if (isCurrentWorkerThread)
            throw new IllegalStateException("This action must not be performed in a Packet Worker thread !")
    }

    def currentThread(): Option[RelayWorkerThread] = {
        Thread.currentThread() match {
            case worker: RelayWorkerThread => Some(worker)
            case _ => None
        }
    }

    def isCurrentWorkerThread: Boolean = {
        currentThread().isDefined
    }

    def safeLock(anyRef: AnyRef, timeout: Long = 0): Unit = {
        checkCurrentIsNotWorker()
        anyRef.synchronized {
            anyRef.wait(timeout)
        }
    }
}
