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

package fr.linkit.api.gnom.cache.sync.contract

/**
 * Contains all the information required to create a mirroring class.
 * @param stubClasses the classes that will be extended by the generated sync class for the mirroring objects.
 *                    first index is the super class, other indexes are for the interfaces.
 * */
case class MirroringInfo(stubClasses: Array[Class[_]])

