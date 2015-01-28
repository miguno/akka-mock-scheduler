package com.miguno.akka.testing

import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class FakeCancellableSpec extends FunSpec with Matchers with GivenWhenThen {

  describe("FakeCancellable") {

    it("should return false when cancelled") {
      Given("an instance")
      val cancellable = FakeCancellable()

      When("I cancel it")
      val result = cancellable.cancel()

      Then("it returns false")
      result should be(false)
    }

    it("isCancelled should return false when cancel was not called yet") {
      Given("an instance")
      val cancellable = FakeCancellable()

      When("I ask whether it has been successfully cancelled")
      Then("it returns false")
      cancellable should not be 'cancelled
    }

    it("isCancelled should return false when cancel was called already") {
      Given("an instance")
      val cancellable = FakeCancellable()
      And("the instance was cancelled")
      cancellable.cancel()

      When("I ask whether it has been successfully cancelled")
      Then("it returns false")
      cancellable should not be 'cancelled
    }

  }

}
