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

package fr.linkit.api.connection.cache.obj

import fr.linkit.api.connection.cache.obj.behavior.{ObjectBehavior, ObjectBehaviorStore}
import fr.linkit.api.connection.cache.obj.invokation.InvocationChoreographer
import fr.linkit.api.connection.cache.obj.invokation.remote.Puppeteer
import fr.linkit.api.connection.cache.obj.tree.{SyncNode, SyncNodeLocation}
import fr.linkit.api.connection.reference.presence.ObjectPresence

import java.io.Serializable

/**
 * This interface depicts a synchronized object. <br>
 * SynchronizedObject classes are dynamically generated and extends the class [[T]] <br>
 * Handwritten classes may not implement this interface.
 *
 * @see fr.linkit.api.connection.cache.obj.generation.SyncClassCenter
 * @see SyncInstanceInstantiator
 */
trait SynchronizedObject[T <: AnyRef] extends ObjectPresence[SyncNodeLocation] with Serializable {

    /**
     * Initialize the puppeteer of the synchronized object.
     *
     * @throws SyncObjectAlreadyInitialisedException if this object is already initialized.
     */
    def initialize(node: SyncNode[T]): Unit //TODO pass in internal

    /**
     * @return The used [[Puppeteer]] of this object.
     * @see Puppeteer
     */
    def getPuppeteer: Puppeteer[T]

    /**
     * @return the behavior of this object
     * @see ObjectBehavior
     */
    def getBehavior: ObjectBehavior[T]

    /**
     * @return the invocation choreographer of this object
     * @see InvocationChoreographer
     */
    def getChoreographer: InvocationChoreographer

    def getStore: ObjectBehaviorStore

    /**
     * Note: a synchronized object is always initialized if it was retrieved normally.
     *
     * @return true if the object is initialized.
     */
    def isInitialized: Boolean

    /**
     * @return true if the engine that created this synchronized object is the current engine.
     */
    def isOwnedByCurrent: Boolean

    /**
     * @return this class's super class.
     */
    def getSuperClass: Class[T]
}
