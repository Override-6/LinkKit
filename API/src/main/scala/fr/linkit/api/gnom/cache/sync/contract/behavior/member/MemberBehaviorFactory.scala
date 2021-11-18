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

package fr.linkit.api.gnom.cache.sync.contract.behavior.member

import fr.linkit.api.gnom.cache.sync.contract.behavior.RMIRulesAgreementBuilder
import fr.linkit.api.gnom.cache.sync.contract.behavior.member.field.FieldBehavior
import fr.linkit.api.gnom.cache.sync.contract.behavior.member.method.MethodBehavior
import fr.linkit.api.gnom.cache.sync.contract.description.{FieldDescription, MethodDescription}
import fr.linkit.api.internal.concurrency.Procrastinator

trait MemberBehaviorFactory {

    def genMethodBehavior(callProcrastinator: Option[Procrastinator],
                          agreementBuilder: RMIRulesAgreementBuilder,
                          desc: MethodDescription): MethodBehavior

    def genFieldBehavior(desc: FieldDescription): FieldBehavior[Any]

}
