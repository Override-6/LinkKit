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

package fr.linkit.api.connection.cache.obj;

import fr.linkit.api.connection.cache.obj.behavior.ObjectBehavior;
import fr.linkit.api.connection.cache.obj.behavior.ObjectBehaviorStore;
import fr.linkit.api.connection.cache.obj.instantiation.SyncInstanceInstantiator;
import fr.linkit.api.connection.cache.obj.tree.SyncNodeLocation;
import fr.linkit.api.connection.cache.obj.invokation.InvocationChoreographer;
import fr.linkit.api.connection.cache.obj.invokation.remote.Puppeteer;
import fr.linkit.api.connection.cache.obj.tree.SyncNode;
import fr.linkit.api.connection.cache.traffic.content.ObjectPresence;

import java.io.Serializable;

/**
 * This interface depicts a synchronized object. <br>
 * SynchronizedObject classes are dynamically generated and extends the class {@link T} <br>
 * Handwritten classes may not implement this interface.
 *
 * @see fr.linkit.api.connection.cache.obj.generation.SyncClassCenter
 * @see SyncInstanceInstantiator
 */
public interface SynchronizedObject<T> extends ObjectPresence<SyncNodeLocation>, Serializable {

    /**
     * Initialize the puppeteer of the synchronized object.
     *
     * @throws SyncObjectAlreadyInitialisedException if this object is already initialized.
     */
    void initialize(SyncNode<T> node); //TODO pass in internal

    /**
     * @return The used {@link Puppeteer} of this object.
     * @see Puppeteer
     */
    Puppeteer<T> getPuppeteer();

    /**
     * @return the behavior of this object
     * @see ObjectBehavior
     */
    ObjectBehavior<T> getBehavior();

    /**
     * @return the invocation choreographer of this object
     * @see InvocationChoreographer
     */
    InvocationChoreographer getChoreographer();

    ObjectBehaviorStore getStore();

    /**
     * Note: a synchronized object is always initialized if it was retrieved normally.
     *
     * @return true if the object is initialized.
     */
    boolean isInitialized();

    /**
     * @return true if the engine that created this synchronized object is the current engine.
     */
    boolean isOwnedByCurrent();

    /**
     * @return self, as a SynchronizedObject implementation must extend T, the return value must be this object.
     * (this has been done to avoid useless casts)
     */
    T asWrapped();

    /**
     * @return this class's super class.
     */
    Class<T> getSuperClass();

}
