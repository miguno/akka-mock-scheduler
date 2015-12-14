package com.miguno.akka.testing

import org.scalatest.mock.MockitoSugar
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

class FakeCancellableSpec extends FunSpec with Matchers with GivenWhenThen with MockitoSugar {

  describe("FakeCancellable") {

    it("should return true when cancelled the first time") {
      Given("an instance")
      val cancellable = MockCancellable(mock[MockScheduler], mock[Task])

      When("I cancel the first time it")
      val result = cancellable.cancel()

      Then("it returns true")
      result should be(true)
    }

    it("should return false when cancelled the second time") {
      Given("an instance")
      val cancellable = MockCancellable(mock[MockScheduler], mock[Task])

      When("I cancel the second time it")
      cancellable.cancel()
      val result = cancellable.cancel()

      Then("it returns false")
      result should be(false)
    }

    it("isCancelled should return false when cancel was not called yet") {
      Given("an instance")
      val cancellable = MockCancellable(mock[MockScheduler], mock[Task])

      When("I ask whether it has been successfully cancelled")
      Then("it returns false")
      cancellable.isCancelled should be(false)
    }

    it("isCancelled should return true when cancel was called already") {
      Given("an instance")
      val cancellable = MockCancellable(mock[MockScheduler], mock[Task])

      And("the instance was cancelled")
      cancellable.cancel()

      When("I ask whether it has been successfully cancelled")
      Then("it returns true")
      cancellable.isCancelled should be(true)
    }

  }

}
