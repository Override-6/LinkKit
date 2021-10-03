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

package fr.linkit.engine.internal.system.fsa.io

import fr.linkit.api.internal.system.fsa.{FileAdapter, FileSystemAdapter}

import java.io._
import java.net.URI

case class IOFileAdapter private[io](file: File, @transient fsa: IOFileSystemAdapter) extends FileAdapter {

    def this(other: IOFileAdapter) = {
        this(other.file, other.fsa)
    }

    override def getFSAdapter: FileSystemAdapter = fsa

    override def getPath: String = file.getPath

    override def getAbsolutePath: String = file.getAbsolutePath

    override def getSize: Long = file.length()

    override def getParent(level: Int): FileAdapter = {
        var parent = file
        for (_ <- 0 to level)
            parent = parent.getParentFile
        fsa.getAdapter(parent.toString)
    }

    override def getName: String = file.getName

    override def getContentString: String = {
        val in  = newInputStream()
        val str = new String(in.readAllBytes())
        in.close()
        str
    }

    override def toUri: URI = file.toURI

    override def resolveSibling(path: String): FileAdapter = fsa.getAdapter(getPath + File.separatorChar + path) //FIXME (bad implementation)

    override def resolveSiblings(path: FileAdapter): FileAdapter = resolveSibling(path.getPath)

    override def isDirectory: Boolean = file.isDirectory

    override def isReadable: Boolean = file.canRead

    override def isWritable: Boolean = file.canWrite

    override def delete(): Boolean = file.delete()

    override def exists: Boolean = file.exists()

    override def notExists: Boolean = !exists

    override def createAsFile(): this.type = {
        if (notExists) {
            file.createNewFile()
        }
        this
    }

    override def createAsFolder(): this.type = {
        if (notExists) {
            file.mkdirs()
        }
        this
    }

    override def newInputStream(): InputStream = new FileInputStream(file)

    override def newOutputStream(append: Boolean = false): OutputStream = new FileOutputStream(file)

    override def write(bytes: Array[Byte], append: Boolean = false): this.type = {
        val out = newOutputStream(append)
        try out.write(bytes)
        finally {
            out.flush()
            out.close()
        }
        this
    }

    override def isPresentOnDisk: Boolean = exists
}
