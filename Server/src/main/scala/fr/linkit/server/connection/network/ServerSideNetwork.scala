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

package fr.linkit.server.connection.network

import fr.linkit.api.connection.cache.SharedCacheManager
import fr.linkit.api.connection.cache.obj.behavior.ObjectBehaviorStore
import fr.linkit.api.connection.packet.traffic.PacketTraffic
import fr.linkit.engine.connection.cache.SharedCacheOriginManager
import fr.linkit.engine.connection.cache.obj.DefaultSynchronizedObjectCenter
import fr.linkit.engine.connection.cache.obj.instantiation.ConstructorBundle
import fr.linkit.engine.connection.network.AbstractNetwork.GlobalCacheID
import fr.linkit.engine.connection.network.{AbstractNetwork, NetworkDataTrunk}
import fr.linkit.server.connection.ServerConnection

class ServerSideNetwork(serverConnection: ServerConnection)(implicit traffic: PacketTraffic)
        extends AbstractNetwork(serverConnection, traffic.defaultPersistenceConfig.getReferenceStore) {

    trunk.addCacheManager(globalCache)

    override def serverIdentifier: String = serverConnection.currentIdentifier

    override protected def retrieveDataTrunk(store: ObjectBehaviorStore): NetworkDataTrunk = {
        globalCache.attachToCache(0, DefaultSynchronizedObjectCenter[NetworkDataTrunk](this))
                .syncObject(0, ConstructorBundle[NetworkDataTrunk](this), store)
    }

    override protected def createGlobalCache: SharedCacheManager = {
        new SharedCacheOriginManager(GlobalCacheID, this, networkStore.createStore(GlobalCacheID.hashCode))
    }

    def removeEngine(identifier: String): Unit = {
        findEngine(identifier).foreach(trunk.removeEngine)
    }
}
