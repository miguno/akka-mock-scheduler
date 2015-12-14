package com.miguno.akka.testing

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}
import org.scalatest.mock.MockitoSugar
import scala.concurrent.duration._

class TaskSpec extends FunSpec with Matchers with GivenWhenThen with MockitoSugar {

  val runnable = new Runnable() { def run(): Unit = {} }
  
  describe("Task") {

    it("a task with a larger delay is greater than one with a smaller delay") {
      Given("an instance")
      val c1 = Task(10.millis, 0, runnable, None)
      

      When("compared to another later instance")
      val c2 = Task(11.millis, 0, runnable, None)

      Then("it returns greater than zero")
      c1.compare(c2) should be > 0
    }
    
    it("a task with a smaller delay is less than one with a larger delay") {
      Given("an instance")
      val c1 = Task(11.millis, 0, runnable, None)
      

      When("compared to another earlier instance")
      val c2 = Task(10.millis, 0, runnable, None)

      Then("it returns less than zero")
      c1.compare(c2) should be < 0
    }
    
    it("for two tasks with equal delays, one with a smaller id is less than anotehr") {
      Given("an instance")
      val c1 = Task(10.millis, 1, runnable, None)
      

      When("compared to another with a lesser id")
      val c2 = Task(10.millis, 0, runnable, None)

      Then("it returns less than zero")
      c1.compare(c2) should be < 0
    }
    
    it("for two tasks with equal delays, one with a greater id is greater than anotehr") {
      Given("an instance")
      val c1 = Task(10.millis, 0, runnable, None)
      

      When("compared to another with a greater id")
      val c2 = Task(10.millis, 1, runnable, None)

      Then("it returns less greater than zero")
      c1.compare(c2) should be > 0
    }
    
    it("tasks with equal delays and ids are equal") {
      Given("an instance")
      val c1 = Task(10.millis, 0, runnable, None)
      

      When("compared to another with the same delay and id")
      val c2 = Task(10.millis, 0, runnable, None)

      Then("it returns true")
      c1.compare(c2) should be(0)
    }
  }

}
