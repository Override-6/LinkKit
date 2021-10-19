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

package fr.linkit.api.gnom.packet

import fr.linkit.api.gnom.network.ExecutorEngine

case class BroadcastPacketCoordinates(override val path: Array[Int],
                                      override val senderID: String,
                                      discardTargets: Boolean,
                                      targetIDs: Seq[String]) extends PacketCoordinates {

    override def forallConcernedTargets(action: String => Boolean): Boolean = {
        //FIXME discuss about Packet Broadcasting utility,
        // Broadcasting a packet is useful to clients to save bandwidth and persistence operation
        // However, with incoming enhancements that comes with the GNOL, object persistence configuration etc,
        // Broadcasting becomes hard to maintain as a serialized packet result can vary between engines,
        // So, either remove packet broadcasting (which can lead to performance issues) or add constraints to serialisation
        if (discardTargets) {
            val network = ExecutorEngine.currentEngine.network
            val allConnections = network.listEngines
            for (engine <- allConnections) {
                val engineId = engine.identifier
                if (!targetIDs.contains(engineId)) {
                    if (!action(engineId))
                        return false
                }
            }
            return true
        }
        targetIDs.forall(action)
    }

    override def toString: String = s"BroadcastPacketCoordinates(${path.mkString("/")}, $senderID, $discardTargets, $targetIDs)"

    def listDiscarded(alreadyConnected: Seq[String]): Seq[String] = {
        if (discardTargets)
            targetIDs
        else alreadyConnected.filterNot(targetIDs.contains)
    }

    def getDedicated(target: String): DedicatedPacketCoordinates = {
        if (targetIDs.contains(target) == discardTargets) {
            throw new IllegalArgumentException(s"These coordinates does not target $target (discardTargets = $discardTargets).")
        }

        DedicatedPacketCoordinates(path, target, senderID)
    }
}

