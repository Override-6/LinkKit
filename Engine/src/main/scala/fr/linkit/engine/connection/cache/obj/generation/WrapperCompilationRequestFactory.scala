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

package fr.linkit.engine.connection.cache.obj.generation

import fr.linkit.api.connection.cache.obj.SynchronizedObject
import fr.linkit.api.connection.cache.obj.generation.GeneratedClassLoader
import fr.linkit.api.local.generation.PuppetClassDescription
import fr.linkit.api.local.generation.compilation.CompilationRequest
import fr.linkit.engine.connection.cache.obj.generation.WrapperCompilationRequestFactory.DefaultClassBlueprint
import fr.linkit.engine.connection.cache.obj.generation.bp.ScalaWrapperClassBlueprint
import fr.linkit.engine.connection.cache.obj.generation.rectifier.ClassRectifier
import fr.linkit.engine.local.generation.compilation.factories.ClassCompilationRequestFactory
import fr.linkit.engine.local.mapping.ClassMappings

import java.io.File
import java.nio.file.Files

class WrapperCompilationRequestFactory extends ClassCompilationRequestFactory[PuppetClassDescription[_], SynchronizedObject[_]](DefaultClassBlueprint) {

    override def loadClass(req: CompilationRequest[Seq[Class[_ <: SynchronizedObject[_]]]], context: PuppetClassDescription[_], className: String, loader: GeneratedClassLoader): Class[_] = {
        val (byteCode, wrapperClass) = new ClassRectifier(context, className, loader, context.clazz).rectifiedClass
        val wrapperClassFile         = req.classDir.resolve(className.replace(".", File.separator) + ".class")
        Files.write(wrapperClassFile, byteCode)
        SyncObjectInstantiationHelper.prepareClass(wrapperClass)
        ClassMappings.putClass(wrapperClass)
        wrapperClass
    }
}

object WrapperCompilationRequestFactory {

    private val DefaultClassBlueprint = new ScalaWrapperClassBlueprint(getClass.getResourceAsStream("/generation/puppet_wrapper_blueprint.scbp"))
}
