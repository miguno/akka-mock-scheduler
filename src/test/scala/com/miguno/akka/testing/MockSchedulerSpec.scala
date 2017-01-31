package com.miguno.akka.testing

import java.util.concurrent.atomic.AtomicInteger

import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration._

class MockSchedulerSpec extends FunSpec with Matchers with GivenWhenThen with MockitoSugar {

  describe("MockScheduler") {

    it("should have a maximum frequency of 1000 Hz") {
      Given("a scheduler")
      val scheduler = {
        val time = mock[VirtualTime](Mockito.withSettings().stubOnly())
        new MockScheduler(time)
      }

      When("I ask for the maximum frequency")
      val frequency = scheduler.maxFrequency

      Then("the frequency is 1000 Hz")
      frequency should be(1000)
    }

    it("should run a one-time task once") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a one-time task")
      val counter = new AtomicInteger(0)
      time.scheduler.scheduleOnce(5.millis)(counter.getAndIncrement)

      Then("the task should not run before its delay")
      time.advance(4.millis)
      counter.get should be(0)
      And("the task should run at the time of its delay")
      time.advance(1.millis)
      counter.get should be(1)
      And("the task should not run again")
      time.advance(10000.millis)
      counter.get should be(1)
    }

    it("should run a recurring task multiple times") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a recurring task")
      val counter = new AtomicInteger(0)
      val initialDelay = 5.millis
      val interval = 10.millis
      time.scheduler.schedule(initialDelay, interval)(counter.getAndIncrement)

      Then("the task should not run before its initial delay")
      time.advance(4.millis)
      counter.get should be(0)
      And("it should run at the time of its initial delay (run #1)")
      time.advance(1.millis)
      counter.get should be(1)
      And("it should not run again before its next interval")
      time.advance(9.millis)
      counter.get should be(1)
      And("it should run again at its next interval (run #2)")
      time.advance(1.millis)
      counter.get should be(2)
      And("it should not run again before its next interval")
      time.advance(9.millis)
      counter.get should be(2)
      And("it should run again at its next interval (run #3)")
      time.advance(1.millis)
      counter.get should be(3)
      And("it should have run 103 times after the initial delay and 102 intervals")
      time.advance(interval * 100)
      counter.get should be(103)
    }

    it("should run tasks with different delays in order") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a recurring task A")
      val counterA = new AtomicInteger(0)
      time.scheduler.schedule(5.millis, 10.millis)(counterA.getAndIncrement)
      And("I schedule a one-time task B to run when A has already been run a couple of times")
      val counterB = new AtomicInteger(0)
      time.scheduler.scheduleOnce(20.millis)(counterB.getAndIncrement)

      Then("A should run before B")
      time.advance(19.millis)
      counterA.get should be(2)
      counterB.get should be(0)
      time.advance(1.millis)
      counterA.get should be(2)
      counterB.get should be(1)
      And("A should continue to run after B finished")
      time.advance(60.millis)
      counterA.get should be(8)
      counterB.get should be(1)
    }

    it("should run tasks that are scheduled for the same time in order of their registration with the scheduler") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a one-time task A")
      val counter = new AtomicInteger(0)
      val delay = 20.millis
      time.scheduler.scheduleOnce(delay)(counter.compareAndSet(0, 1))
      And("I then schedule a recurring task B whose initial run is scheduled at the same time as A")
      time.scheduler.schedule(delay, delay)(counter.compareAndSet(1, 42))
      And("I then schedule a one-time task C to run at the same time as A")
      time.scheduler.scheduleOnce(delay)(counter.compareAndSet(42, 80))

      Then("A should run before B, and B should run before C")
      time.advance(delay)
      counter.get should be(80)
    }

    it("should support recursive scheduling") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a task A that schedules another task B")
      val counter = new AtomicInteger(0)
      time.scheduler.scheduleOnce(10.millis)(time.scheduler.scheduleOnce(20.millis)(counter.getAndIncrement))
      And("I advance the time so that A was already run (and thus B is now registered with the scheduler)")
      time.advance(50.millis)
      counter.get should be(0) // <<< the scheduler has only ticked once at this point, so B will not have been executed yet

      Then("B should be run with the configured delay (which will happen in one of the next ticks of the scheduler)")
      time.advance(19.millis)
      counter.get should be(0)
      time.advance(1.millis)
      counter.get should be(1)
    }

    it("should not run a cancelled task") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a one-time task")
      val delay = 5.millis
      val counter = new AtomicInteger(0)
      val scheduledIncrement = time.scheduler.scheduleOnce(delay)(counter.getAndIncrement)
      And("I cancel the task before its execution time")
      scheduledIncrement.cancel()
      time.advance(delay)

      Then("the task should not run")
      counter.get should be(0)
    }

    it("should not run a recurring task after it was cancelled") {
      Given("a time with a scheduler")
      val time = new VirtualTime
      And("and an execution context")
      import scala.concurrent.ExecutionContext.Implicits.global

      When("I schedule a recurring task")
      val delay = 5.millis
      val counter = new AtomicInteger(0)
      val scheduledIncrement = time.scheduler.schedule(delay, delay)(counter.getAndIncrement)
      And("I advance the time so that task is executed once")
      time.advance(delay)
      counter.get should be(1)
      And("I cancel the task and advance the time further")
      scheduledIncrement.cancel()
      time.advance(delay)

      Then("the task should not run any more")
      counter.get should be(1)
    }
  }

}