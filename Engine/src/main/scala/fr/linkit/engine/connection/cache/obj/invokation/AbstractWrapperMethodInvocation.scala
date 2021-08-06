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

package fr.linkit.engine.connection.cache.obj.invokation

import fr.linkit.api.connection.cache.obj.PuppetWrapper
import fr.linkit.api.connection.cache.obj.behavior.MethodBehavior
import fr.linkit.api.connection.cache.obj.invokation.WrapperMethodInvocation

abstract class AbstractWrapperMethodInvocation[R](override val methodBehavior: MethodBehavior,
                                                  override val wrapper: PuppetWrapper[_]) extends WrapperMethodInvocation[R] {

    override val methodID: Int = methodBehavior.desc.methodId

    override val callerIdentifier: String
    override val methodArguments: Array[Any]
    override def callSuper(): R

}
