# akka-mock-scheduler [![Build Status](https://travis-ci.org/miguno/akka-mock-scheduler.svg?branch=develop)](https://travis-ci.org/miguno/akka-mock-scheduler) [![Coverage Status](https://coveralls.io/repos/miguno/akka-mock-scheduler/badge.svg)](https://coveralls.io/r/miguno/akka-mock-scheduler)

A mock Akka scheduler to simplify testing of scheduler-dependent code.

---

Table of Contents

* <a href="#Motivation">Motivation</a>
* <a href="#Usage">Usage examples</a>
* <a href="#Design">Design and limitations</a>
* <a href="#License">License</a>
* <a href="#Credits">Credits</a>

---


<a name="Motivation"></a>

# Motivation

[Akka Scheduler](http://doc.akka.io/docs/akka/snapshot/java/scheduler.html) is a convenient tool to make things happen
in the future -- for example, "run this function in 5 seconds" or "run that function every 100 milliseconds".

Let's say you want to periodically run the function `myFunction()` in your code via Akka Scheduler:

```scala
def myFunction() = ???

val initialDelay = 0.millis
val interval = 100.millis
scheduler.schedule(initialDelay, interval)(myFunction())
```

Unfortunately, the current Akka implementation apparently does not provide a simple way to test-drive code that relies
on Akka Scheduler (see e.g. [Testing Actor Systems](http://doc.akka.io/docs/akka/snapshot/scala/testing.html)).  This
project closes this gap by providing a "mock scheduler" and an accompanying "virtual time" implementation so that your
test suite does not degrade into `Thread.sleep()` hell.

Please note that the scope of this project is not to become a full-fledged testing kit for Akka Scheduler!


<a name="Usage"></a>

# Usage examples

## Example 1

In this example we schedule a one-time task to run in 5 milliseconds from "now".  We create an instance of
[`VirtualTime`](src/main/scala/com/miguno/akka/testing/VirtualTime.scala), which contains its own
[`MockScheduler`](src/main/scala/com/miguno/akka/testing/MockScheduler.scala) instance.

> Tip: In practice, you rarely create `MockScheduler` instances yourself and instead interact with the scheduler through
> its enclosing `VirtualTime` instance.

Here, think of `time.advance()` as the logical equivalent of `Thread.sleep()`.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// A time instance has its own mock scheduler associated with it
val time = new VirtualTime

// Schedule a one-time task that increments a counter
val counter = new AtomicInteger(0)
time.scheduler.scheduleOnce(5.millis)(counter.getAndIncrement)

time.advance(4.millis)
assert(time.elapsed == 4.millis)
assert(counter.get == 0) // <<< not yet, still too early

time.advance(1.millis)
assert(time.elapsed == 5.millis)
assert(counter.get == 1) // <<< task was run at the right time!
```


## Example 2

In your code you may want to make the scheduler configurable.  In the following example the class `Foo` has a field
`scheduler` that defaults to Akka's `system.scheduler` (cf. `akka.actor.ActorSystem#scheduler`).

```scala
class Foo(scheduler: Scheduler = system.scheduler) {

  scheduler.scheduleOnce(500.millis)(bar())

  def bar: Unit = ???

}
```

During testing you can then plug in the mock scheduler:

```scala
val time = VirtualTime
val foo = Foo(time.scheduler)

// ...actual tests follow...
```


## Further examples

See [MockSchedulerSpec](https://github.com/miguno/akka-mock-scheduler/blob/develop/src/test/scala/com/miguno/akka/testing/MockSchedulerSpec.scala)
for further details and examples.

You can also run the include test suite, which includes `MockSchedulerSpec`, to improve your understanding of how
the mock scheduler and virtual time work:

    $ ./sbt clean test

Example output:

    [info] VirtualTimeSpec:
    [info] VirtualTime
    [info] - should start at time zero
    [info]   + Given no time
    [info]   + When I create a time
    [info]   + Then its elapsed time should be zero
    [info] - should track elapsed time
    [info]   + Given a time
    [info]   + When I advance the time
    [info]   + Then the elapsed time should be correct
    [info] - should accept a step defined as a Long that represents the number of milliseconds
    [info]   + Given a time
    [info]   + When I advance the time by a Long value of 1234
    [info]   + Then the elapsed time should be 1234 milliseconds
    [info] - should have a meaningful string representation
    [info]   + Given a time
    [info]   + When I request its string representation
    [info]   + Then the representation should include the elapsed time in milliseconds
    [info] MockSchedulerSpec:
    [info] MockScheduler
    [info] - should run a one-time task once
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a one-time task
    [info]   + Then the task should not run before its delay
    [info]   + And the task should run at the time of its delay
    [info]   + And the task should not run again
    [info] - should run a recurring task multiple times
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a recurring task
    [info]   + Then the task should not run before its initial delay
    [info]   + And it should run at the time of its initial delay (run #1)
    [info]   + And it should not run again before its next interval
    [info]   + And it should run again at its next interval (run #2)
    [info]   + And it should not run again before its next interval
    [info]   + And it should run again at its next interval (run #3)
    [info]   + And it should have run 103 times after the initial delay and 102 intervals
    [info] - should run tasks in order
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a recurring task A
    [info]   + And I schedule a one-time task B to run when A has already been run a couple of times
    [info]   + Then A should run before B
    [info]   + And A should continue to run after B finished
    [info] - should run one-time tasks in order of their registration with the scheduler
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a one-time task A
    [info]   + And I then schedule a one-time task B to run at the same time as A
    [info]   + Then A should run before B
    [info] - should, for tasks that are scheduled for the same time, run one-time tasks before subsequent runs of recurring tasks
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a recurring task A
    [info]   + And I schedule two one-time tasks B and C, which are scheduled at the same time as A's recurring runs
    [info]   + Then B and C should happen before the recurring runs of A
    [info] - should support recursive scheduling
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a task A that schedules another task B
    [info]   + And I advance the time so that A was already run (and thus B is now registered with the scheduler)
    [info]   + Then B should be run with the configured delay (which will happen in one of the next ticks of the scheduler)
    [info] Run completed in 313 milliseconds.
    [info] Total number of tests run: 10
    [info] Suites: completed 2, aborted 0
    [info] Tests: succeeded 10, failed 0, canceled 0, ignored 0, pending 0
    [info] All tests passed.


<a name="Design"></a>

# Design and limitations

* If you call `time.advance()`, then the scheduler will run any tasks that need to be executed in "one big swing":
  there will be no delay in-between tasks runs, however the execution _order_ of the tasks is honored.
    * Example: `time.elapsed` is `0 millis`.  Tasks `A` and `B` are scheduled to run with a delay of `10 millis` and
      `20 millis`, respectively.  If you now `advance()` the time straight to `50 millis`, then A will be executed first
      and, once A has finished and without any further delay, B will be executed immediately.
* Tasks are executed synchronously when the scheduler's `tick()` method is called.
* For simplicity reasons the `akka.actor.Cancellable` instances returned by this scheduler are not really functional.
  The `Cancellable.cancel()` method is a no-op and will always return false.  This has the effect that
  `Cancellable.isCancelled` will always return false, too, to adhere to the `Cancellable` contract.


<a name="License"></a>

# License

Copyright © 2014-2015 Michael G. Noll

See [LICENSE](LICENSE) for licensing information.


<a name="Credits"></a>

# Credits

The code in this project was inspired by
[MockScheduler](https://github.com/apache/kafka/blob/trunk/core/src/test/scala/unit/kafka/utils/MockScheduler.scala)
and [MockTime](https://github.com/apache/kafka/blob/trunk/core/src/test/scala/unit/kafka/utils/MockTime.scala)
in the [Apache Kafka](http://kafka.apache.org/) project.

See also [NOTICE](NOTICE).
