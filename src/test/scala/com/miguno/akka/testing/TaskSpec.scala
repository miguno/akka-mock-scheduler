package com.miguno.akka.testing

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration._

class TaskSpec extends FunSpec with Matchers with GivenWhenThen with MockitoSugar {

  val runnable = new Runnable() {
    def run(): Unit = {}
  }

  private val anyId = 0
  private val anyDelay = 10.millis

  describe("Task") {

    it("a task is smaller than another task with a larger delay") {
      Given("an instance")
      val c1 = Task(anyDelay, anyId, runnable, None)

      When("compared to a second instance that runs later")
      val c2 = Task(anyDelay + 1.millis, anyId, runnable, None)

      Then("the first instance is greater than the second")
      c1.compare(c2) should be > 0
      And("the second instance is smaller than the first")
      c2.compare(c1) should be < 0
    }

    it("for two tasks with equal delays, the one with the smaller id is less than the other") {
      Given("an instance")
      val c1 = Task(anyDelay, anyId, runnable, None)

      When("compared to second instance with a larger id")
      val c2 = Task(anyDelay, anyId + 1, runnable, None)

      Then("the first instance is greater than the second")
      c1.compare(c2) should be > 0
      And("the second instance is smaller than the first")
      c2.compare(c1) should be < 0
    }

    it("tasks with equal delays and equal ids are equal") {
      Given("an instance")
      val c1 = Task(anyDelay, anyId, runnable, None)

      When("compared to another with the same delay and id")
      val c2 = Task(anyDelay, anyId, runnable, None)

      Then("it returns true")
      c1.compare(c2) should be(0)
    }

  }

}