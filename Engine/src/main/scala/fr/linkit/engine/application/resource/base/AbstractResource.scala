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

package fr.linkit.engine.application.resource.base

import fr.linkit.api.application.resource.ResourcesMaintainer
import fr.linkit.api.application.resource.external.{Resource, ResourceFolder}
import fr.linkit.api.internal.system.Versions
import fr.linkit.api.internal.system.fsa.FileAdapter
import fr.linkit.engine.internal.system.{DynamicVersions, StaticVersions}
import org.jetbrains.annotations.Nullable

abstract class AbstractResource(@Nullable parent: ResourceFolder, adapter: FileAdapter) extends Resource {

    override     val name: String = adapter.getName
    private lazy val lastModified = getMaintainer.getLastModified(name)

    protected def getMaintainer: ResourcesMaintainer

    override def getLocation: String = {
        if (parent == null)
            return "/"
        parent.getLocation + '/' + name
    }

    override def getLastModified: Versions = lastModified

    override def getParent: ResourceFolder = parent

    override def getRoot: ResourceFolder = {
        var lastParent: ResourceFolder = getParent
        while (lastParent != null)
            lastParent = lastParent.getParent
        lastParent
    }

    override def getAdapter: FileAdapter = adapter

    override def getChecksum: Long

    override def getLastChecksum: Long = getMaintainer.getLastChecksum(name)

    override def markAsModifiedByCurrentApp(): Unit = {
        lastModified match {
            case dynamic: DynamicVersions => dynamic.setAll(StaticVersions.currentVersions)
            case _                        =>
            //If the Versions implementation isn't dynamic, this means that we may not
            //Update the versions.
        }
        if (getParent != null)
            getParent.markAsModifiedByCurrentApp()
    }

}
