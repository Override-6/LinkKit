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

package fr.linkit.engine.gnom.cache.sync.generation.bp

import fr.linkit.api.gnom.cache.sync.description.{MethodDescription, SyncStructureDescription}
import fr.linkit.api.internal.generation.compilation.access.CompilerType
import fr.linkit.engine.gnom.cache.sync.generation.bp.ScalaBlueprintUtilities._
import fr.linkit.engine.internal.generation.compilation.access.CommonCompilerTypes
import fr.linkit.engine.internal.language.cbp.AbstractClassBlueprint

import java.io.InputStream

class ScalaClassBlueprint(in: InputStream) extends AbstractClassBlueprint[SyncStructureDescription[_]](in) {

    override val compilerType: CompilerType = CommonCompilerTypes.Scalac

    override val rootScope: RootValueScope = new RootValueScope {
        bindValue("WrappedClassSimpleName" ~> (_.clazz.getSimpleName))
        bindValue("WrappedClassName" ~> (_.clazz.getTypeName.replaceAll("\\$", ".")))
        bindValue("TParamsIn" ~> (getGenericParams(_, _.getName)))
        bindValue("TParamsOut" ~> (getGenericParams(_, _.getName)))
        bindValue("TParamsInBusted" ~> (getGenericParams(_, _ => "_")))

        bindSubScope(new ScalaSyncMethodBlueprint.ValueScope("INHERITED_METHODS", _, _), (desc, action: MethodDescription => Unit) => {
            desc.listMethods()
                    .toSeq
                    .distinctBy(_.methodId)
                    // .filterNot(m => m.symbol.isSetter || m.symbol.isGetter)
                    .foreach(action)
        })

    }

}
