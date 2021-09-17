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

package fr.linkit.api.connection.network

import fr.linkit.api.connection.ConnectionContext
import fr.linkit.api.connection.cache.SharedCacheManager
import fr.linkit.api.connection.packet.persistence.context.MutableReferencedObjectStore

import java.sql.Timestamp

trait Network {

    val connectionEngine: Engine

    val connection: ConnectionContext

    val refStore: MutableReferencedObjectStore

    def globalCache: SharedCacheManager

    def serverIdentifier: String

    def serverEngine: Engine

    def countConnections: Int

    def listEngines: List[Engine]

    def findEngine(identifier: String): Option[Engine]

    def isConnected(identifier: String): Boolean

    def startUpDate: Timestamp

    def attachToCacheManager(family: String): SharedCacheManager

    def declareNewCacheManager(family: String): SharedCacheManager

    def findCacheManager(family: String): Option[SharedCacheManager]

}