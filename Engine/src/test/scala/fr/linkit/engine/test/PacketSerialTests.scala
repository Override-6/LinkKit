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

import fr.linkit.engine.connection.cache.repo.CloudObjectRepository.PuppetProfile
import fr.linkit.engine.connection.packet.fundamental.RefPacket.ObjectPacket
import fr.linkit.engine.connection.packet.serialization.DefaultSerializer
import fr.linkit.engine.connection.packet.traffic.channel.request.RequestPacket
import fr.linkit.engine.local.LinkitApplication
import fr.linkit.engine.local.mapping.ClassMapEngine
import fr.linkit.engine.local.system.fsa.LocalFileSystemAdapters
import fr.linkit.engine.local.utils.ScalaUtils
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{Assertions, BeforeAll, Test, TestInstance}

@TestInstance(Lifecycle.PER_CLASS)
class PacketSerialTests {

    private val testedPacket                 = RequestPacket(7, Array(ObjectPacket(PuppetProfile(Array(8, 5, 3, 4), PlayerTest(5, "TestServer1", "jimmy", 89, 12), "TestServer1"))))
    private var testPacketBytes: Array[Byte] = _

    @BeforeAll
    def init(): Unit = {
        LinkitApplication.mapEnvironment(LocalFileSystemAdapters.Nio, Seq(getClass))
    }



    @Test
    def serialize(): Unit = {
        testPacketBytes = new DefaultSerializer().serialize(testedPacket, true)
        println(s"Serialized testedPacket : ${ScalaUtils.toPresentableString(testPacketBytes)}")
    }

    @Test
    def deserialize(): Unit = {
        val packet = Assertions.assertInstanceOf(classOf[RequestPacket], new DefaultSerializer().deserialize(testPacketBytes))
    }

}
