package com.louvre2489

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.louvre2489.Calculator._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}

object Main extends App {

  val system = ActorSystem(Calculator("sample"), "sample")

  implicit val ec        = system.executionContext
  implicit val timeout   = requestTimeout
  implicit val scheduler = system.scheduler

  system ! Add(1)
  system ! Add(2)
  system ! Add(3)
  system ! Add(4)
  system ! Add(5)
  system ! PrintResult
  system.ask(replyTo => GetResult(replyTo)).onComplete {
    case Success(value) =>
      println(s"answer for asking: $value")
    case Failure(exception) =>
      println(exception)
      throw exception
  }

  system ! Add(6)
  system ! Subtract(7)
  system.ask(replyTo => GetResult(replyTo)).onComplete {
    case Success(value) =>
      println(s"answer for asking: $value")
    case Failure(exception) =>
      println(exception)
      throw exception
  }

  system ! Clear
  system.ask(replyTo => GetResult(replyTo)).onComplete {
    case Success(value) =>
      println(s"answer for asking: $value")
    case Failure(exception) =>
      println(exception)
      throw exception
  }

  val queries =
    PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

  val src: Source[EventEnvelope, NotUsed] =
    queries.currentEventsByPersistenceId("sample")

  val events: Source[Calculator.Event, NotUsed] =
    src.map(_.event.asInstanceOf[Calculator.Event])

  implicit val sss = system
  val res: Future[Seq[Calculator.Event]] = events.runWith(Sink.seq)

  Thread.sleep(3000L)
  println( Await.result(res, Duration.Inf))

  private def requestTimeout: Timeout = {
    val d = Duration("3 s")
    FiniteDuration(d.length, d.unit)
  }
}
