package com.miguno.akka.testing

import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration._

class TaskSpec extends FeatureSpec with Matchers with GivenWhenThen {

  private val AnyTask: Task = {
    val anyDelay = 10.millis
    val anyId = 0
    val anyRunnable = new Runnable {
      override def run(): Unit = {}
    }
    val anyInterval: Option[FiniteDuration] = None
    Task(anyDelay, anyId, anyRunnable, anyInterval)
  }

  info("A task that can be scheduled and run")

  feature("Task supports Ordered trait") {

    scenario("Two tasks with identical delays and ids") {
      Given("a task")
      val first = AnyTask
      And("another task that has the same delay and id")
      val second = {
        val runnable = new Runnable {
          override def run(): Unit = {}
        }
        val interval = Option(1234.millis)
        Task(first.delay, first.id, runnable, interval)
      }

      When("I compare the two tasks")
      val firstToSecond = first.compare(second)
      val secondToFirst = second.compare(first)

      Then("the result is zero")
      firstToSecond should (be(secondToFirst) and be(0))
    }

  }

}