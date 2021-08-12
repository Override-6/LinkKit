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

package fr.linkit.engine.local.script

import fr.linkit.api.local.ApplicationContext
import fr.linkit.api.local.generation.cbp.ClassBlueprint
import fr.linkit.api.local.generation.compilation.CompilerCenter
import fr.linkit.engine.local.LinkitApplication
import fr.linkit.engine.local.generation.compilation.factories.ClassCompilationRequestFactory
import fr.linkit.engine.local.generation.compilation.resource.ClassFolderResource
import fr.linkit.engine.local.resource.external.LocalResourceFolder

import java.net.URL
import scala.reflect.ClassTag

object ScriptExecutor {

    val DefaultScriptHandler = new SimpleScriptHandler

    def newScalaScript(scriptCode: String, scriptName: String)(implicit center: CompilerCenter): ScriptFile = {
        newScalaScript[ScriptFile](scriptCode, scriptName)(DefaultScriptHandler, center)
    }

    def newScalaScript[S <: ScriptFile](scriptCode: String, scriptName: String)(implicit scriptHandler: ScriptHandler[S], center: CompilerCenter): S = {
        val clazz = center.processRequest {
            val classLoader = Thread.currentThread().getContextClassLoader
            val blueprint = new ScalaScriptBlueprint(scriptHandler.scriptClassBlueprint).asInstanceOf[ClassBlueprint[ScriptContext]]
            new ClassCompilationRequestFactory[ScriptContext, S](blueprint)
                    .makeRequest(scriptHandler.newScriptContext(scriptCode, scriptName, classLoader))
        }.getResult.get
        scriptHandler.newScript(clazz)
    }

    def newScalaScript[S <: ScriptFile](scriptUrl: URL)(implicit scriptHandler: ScriptHandler[S], center: CompilerCenter): S = {
        val scriptCode = new String(scriptUrl.openStream().readAllBytes())
        val path       = scriptUrl.getPath
        val scriptName = path.drop(path.lastIndexOf('/'))
        newScalaScript[S](scriptCode, scriptName)
    }

    def getOrCreateScript[S <: ScriptFile](scriptUrl: URL, app: ApplicationContext)(implicit scriptHandler: ScriptHandler[S], center: CompilerCenter): S = {
        val path       = scriptUrl.getPath
        val scriptName = path.drop(path.lastIndexOf('/'))
        getScript[S](scriptName, app).getOrElse(newScalaScript(scriptUrl))
    }

    def getOrCreateScript(scriptUrl: URL, app: ApplicationContext)(implicit center: CompilerCenter): ScriptFile = {
        getOrCreateScript[ScriptFile](scriptUrl, app)(DefaultScriptHandler, center)
    }

    def getScript(name: String)(implicit app: ApplicationContext): Option[ScriptFile] = {
        getScript(name, app)(DefaultScriptHandler)
    }

    def getScript[S <: ScriptFile](name: String, app: ApplicationContext)(implicit handler: ScriptHandler[S]): Option[S] = {
        val classTag = ClassTag[ClassFolderResource[ScriptFile]](classOf[ClassFolderResource[ScriptFile]])
        val resources = app.getAppResources
                .getOrOpen[LocalResourceFolder](LinkitApplication.getProperty("compilation.working_dir.classes"))
                .getEntry
                .getOrAttachRepresentation[ClassFolderResource[ScriptFile]](classTag, ClassFolderResource.factory[ScriptFile])
        handler.findScript(name, resources)
    }

}

