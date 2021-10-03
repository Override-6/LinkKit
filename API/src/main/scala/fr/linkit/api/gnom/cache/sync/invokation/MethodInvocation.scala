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

package fr.linkit.api.gnom.cache.sync.invokation

import fr.linkit.api.gnom.cache.sync.SynchronizedObject
import fr.linkit.api.gnom.cache.sync.behavior.member.method.MethodBehavior

/**
 * The invocation information for a synchronized object's method.
 *
 * @tparam R the return type of the method invoked
 * */
trait MethodInvocation[R] {

    /**
     * The synchronized object on which the method is called.
     * */
    val synchronizedObject: SynchronizedObject[_]

    /**
     * The method's identifier.
     * */
    val methodID: Int

    /**
     * The method's behavior
     */
    val methodBehavior: MethodBehavior

    val debug: Boolean = true

}
