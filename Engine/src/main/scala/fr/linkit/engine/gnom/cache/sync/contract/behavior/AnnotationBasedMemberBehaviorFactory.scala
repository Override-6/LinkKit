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

package fr.linkit.engine.gnom.cache.sync.contract.behavior

import fr.linkit.api.gnom.cache.sync.contract.behavior.RMIRulesAgreementBuilder
import fr.linkit.api.gnom.cache.sync.contract.behavior.annotation._
import fr.linkit.api.gnom.cache.sync.contract.behavior.member._
import fr.linkit.api.gnom.cache.sync.contract.behavior.member.field.FieldBehavior
import fr.linkit.api.gnom.cache.sync.contract.behavior.member.method.ParameterBehavior
import fr.linkit.api.gnom.cache.sync.contract.description._
import fr.linkit.api.internal.concurrency.Procrastinator
import fr.linkit.engine.gnom.cache.sync.contract.behavior.member.{MethodParameterBehavior, MethodReturnValueBehavior, SyncFieldBehavior, SyncMethodBehavior}

import java.lang.reflect.{Method, Parameter}

object AnnotationBasedMemberBehaviorFactory extends MemberBehaviorFactory {

    def getParamBehaviors(method: Method): Array[ParameterBehavior[Any]] = {
        val params = method.getParameters
                .map(genParameterBehavior)
        if (params.exists(_.isActivated))
            params
        //no behavior specified with annotation,
        //the invocation part of the system makes some optimisations for methods behaviors with empty parameter behaviors.
        else Array.empty
    }

    def genParameterBehavior(param: Parameter): ParameterBehavior[Any] = {
        val isSynchronized = param.isAnnotationPresent(classOf[Synchronized])
        new MethodParameterBehavior[Any](isSynchronized)
    }

    override def genMethodBehavior(procrastinator: Option[Procrastinator], agreementBuilder: RMIRulesAgreementBuilder, desc: MethodDescription): SyncMethodBehavior = {
        val javaMethod       = desc.javaMethod
        val controlOpt       = Option(javaMethod.getAnnotation(classOf[MethodControl]))
        val control          = controlOpt.getOrElse(DefaultMethodControl)
        val isActivated      = controlOpt.isDefined
        val paramBehaviors   = getParamBehaviors(desc.javaMethod)
        val rule             = control.value()
        val isHidden         = control.hide
        val innerInvocations = control.forceLocalInnerInvocations()
        val returnValueBhv   = new MethodReturnValueBehavior[Any](control.synchronizeReturnValue())

        rule.apply(agreementBuilder)
        val agreement = agreementBuilder.result
        SyncMethodBehavior(
            isActivated, paramBehaviors, returnValueBhv, isHidden,
            innerInvocations, agreement
        )
    }

    override def genFieldBehavior(desc: FieldDescription): FieldBehavior[Any] = {
        val control        = Option(desc.javaField.getAnnotation(classOf[Synchronized]))
        val isSynchronized = control.isDefined
        SyncFieldBehavior(desc, isSynchronized, null)
    }

    val DefaultMethodControl: MethodControl = {
        new MethodControl {
            override def value(): BasicInvocationRule = BasicInvocationRule.ONLY_CURRENT

            override def synchronizeReturnValue(): Boolean = false

            override def hide(): Boolean = false

            override def forceLocalInnerInvocations(): Boolean = false

            override def annotationType(): Class[_ <: java.lang.annotation.Annotation] = getClass
        }
    }
}