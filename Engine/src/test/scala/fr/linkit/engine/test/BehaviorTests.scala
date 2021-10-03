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

package fr.linkit.engine.test

import fr.linkit.engine.gnom.cache.sync.invokation.SimpleRMIRulesAgreementBuilder
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{Assertions, Test, TestInstance}

@TestInstance(Lifecycle.PER_CLASS)
class BehaviorTests {

    @Test
    def testAgreement(): Unit = {
        val builder = new SimpleRMIRulesAgreementBuilder("Johny", "Johny")
        builder.discardAll()
                .acceptOwner()
        val agreement = builder.result
        Assertions.assertTrue(agreement.mayCallSuper)
    }

}
