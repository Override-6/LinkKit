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

package fr.linkit.engine.test

import fr.linkit.api.application.ApplicationContext
import fr.linkit.api.application.connection.ConnectionContext

/*@RunWith(classOf[JUnitPlatform])*/
//@TestInstance(Lifecycle.PER_CLASS)
abstract class EngineTests {

}

object EngineTests {

    var application   : ApplicationContext = _
    var testConnection: ConnectionContext  = _

}
