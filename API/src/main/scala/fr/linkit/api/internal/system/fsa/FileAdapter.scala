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

package fr.linkit.api.internal.system.fsa

import java.io.{InputStream, OutputStream}
import java.net.URI

//TODO implements more methods from java.nio.file.Path
trait FileAdapter extends Serializable {

    override def toString: String = getAbsolutePath

    override def equals(obj: Any): Boolean = obj != null && obj.getClass == getClass && obj.toString == toString

    def getFSAdapter: FileSystemAdapter

    def isPresentOnDisk: Boolean

    def getPath: String

    def getAbsolutePath: String

    def getName: String

    def getContentString: String

    def getLines: Array[String] = getContentString.split("\n")

    def getSize: Long

    def getParent: FileAdapter = getParent(1)

    def getParent(level: Int): FileAdapter

    def resolveSibling(path: String): FileAdapter

    def resolveSiblings(path: FileAdapter): FileAdapter

    def toUri: URI

    def isDirectory: Boolean

    def isReadable: Boolean

    def isWritable: Boolean

    def delete(): Boolean

    def exists: Boolean

    def notExists: Boolean

    def createAsFile(): this.type

    def createAsFolder(): this.type

    def newInputStream(): InputStream

    def newOutputStream(append: Boolean = false): OutputStream

    def write(bytes: Array[Byte], append: Boolean = false): this.type

}
