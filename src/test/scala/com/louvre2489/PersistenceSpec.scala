package com.louvre2489

import java.io.File

import com.typesafe.config._

import scala.util._
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.util.Timeout
import org.apache.commons.io.FileUtils
import org.scalatest._
import org.scalatest.wordspec.AsyncWordSpecLike

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class PersistenceSpec(system: ActorSystem[Nothing])
    extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
    with AsyncWordSpecLike
    with BeforeAndAfterEach
    with LogCapturing
    with PersistenceCleanup {

//  private val eventSourcedTestKit =
//    EventSourcedBehaviorTestKit[Calculator.Command, Calculator.Event, Calculator.CalculationResult](
//      system,
//      Calculator("test"))

  override protected def beforeAll() = deleteStorageLocations("BEFORE ALL")

  override protected def afterAll() = {
    deleteStorageLocations("AFTER ALL")
    system.terminate()
  }

  def killActors(as: ActorRef[Calculator.Command])(implicit timeout: Timeout, scheduler: Scheduler) = {

    val f = as.ask(replyTo => Calculator.Stop(replyTo))
    Await.result(f, Duration.Inf)
  }
}

trait PersistenceCleanup {
  val config = ConfigFactory.load()

  def system: ActorSystem[Nothing]

  private val storageLocations = List("akka.persistence.journal.leveldb.dir",
                                      "akka.persistence.journal.leveldb-shared.store.dir",
                                      "akka.persistence.snapshot-store.local.dir").map { s =>
    new File(config.getString(s))
  }

  def deleteStorageLocations(msg: String): Unit = {
    println(msg)
    storageLocations.foreach { dir =>
      if (dir.exists())
        Try(FileUtils.deleteDirectory(dir))
    }
  }
}
