package fr.linkit.engine.internal.debug

import fr.linkit.api.application.connection.ConnectionContext
import fr.linkit.api.internal.concurrency.Worker
import fr.linkit.engine.gnom.packet.traffic.AbstractPacketTraffic

import java.io.PrintStream
import scala.collection.mutable

object Debugger {

    private[debug] val threadStates = mutable.HashMap.empty[Thread, ThreadWorkStack]

    private val connections = mutable.Set.empty[ConnectionContext]

    private[linkit] def registerConnection(connectionContext: ConnectionContext): Unit = {
        connections += connectionContext
    }

    def push(step: => Step): Unit = {
        val stp = step
        currentStack.push(stp)
        stp match {
            case _: RequestStep => DeadlockWatchdog.notifyNewRequestPending()
            case _              =>
        }
    }

    def pop(): Step = {
        val stack = currentStack
        val step  = stack.pop()
        if (stack.isEmpty)
            threadStates.remove(stack.thread)
        step
    }

    private def currentStack: ThreadWorkStack = {
        val currentThread = Thread.currentThread()
        threadStates.getOrElseUpdate(currentThread, new ThreadWorkStack(currentThread))
    }

    def dumpWorkers(out: PrintStream = System.out): Unit = {
        val (workers, others) = threadStates.keys.partitionMap {
            case w: Worker => Left(w)
            case o         => Right(o)
        }
        workers.groupBy(_.pool).foreach { case (pool, poolWorkers) =>
            out.println(s"worker pool '${pool.name}': ")
            poolWorkers.toArray.sortBy(_.getName).foreach(threadStates(_).printStack(out))
        }
        if (others.nonEmpty) {
            out.println("other threads:")
            others.toArray.sortBy(_.getName).foreach(threadStates(_).printStack(out))
        }
    }

    def dumpTraffic(out: PrintStream = System.out): Unit = connections.foreach(dumpConnectionTraffic(_, out))

    def dumpConnectionTraffic(connection: ConnectionContext, out: PrintStream = System.out): Unit = {
        val connectionServerID = connection.network.serverIdentifier
        connection.traffic match {
            case traffic: AbstractPacketTraffic =>
                out.println(s"Dumping connection '$connectionServerID' traffic:")
                traffic.dump(out)
            case _                              =>
                out.println(s"Could not dump connection '$connectionServerID' traffic.")
        }
    }

}
