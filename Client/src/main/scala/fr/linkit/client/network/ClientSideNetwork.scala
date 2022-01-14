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

package fr.linkit.client.network

import fr.linkit.api.gnom.cache.sync.contract.descriptors.ContractDescriptorData
import fr.linkit.api.gnom.cache.{CacheSearchMethod, SharedCacheManager}
import fr.linkit.engine.gnom.cache.SharedCacheDistantManager
import fr.linkit.engine.gnom.cache.sync.DefaultSynchronizedObjectCache
import fr.linkit.engine.gnom.network.AbstractNetwork.GlobalCacheID
import fr.linkit.engine.gnom.network.{AbstractNetwork, NetworkDataTrunk}
import fr.linkit.engine.gnom.packet.traffic.SocketPacketTraffic

class ClientSideNetwork(traffic: SocketPacketTraffic) extends AbstractNetwork(traffic) {

    override protected def retrieveDataTrunk(contracts: ContractDescriptorData): NetworkDataTrunk = {
        val trunk = globalCache.attachToCache(0, DefaultSynchronizedObjectCache[NetworkDataTrunk](contracts, this), CacheSearchMethod.GET_OR_CRASH)
                .findObject(0)
                .getOrElse {
                    throw new NoSuchElementException("netork data trunk not found.")
                }
        trunk
    }

    override protected def createGlobalCache: SharedCacheManager = {
        traffic.setNetwork(this)
        new SharedCacheDistantManager(GlobalCacheID, serverIdentifier, this, networkStore.createStore(GlobalCacheID.hashCode))
    }

    override def serverIdentifier: String = traffic.serverIdentifier

}