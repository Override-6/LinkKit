/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.gnom.packet.traffic.channel.request

import fr.linkit.api.gnom.packet.channel.request.{ResponseHolder, SubmitterPacket}
import fr.linkit.api.internal.system.AppLogger
import fr.linkit.api.internal.concurrency.WorkerPools.currentTasksId
import fr.linkit.engine.internal.utils.ConsumerContainer

import java.util.concurrent.BlockingQueue

case class SimpleResponseHolder(override val id: Int,
                                queue: BlockingQueue[AbstractSubmitterPacket],
                                handler: SimpleRequestPacketChannel) extends ResponseHolder {

    private val responseConsumer = ConsumerContainer[AbstractSubmitterPacket]()

    override def nextResponse: SubmitterPacket = {
        AppLogger.vDebug(s"$currentTasksId <> Waiting for response... ($id) " + this)
        val response = queue.take()
        AppLogger.vError(s"$currentTasksId <> RESPONSE ($id) RECEIVED ! $response, $queue")
        response
    }

    override def addOnResponseReceived(callback: SubmitterPacket => Unit): Unit = {
        responseConsumer += callback
    }

    private[request] def pushResponse(response: AbstractSubmitterPacket): Unit = {
        AppLogger.vError(s"$currentTasksId <> ADDING RESPONSE $response FOR REQUEST $this")
        queue.add(response)
        responseConsumer.applyAllLater(response)
        AppLogger.vError(s"$currentTasksId <> RESPONSE $response ADDED TO REQUEST $this")
    }

}