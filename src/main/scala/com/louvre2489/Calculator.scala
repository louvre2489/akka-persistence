package com.louvre2489

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import akka.persistence.typed.scaladsl.{ Effect, EventSourcedBehavior }
import akka.persistence.typed.{ PersistenceId, RecoveryCompleted }

object Calculator {

  sealed trait Command
  final case class Add(value: Double)                   extends Command
  final case class Subtract(value: Double)              extends Command
  final case class Divide(value: Double)                extends Command
  final case class Multiply(value: Double)              extends Command
  final case class GetResult(replyTo: ActorRef[Double]) extends Command
  case object Clear                                     extends Command
  case object PrintResult                               extends Command
  case object Stop                                      extends Command

  sealed trait Event
  case object Reset                          extends Event
  final case class Added(value: Double)      extends Event
  final case class Subtracted(value: Double) extends Event
  final case class Divided(value: Double)    extends Event
  final case class Multiplied(value: Double) extends Event

  final case class CalculationResult(result: Double = 0) {
    def reset: CalculationResult                   = copy(result = 0)
    def add(value: Double): CalculationResult      = copy(result = this.result + value)
    def subtract(value: Double): CalculationResult = copy(result = this.result - value)
    def divide(value: Double): CalculationResult   = copy(result = this.result / value)
    def multiply(value: Double): CalculationResult = copy(result = this.result * value)
  }

  val commandHandler: (CalculationResult, Command) => Effect[Event, CalculationResult] = { (state, command) =>
    command match {
      case Add(value) =>
        Effect.persist(Added(value))
      case Subtract(value) =>
        Effect.persist(Subtracted(value))
      case Divide(value) =>
        if (value == 0)
          Effect.persist(Divided(value))
        else
          Effect.none
      case Multiply(value) =>
        Effect.persist(Multiplied(value))
      case PrintResult =>
        println(s"the result is: ${state.result}")
        Effect.none
      case GetResult(replyTo) =>
        replyTo ! state.result
        Effect.none
      case Clear =>
        Effect.persist(Reset)
      case Stop =>
        Behaviors.stopped
        Effect.none
    }
  }

  val eventHandler: (CalculationResult, Event) => CalculationResult = { (state, event) =>
    event match {
      case Reset             => state.reset
      case Added(value)      => state.add(value)
      case Subtracted(value) => state.subtract(value)
      case Divided(value)    => state.divide(value)
      case Multiplied(value) => state.multiply(value)
    }
  }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, CalculationResult](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = CalculationResult(),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    ).receiveSignal {
      case (state, RecoveryCompleted) =>
        println(s"RecoveryCompleted!![$state]")
    }

}
