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

import fr.linkit.api.gnom.packet.DedicatedPacketCoordinates
import fr.linkit.api.gnom.persistence.context.PersistenceConfig
import fr.linkit.engine.gnom.packet.SimplePacketAttributes
import fr.linkit.engine.gnom.packet.fundamental.RefPacket.ObjectPacket
import fr.linkit.engine.gnom.packet.traffic.channel.request.RequestPacket
import fr.linkit.engine.gnom.persistence.context.{ImmutablePersistenceContext, PersistenceConfigBuilder}
import fr.linkit.engine.gnom.persistence.serializor.DefaultObjectPersistence
import fr.linkit.engine.internal.LinkitApplication
import fr.linkit.engine.internal.system.fsa.LocalFileSystemAdapters
import fr.linkit.engine.internal.utils.ClassMap
import fr.linkit.engine.test.PacketTests.testPacket
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.{BeforeAll, RepeatedTest, Test, TestInstance}

import java.io.File
import scala.collection.mutable.ArrayBuffer

@TestInstance(Lifecycle.PER_CLASS)
class PacketTests {

    @BeforeAll
    def makeMapping(): Unit = {
        LinkitApplication.mapEnvironment(LocalFileSystemAdapters.Nio, Seq(getClass))
    }

    @Test
    def simplePacketTest(): Unit = {
        val f      = new File("/test.txt")
        val config = new PersistenceConfigBuilder() {
            setTConverter[File, String](_.toString)(new File(_))
        }.build(ImmutablePersistenceContext(null, new ClassMap, new ClassMap))
        testPacket(Array(f, f, f), config)
    }

    @RepeatedTest(50)
    @Test
    def perfTests(): Unit = {
        /*val f      = ArrayBuffer(DedicatedPacketCoordinates(Array.empty, "TestServer1", "s1"), SimplePacketAttributes("family" -> "Global Cache", "behavior" -> "GET_OR_OPEN"), RequestPacket(1, Array(IntPacket(3))))
        val obj    = Array[AnyRef](f, f, f)
        val coords = DedicatedPacketCoordinates(Array.empty, "SALAM", "SALAM")
        val buff   = ByteBuffer.allocate(1000)
        val config = new PersistenceConfigBuilder() {
            setTConverter[File, String](_.toString)(new File(_))
        }.build(ImmutablePersistenceContext(null, new ClassMap, new ClassMap))
        val t0     = System.currentTimeMillis()
        for (_ <- 0 to 1000) {
            serializer.serializeObjects(obj, coords, buff)(config)
            buff.position(0)
            serializer.deserializePacket(buff)
                    .forEachObjects(config)(() => _)
            buff.position(0)
        }
        val t1 = System.currentTimeMillis()
        println(t1 - t0)*/
    }

    @Test
    def moreComplexPacketTest(): Unit = {
        testPacket(Array[AnyRef](Array[Int](0, -1598464148)))
    }

    @Test
    def complexPacketTest(): Unit = {
        val packet = ArrayBuffer(DedicatedPacketCoordinates(Array.empty, "TestServer1", "s1"), SimplePacketAttributes("family" -> "Global Cache"), RequestPacket(1, Array(ObjectPacket((-65610, 18965131615655L, 478.215414D, 7854.42145F, true, false)))))
        val config = new PersistenceConfigBuilder() {
            setTConverter[File, String](_.toString)(new File(_))
        }.build(ImmutablePersistenceContext(null, new ClassMap, new ClassMap))
        testPacket(Array(packet), config)
        testPacket(Array(packet), config)
    }

}

object PacketTests {

    def main(args: Array[String]): Unit = {
        val t = new PacketTests()
        for (_ <- 0 to 50)
            t.perfTests()
    }

    private val serializer = new DefaultObjectPersistence(null)

    def testPacket(obj: Array[AnyRef]): Unit = {
        testPacket(obj, new PersistenceConfigBuilder().build(ImmutablePersistenceContext(null, new ClassMap, new ClassMap)))
    }

    def testPacket(obj: Array[AnyRef], config: PersistenceConfig): Unit = {
        ???
        /*println(s"Serializing packets ${obj.mkString("Array(", ", ", ")")}...")
        val buff   = ByteBuffer.allocate(1000)
        serializer.serializeObjects(obj, DedicatedPacketCoordinates(Array.empty, "SALAM", "SALAM"), buff)(config)
        val bytes = buff.array().take(buff.position())
        buff.position(0)
        println(s"bytes = ${ScalaUtils.toPresentableString(bytes)} (size: ${bytes.length})")
        val deserial = serializer.deserializePacket(buff)
        println(s"deserialized coords = ${deserial.getCoordinates}")
        deserial.forEachObjects(config)(packet2 => {
            println(s"deserialized packet = ${packet2}")
        })*/
    }

}
