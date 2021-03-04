package fr.`override`.linkit.api.concurrency

import java.lang.annotation.{ElementType, Retention, RetentionPolicy, Target}

import scala.annotation.StaticAnnotation

/**
 * Specifies that this method or constructor must be executed by a relay worker thread
 * If the annotated code isn't running in a relay worker thread pool, some problem could occur. <br>
 *
 * @see [[RelayWorkerThreadPool]]
 * @see [[fr.`override`.linkit.api.exception.IllegalThreadException]]
 * */
@Target(Array[ElementType](ElementType.CONSTRUCTOR, ElementType.METHOD))
@Retention(RetentionPolicy.CLASS)
class relayWorkerExecution extends StaticAnnotation {

}
