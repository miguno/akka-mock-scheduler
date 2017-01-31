package com.miguno.akka.testing

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration._

class VirtualTimeSpec extends FunSpec with Matchers with GivenWhenThen {

  describe("VirtualTime") {

    it("should start at time zero") {
      Given("no time")

      When("I create a time")
      val time = new VirtualTime

      Then("its elapsed time should be zero")
      time.elapsed should be(0.millis)
    }

    it("should track elapsed time") {
      Given("a time")
      val time = new VirtualTime

      When("I advance the time")
      time.advance(10.millis)
      time.advance(20.millis)

      Then("the elapsed time should be correct")
      time.elapsed should be(30.millis)
    }

    it("should accept a step defined as a Long that represents the number of milliseconds") {
      Given("a time")
      val time = new VirtualTime

      When("I advance the time by a Long value of 1234")
      time.advance(1234)

      Then("the elapsed time should be 1234 milliseconds")
      time.elapsed should be(1234.millis)
    }

    it("should have a meaningful string representation") {
      Given("a time")
      val time = new VirtualTime
      val anyStep = 123.seconds
      time.advance(anyStep)

      When("I request its string representation")
      val s = time.toString

      Then("the representation should include the elapsed time in milliseconds")
      s should be(s"VirtualTime(${anyStep.toMillis})")

    }

    it("should enforce a minimum advancement of 1 milliseconds") {
      Given("a time")
      val time = new VirtualTime

      Then("it will throw an exception if time is advanced by less than 1 millisecond")
      intercept[IllegalArgumentException] {
        time.advance(0.millis)
      }
    }

  }

}
