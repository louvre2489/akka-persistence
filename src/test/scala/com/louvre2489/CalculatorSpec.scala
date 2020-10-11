package com.louvre2489

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import akka.actor.testkit.typed.scaladsl.TestProbe
import org.scalactic.TypeCheckedTripleEquals.convertToCheckingEqualizer
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

class CalculatorSpec extends PersistenceSpec with PersistenceCleanup {

  "The Calculator" should {
    "recover last known result after crash" in {

      val system = ActorSystem(Calculator("test"), "test")

      implicit val ec        = system.executionContext
      implicit val timeout   = requestTimeout
      implicit val scheduler = system.scheduler

      system ! Calculator.Add(1)
      val f = system.ask(replyTo => Calculator.GetResult(replyTo))
      val r = Await.result(f, Duration.Inf)

      r shouldBe 1

//      killActors(system)
    }
  }

  private def requestTimeout: Timeout = {
    val d = Duration("3 s")
    FiniteDuration(d.length, d.unit)
  }
}
