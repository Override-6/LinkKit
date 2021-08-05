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

package fr.linkit.engine.connection.cache.obj.description

import fr.linkit.api.connection.cache.obj.description.{MemberBehaviorFactory, ObjectTreeBehavior, WrapperBehavior}
import fr.linkit.engine.connection.cache.obj.description.annotation.AnnotationBasedMemberBehaviorFactory

import scala.collection.mutable
import scala.reflect.runtime.universe
import scala.reflect.{ClassTag, classTag}

class ObjectTreeDefaultBehavior(override val factory: MemberBehaviorFactory) extends ObjectTreeBehavior {

    private val behaviors = mutable.HashMap.empty[Class[_], WrapperBehavior[_]]

    override def get[B: universe.TypeTag : ClassTag]: WrapperBehavior[B] = {
        getFromAnyClass(classTag[B].runtimeClass)
    }

    override def getFromClass[B](clazz: Class[_]): WrapperBehavior[B] = {
        getFromAnyClass[B](clazz)
    }

    private def getFromAnyClass[B](clazz: Class[_]): WrapperBehavior[B] = {
        behaviors.getOrElseUpdate(clazz, WrapperInstanceBehavior(SimpleClassDescription(clazz), this))
                .asInstanceOf[WrapperBehavior[B]]
    }

    override def put[B](clazz: Class[B], bhv: WrapperBehavior[B]): Unit = behaviors.put(clazz, bhv)

}