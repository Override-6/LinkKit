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

package fr.linkit.engine.gnom.persistence.pool

import fr.linkit.api.gnom.persistence.obj.ReferencedNetworkObject
import fr.linkit.api.gnom.reference.NetworkObjectReference

class SimpleReferencedNetworkObject(override val locationIdx: Int,
                                    override val location: NetworkObjectReference,
                                    override val value: AnyRef) extends ReferencedNetworkObject {

    override def equals(obj: Any): Boolean = {
        obj match {
            case ref: AnyRef => (ref eq value) || (ref eq this)
        }
    }
}