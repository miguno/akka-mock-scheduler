package com.miguno.akka.testing

import akka.actor.Cancellable
import org.scalatest.{FunSpec, FeatureSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration._

class CancellableTaskSpec extends FeatureSpec with Matchers with GivenWhenThen {

  private val AnyTask = {
    val anyDelay = 10.millis
    val anyId = 0
    val anyRunnable = new Runnable {
      override def run(): Unit = {}
    }
    val anyInterval: Option[FiniteDuration] = None
    Task(anyDelay, anyId, anyRunnable, anyInterval)
  }

  info("A wrapper for tasks that supports cancellation of the task")

  feature("CancellableTask supports Ordered trait") (pending)

  feature("CancellableTask supports Cancellable trait") {

      scenario("it should return false when cancelled") {
        Given("an instance")
        val cancellable: Cancellable = CancellableTask(AnyTask)

        When("I cancel it")
        val result = cancellable.cancel()

        Then("it returns false")
        result should be(false)
      }

      scenario("isCancelled should return false when cancel has not been called yet") {
        Given("an instance")
        val cancellable: Cancellable = CancellableTask(AnyTask)

        When("I ask whether it has been successfully cancelled")
        Then("it returns false")
        cancellable should not be 'cancelled
      }

      scenario("isCancelled should return true when cancel was called already") {
        Given("an instance")
        val cancellable: Cancellable = CancellableTask(AnyTask)
        And("the task was cancelled")
        cancellable.cancel()

        When("I ask whether it has been successfully cancelled")
        Then("it returns true")
        cancellable shouldBe 'cancelled
      }

    }

}