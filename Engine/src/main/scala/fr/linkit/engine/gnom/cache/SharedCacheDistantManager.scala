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

package fr.linkit.engine.gnom.cache

import fr.linkit.api.gnom.cache.{CacheContent, CacheNotAcceptedException, CacheOpenException, CacheSearchMethod}
import fr.linkit.api.gnom.network.Network
import fr.linkit.api.gnom.packet.Packet
import fr.linkit.api.gnom.packet.channel.request.RequestPacketBundle
import fr.linkit.api.gnom.packet.traffic.PacketInjectableStore
import fr.linkit.api.gnom.persistence.context.{Deconstructible, Persist}
import fr.linkit.api.internal.system.AppLogger
import fr.linkit.engine.gnom.packet.fundamental.RefPacket.{ObjectPacket, StringPacket}
import fr.linkit.engine.gnom.packet.fundamental.ValPacket.IntPacket
import fr.linkit.engine.gnom.packet.fundamental.{EmptyPacket, RefPacket}
import fr.linkit.engine.gnom.packet.traffic.ChannelScopes

final class SharedCacheDistantManager @Persist()(family: String,
                                                 override val ownerID: String,
                                                 network: Network,
                                                 store: PacketInjectableStore) extends AbstractSharedCacheManager(family, network, store) with Deconstructible {

    @transient private val ownerScope = prepareScope(ChannelScopes.include(ownerID))

    override def deconstruct(): Array[Any] = Array(family, ownerID, network, store)

    override def retrieveCacheContent(cacheID: Int, behavior: CacheSearchMethod): Option[CacheContent] = {
        println(s"Sending request to $ownerID in order to retrieve content of cache number $cacheID")
        val request = channel
                .makeRequest(ownerScope)
                .putAttribute("behavior", behavior)
                .addPacket(IntPacket(cacheID))
                .submit()

        val response = request.nextResponse
            response.nextPacket[Packet] match {
                case StringPacket(errorMsg)               =>
                    throw new CacheOpenException(s"Could not open cache '$cacheID' in shared cache manager <$family, $ownerID>. Received error message from '$ownerID': $errorMsg")
                case ref: RefPacket[Option[CacheContent]] =>
                    val content = ref.value
                    //println(s"Content '$cacheID' received ! ($content)")
                    content
            }
    }

    override protected def preCacheOpenChecks(cacheID: Int, cacheType: Class[_]): Unit = {
        channel.makeRequest(ownerScope)
                .addPacket(ObjectPacket((cacheID, cacheType)))
                .submit()
                .nextResponse
                .nextPacket[Packet] match {
            case EmptyPacket =>
            // OK, the cache is not open or is open and the given cacheType
            // is assignable and was accepted by the AttachHandler of the owner's cache handler.
            case StringPacket(msg: String) =>
                // The cache could not be accepted
                // (for any reason. Maybe because cacheType is not assignable with the other caches,
                // or because the AttachHandler of the cache refused the connection.)
                throw new CacheNotAcceptedException(s"This message comes from engine $ownerID: " + msg)
        }
    }

    override def handleRequest(requestBundle: RequestPacketBundle): Unit = {
        val response = requestBundle.responseSubmitter
        println(s"HANDLING REQUEST $requestBundle")
        val msg =
            s"""This request can't be processed by this engine.
               | The request must be send to the engine that hosts the manager.
               | (Current Identifier = $currentIdentifier, host identifier = $ownerID)""".stripMargin
        response.putAttribute("errorMsg", msg)
        response.submit()
    }

}
