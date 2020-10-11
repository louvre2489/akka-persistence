package com.louvre2489

import java.io.File

import com.typesafe.config._

import scala.util._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import org.apache.commons.io.FileUtils
import org.scalatest._
import org.scalatest.wordspec.AnyWordSpecLike

abstract class PersistenceSpec
    extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
    with AnyWordSpecLike
    with BeforeAndAfterEach
    with LogCapturing
    with PersistenceCleanup {

  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[Calculator.Command, Calculator.Event, Calculator.CalculationResult](
      system,
      Calculator("test"))

//  def this(name: String, config: Config) = this(ActorSystem(name, config))
  override protected def beforeAll() = deleteStorageLocations()

  override protected def afterAll() = {
    deleteStorageLocations()
    ActorTestKit.shutdown(system)
  }

  def killActors(actor: ActorSystem[Calculator.Command]) = {
    ActorTestKit.shutdown(system)
  }
}

trait PersistenceCleanup {
  val config = ConfigFactory.load()

  def system: ActorSystem[Nothing]

  val storageLocations = List("akka.persistence.journal.leveldb.dir",
                              "akka.persistence.journal.leveldb-shared.store.dir",
                              "akka.persistence.snapshot-store.local.dir").map { s =>
    new File(config.getString(s))
  }

  def deleteStorageLocations(): Unit = {
    storageLocations.foreach { dir =>
      Try(FileUtils.deleteDirectory(dir))
      println(dir.getAbsolutePath)
    }
  }
}
