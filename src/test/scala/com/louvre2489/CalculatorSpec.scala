package com.louvre2489

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.util.Timeout
import com.louvre2489.Calculator._

import scala.concurrent.duration.{Duration, FiniteDuration}

class CalculatorSpec extends PersistenceSpec(ActorSystem(Calculator("test"), "CalculatorSpec")) with PersistenceCleanup {

  "The Calculator" should {
    "recover last known result after crash" in {

      val calc = Calculator("test")
      val actor = system.systemActorOf(calc, "test")

      implicit val ec        = system.executionContext
      implicit val timeout   = requestTimeout
      implicit val scheduler = system.scheduler

      actor ! Calculator.Add(1)
      actor ! Calculator.Add(2)
      actor
        .ask(replyTo => Calculator.GetResult(replyTo))
        .map(result => assert(result == 3))

      // Actorを停止させる
      killActors(actor)

      // 別Actorを生成する
      // この時にリカバリが発生していることがログからわかる
      //   RecoveryCompleted!![CalculationResult(3.0)]
      val reCreatedActor = system.systemActorOf(calc, "test2")
      reCreatedActor ! Calculator.Add(10)
      reCreatedActor
        .ask(replyTo => Calculator.GetResult(replyTo))
        .map(result => assert(result == 13))
    }

    "recover at last Subtracted" in {

      val calc = Calculator("test")
      val actor = system.systemActorOf(calc, "test")

      implicit val ec        = system.executionContext
      implicit val timeout   = requestTimeout
      implicit val scheduler = system.scheduler

      actor ! Calculator.Add(10)

      // Actorを停止させる
      killActors(actor)

      val reCreatedActor = system.systemActorOf(calc, "test2")
      reCreatedActor ! Subtract(7)
      reCreatedActor ! Calculator.Add(1)
      reCreatedActor
        .ask(replyTo => Calculator.GetResult(replyTo))
        .map(result => assert(result == 4))
    }
  }

  private def requestTimeout: Timeout = {
    val d = Duration("3 s")
    FiniteDuration(d.length, d.unit)
  }
}
