/*
 *  Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can only use it for personal uses, studies or documentation.
 *  You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 *  ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 *  Please contact maximebatista18@gmail.com if you need additional information or have any
 *  questions.
 */

package fr.linkit.engine.connection.packet.traffic.channel.request


import fr.linkit.api.connection.packet.channel.ChannelScope.ScopeFactory
import fr.linkit.api.connection.packet.channel.{ChannelScope, PacketChannel}
import fr.linkit.api.connection.packet.traffic.PacketInjectableFactory
import fr.linkit.api.connection.packet.traffic.injection.PacketInjection
import fr.linkit.api.local.system.AppLogger
import fr.linkit.engine.connection.packet.traffic.ChannelScopes
import fr.linkit.engine.connection.packet.traffic.channel.AbstractPacketChannel
import fr.linkit.engine.local.concurrency.pool.BusyWorkerPool
import fr.linkit.api.local.concurrency.WorkerPools.currentTasksId
import fr.linkit.engine.local.utils.ConsumerContainer
import org.jetbrains.annotations.Nullable
import java.util.NoSuchElementException

import fr.linkit.api.local.concurrency.WorkerPools

import scala.collection.mutable

class RequestPacketChannel(@Nullable parent: PacketChannel, scope: ChannelScope) extends AbstractPacketChannel(parent, scope) {

    private val requestHolders           = mutable.LinkedHashMap.empty[Int, RequestHolder]
    private val requestConsumers         = ConsumerContainer[RequestBundle]()
    @volatile private var requestID      = 0

    //debug only
    private val source = scope.traffic.supportIdentifier

    AppLogger.vDebug(s"Created $this, of parent $parent")

    override def handleInjection(injection: PacketInjection): Unit = {
        val coords = injection.coordinates
        injection.attachPin { (packet, attr) =>
            packet match {
                case request: RequestPacket =>
                    AppLogger.vDebug(this)
                    AppLogger.vDebug(s"${currentTasksId} <> $source: INJECTING REQUEST $request with attributes ${request.getAttributes}" + this)
                    request.setAttributes(attr)

                    val submitterScope = scope.shareWriter(ChannelScopes.retains(coords.senderID))
                    val submitter = new ResponseSubmitter(request.id, submitterScope)

                    requestConsumers.applyAllLater(RequestBundle(this, request, coords, submitter))

                case response: ResponsePacket =>
                    AppLogger.vDebug(s"${currentTasksId} <> $source: INJECTING RESPONSE $response with attributes ${response.getAttributes}" + this)
                    response.setAttributes(attr)

                    requestHolders.get(response.id) match {
                        case Some(request) => request.pushResponse(response)
                        case None          => throw new NoSuchElementException(s"(${Thread.currentThread().getName}) Response.id not found (${response.id}) ($requestHolders)")
                    }
            }
        }
    }

    def addRequestListener(callback: RequestBundle => Unit): Unit = {
        requestConsumers += callback
    }

    def makeRequest(scopeFactory: ScopeFactory[_ <: ChannelScope]): RequestSubmitter = {
        val writer = traffic.newWriter(identifier)
        makeRequest(scopeFactory(writer))
    }

    def makeRequest(scope: ChannelScope): RequestSubmitter = {
        val pool = WorkerPools.ensureCurrentIsWorker()

        val requestID = nextRequestID
        new RequestSubmitter(requestID, scope, pool, this)
    }

    private def nextRequestID: Int = {
        requestID += 1
        requestID
    }

    private[request] def addRequestHolder(holder: RequestHolder): Unit = {
        requestHolders.put(holder.id, holder)
    }

    private[request] def removeRequestHolder(holder: RequestHolder): Unit = {
        requestHolders -= holder.id
    }

}

object RequestPacketChannel extends PacketInjectableFactory[RequestPacketChannel] {

    override def createNew(@Nullable parent: PacketChannel, scope: ChannelScope): RequestPacketChannel = new RequestPacketChannel(parent, scope)
}
