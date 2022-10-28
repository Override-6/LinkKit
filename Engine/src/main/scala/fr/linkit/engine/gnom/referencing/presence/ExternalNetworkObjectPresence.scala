/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can USE it as you want.
 * You can download this source code, and modify it ONLY FOR PRIVATE USE but you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.gnom.referencing.presence

import fr.linkit.api.gnom.network.{IdentifierTag, NetworkFriendlyEngineTag, UniqueTag}
import fr.linkit.api.gnom.referencing.NetworkObjectReference
import fr.linkit.api.gnom.referencing.presence.ObjectPresenceState._
import fr.linkit.api.gnom.referencing.presence.{NetworkObjectPresence, ObjectPresenceState}
import fr.linkit.api.internal.system.log.AppLoggers

import scala.collection.mutable

class ExternalNetworkObjectPresence[R <: NetworkObjectReference](handler: AbstractNetworkPresenceHandler[R], val reference: R)
        extends NetworkObjectPresence {

    private val presences = mutable.HashMap.empty[IdentifierTag, ObjectPresenceState]
    
    override def isPresenceKnownFor(engineTag: UniqueTag with NetworkFriendlyEngineTag): Boolean = {
        val engineId = handler.
        presences.contains(engineId)
    }
    
    override def getPresenceFor(engineId: UniqueTag with NetworkFriendlyEngineTag): ObjectPresenceState = {
        if (engineId == null)
            throw new NullPointerException("engineId is null.")
        this.synchronized {
            presences.getOrElseUpdate(engineId, {
                val present = handler.askIfPresent(engineId, reference)
                if (present) PRESENT
                else NOT_PRESENT
            })
        }
    }

    def setToPresent(engineId: String): Unit = {
        if (engineId == null)
            throw new NullPointerException()
        presences(engineId) = PRESENT
    }

    def setToNotPresent(engineId: String): Unit = {
        if (engineId == null)
            throw new NullPointerException()
        presences(engineId) = NOT_PRESENT
    }
}
