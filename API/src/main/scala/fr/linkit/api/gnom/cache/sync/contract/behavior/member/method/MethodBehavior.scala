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

package fr.linkit.api.gnom.cache.sync.contract.behavior.member.method

import fr.linkit.api.gnom.cache.sync.contract.behavior.RMIRulesAgreement
import fr.linkit.api.gnom.cache.sync.contract.behavior.member.MemberBehavior
import org.jetbrains.annotations.Nullable

trait MethodBehavior extends MemberBehavior {

    @Nullable("when isActivated = false")
    val agreement: RMIRulesAgreement

    val isHidden                  : Boolean
    val forceLocalInnerInvocations: Boolean
    // Suspended -> val defaultReturnValue        : Any
    val parameterBehaviors        : Array[ParameterBehavior[Any]]
    val returnValueBehavior       : ReturnValueBehavior[Any]

}
