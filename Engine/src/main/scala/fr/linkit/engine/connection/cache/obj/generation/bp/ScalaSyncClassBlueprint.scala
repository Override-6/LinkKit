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

package fr.linkit.engine.connection.cache.obj.generation.bp

import fr.linkit.api.connection.cache.obj.description.{MethodDescription, SyncObjectSuperclassDescription}
import fr.linkit.api.local.generation.compilation.access.CompilerType
import fr.linkit.engine.connection.cache.obj.generation.bp.ScalaBlueprintUtilities._
import fr.linkit.engine.local.generation.compilation.access.CommonCompilerTypes
import fr.linkit.engine.local.language.cbp.AbstractClassBlueprint

import java.io.InputStream

class ScalaClassBlueprint(in: InputStream) extends AbstractClassBlueprint[SyncObjectSuperclassDescription[_]](in) {

    override val compilerType: CompilerType = CommonCompilerTypes.Scalac

    override val rootScope: RootValueScope = new RootValueScope {
        bindValue("WrappedClassSimpleName" ~> (_.clazz.getSimpleName))
        bindValue("WrappedClassName" ~> (_.clazz.getTypeName.replaceAll("\\$", ".")))
        bindValue("TParamsIn" ~> (getGenericParams(_, _.getName)))
        bindValue("TParamsOut" ~> (getGenericParams(_, _.getName)))
        bindValue("TParamsInBusted" ~> (getGenericParams(_, _ => "_")))

        bindSubScope(new ScalaWrapperMethodBlueprint.ValueScope("INHERITED_METHODS", _, _), (desc, action: MethodDescription => Unit) => {
            desc.listMethods()
                    .toSeq
                    .distinctBy(_.methodId)
                    // .filterNot(m => m.symbol.isSetter || m.symbol.isGetter)
                    .foreach(action)
        })

    }

}
