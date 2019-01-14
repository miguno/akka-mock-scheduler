# akka-mock-scheduler

A mock Akka scheduler to simplify testing scheduler-dependent code.

[![Build Status](https://travis-ci.org/miguno/akka-mock-scheduler.svg?branch=develop)](https://travis-ci.org/miguno/akka-mock-scheduler)
[![Coverage Status](https://coveralls.io/repos/miguno/akka-mock-scheduler/badge.svg?branch=develop&service=github)](https://coveralls.io/github/miguno/akka-mock-scheduler?branch=develop)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)
[<img src="https://img.shields.io/maven-central/v/com.miguno.akka/akka-mock-scheduler_2.12.svg?label=latest%20release%20for%202.12"/>](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22akka-mock-scheduler_2.12%22)
[<img src="https://img.shields.io/maven-central/v/com.miguno.akka/akka-mock-scheduler_2.11.svg?label=latest%20release%20for%202.11"/>](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22akka-mock-scheduler_2.11%22)
[<img src="https://img.shields.io/maven-central/v/com.miguno.akka/akka-mock-scheduler_2.10.svg?label=latest%20release%20for%202.10"/>](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22akka-mock-scheduler_2.10%22)

---

Table of Contents

* <a href="#Motivation">Motivation</a>
* <a href="#Usage">Usage</a>
    * <a href="#Build-dependency">Build dependency</a>
    * <a href="#Examples">Examples</a>
* <a href="#Design">Design and limitations</a>
* <a href="#Development">Development</a>
* <a href="#Changelog">Change log</a>
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

# Usage


<a name="Build-dependency"></a>

## Build dependency

This project is published to [Sonatype](https://oss.sonatype.org/).

* Releases are available on [Maven Central](http://search.maven.org/).
* Snapshots are available in the
  [Sonatype Snapshot repository](https://oss.sonatype.org/content/repositories/snapshots).

Latest **release** (works with Java 8+):

* Scala 2.12: [<img src="https://img.shields.io/maven-central/v/com.miguno.akka/akka-mock-scheduler_2.12*.svg?label=latest%20release%20for%202.12"/>](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22akka-mock-scheduler_2.12%22)
* Scala 2.11: [<img src="https://img.shields.io/maven-central/v/com.miguno.akka/akka-mock-scheduler_2.11*.svg?label=latest%20release%20for%202.11"/>](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22akka-mock-scheduler_2.11%22)

```scala
// Scala 2.12
libraryDependencies ++= Seq("com.miguno.akka" % "akka-mock-scheduler_2.12" % "0.5.4")

// Scala 2.11
libraryDependencies ++= Seq("com.miguno.akka" % "akka-mock-scheduler_2.11" % "0.5.4")
```

> **Scala 2.10 support is deprecated**: the latest release for Scala 2.10 and Java 7 is `0.4.0.`.
>
> ```scala
> // Scala 2.10
> libraryDependencies ++= Seq("com.miguno.akka" % "akka-mock-scheduler_2.10" % "0.4.0")
> ```

Latest **snapshot** (works with Java 8+; you can also
[browse the snapshot repository](https://oss.sonatype.org/content/repositories/snapshots/com/miguno/akka/)):

```scala
resolvers ++= Seq("sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")

// Scala 2.12
libraryDependencies ++= Seq("com.miguno.akka" % "akka-mock-scheduler_2.12" % "0.5.5-SNAPSHOT")

// Scala 2.11
libraryDependencies ++= Seq("com.miguno.akka" % "akka-mock-scheduler_2.11" % "0.5.5-SNAPSHOT")
```


<a name="Examples"></a>

## Examples

### First step: start sbt console

You can interactively test-drive the examples below via sbt console, which will automatically make the
akka-mock-scheduler library (plus dependencies such as Akka) available in the console:

```bash
$ ./sbt console
```

Then you can copy-paste the example code below to play around.


### Example 1

In this example we schedule a one-time task to run in 5 milliseconds from "now".  We create an instance of
[`VirtualTime`](src/main/scala/com/miguno/akka/testing/VirtualTime.scala), which contains its own
[`MockScheduler`](src/main/scala/com/miguno/akka/testing/MockScheduler.scala) instance.

> Tip: In practice, you rarely create `MockScheduler` instances yourself and instead interact with the scheduler through
> its enclosing `VirtualTime` instance.

Here, think of `time.advance()` as the logical equivalent of `Thread.sleep()`.

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import com.miguno.akka.testing.VirtualTime

// A time instance has its own mock scheduler associated with it
val time = new VirtualTime

// Schedule a one-time task that increments a counter
val counter = new java.util.concurrent.atomic.AtomicInteger(0)
time.scheduler.scheduleOnce(5.millis)(counter.getAndIncrement)

time.advance(4.millis)
assert(time.elapsed == 4.millis)
assert(counter.get == 0) // <<< not yet incremented, still too early

time.advance(1.millis)
assert(time.elapsed == 5.millis)
assert(counter.get == 1) // <<< now incremented, which means the task was run at the right time!
```


### Example 2

In your code you may want to make the scheduler configurable.  In the following example the class `Foo` has a field
`scheduler` that defaults to Akka's `system.scheduler` (cf. `akka.actor.ActorSystem#scheduler`).

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

val system = akka.actor.ActorSystem("my-system")

class Foo(scheduler: akka.actor.Scheduler = system.scheduler) {

  scheduler.scheduleOnce(500.millis)(bar())

  def bar(): Unit = ???

}
```

During testing you can then plug in the mock scheduler:

```scala
import com.miguno.akka.testing.VirtualTime

val time = new VirtualTime
val foo = new Foo(time.scheduler)

// Actual tests follow, which will leverage `time.advance(...)`.
```


### Further examples

See [MockSchedulerSpec](https://github.com/miguno/akka-mock-scheduler/blob/develop/src/test/scala/com/miguno/akka/testing/MockSchedulerSpec.scala)
for further details and examples.

You can also run the include test suite, which includes `MockSchedulerSpec`, to improve your understanding of how
the mock scheduler and virtual time work:

```bash
$ ./sbt clean test
```

Example output:

    [info] TaskSpec:
    [info] Task
    [info] - a task is smaller than another task with a larger delay
    [info]   + Given an instance
    [info]   + When compared to a second instance that runs later
    [info]   + Then the first instance is greater than the second
    [info]   + And the second instance is smaller than the first
    [info] - for two tasks with equal delays, the one with the smaller id is less than the other
    [info]   + Given an instance
    [info]   + When compared to second instance with a larger id
    [info]   + Then the first instance is greater than the second
    [info]   + And the second instance is smaller than the first
    [info] - tasks with equal delays and equal ids are equal
    [info]   + Given an instance
    [info]   + When compared to another with the same delay and id
    [info]   + Then it returns true
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
    [info] - should enforce a minimum advancement of 1 miliseconds
    [info]   + Given a time
    [info]   + Then it will throw an exception if time is advanced by less than 1 millisecond
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
    [info] - should run tasks with different delays in order
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a recurring task A
    [info]   + And I schedule a one-time task B to run when A has already been run a couple of times
    [info]   + Then A should run before B
    [info]   + And A should continue to run after B finished
    [info] - should run tasks that are scheduled for the same time in order of their registration with the scheduler
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a one-time task A
    [info]   + And I then schedule a recurring task B whose initial run is scheduled at the same time as A
    [info]   + And I then schedule a one-time task C to run at the same time as A
    [info]   + Then A should run before B, and B should run before C
    [info] - should support recursive scheduling
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a task A that schedules another task B
    [info]   + And I advance the time so that A was already run (and thus B is now registered with the scheduler)
    [info]   + Then B should be run with the configured delay (which will happen in one of the next ticks of the scheduler)
    [info] - should not run a cancelled task
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a one-time task
    [info]   + And I cancel the task before its execution time
    [info]   + Then the task should not run
    [info] - should not run a recurring task after it was cancelled
    [info]   + Given a time with a scheduler
    [info]   + And and an execution context
    [info]   + When I schedule a recurring task
    [info]   + And I advance the time so that task is executed once
    [info]   + And I cancel the task and advance the time further
    [info]   + Then the task should not run any more
    [info] MockCancellableSpec:
    [info] MockCancellable
    [info] - should return true when cancelled the first time
    [info]   + Given an instance
    [info]   + When I cancel it the first time
    [info]   + Then it returns true
    [info] - should return false when cancelled the second time
    [info]   + Given an instance
    [info]   + When I cancel it the second time
    [info]   + Then it returns false
    [info] - isCancelled should return false when cancel was not called yet
    [info]   + Given an instance
    [info]   + When I ask whether it has been cancelled
    [info]   + Then it returns false
    [info] - isCancelled should return true when cancel was called already
    [info]   + Given an instance
    [info]   + And the instance was cancelled
    [info]   + When I ask whether it has been cancelled
    [info]   + Then it returns true
    [info] Run completed in 360 milliseconds.
    [info] Total number of tests run: 19
    [info] Suites: completed 4, aborted 0
    [info] Tests: succeeded 19, failed 0, canceled 0, ignored 0, pending 0
    [info] All tests passed.


<a name="Design"></a>

# Design and limitations

* If you call `time.advance()`, then the scheduler will run any tasks that need to be executed in "one big swing":
  there will be no delay in-between tasks runs, however the execution _order_ of the tasks is honored.
  Note: If tasks are scheduled to run at the same time, then they will be run in the order of their registration with
  the scheduler.
    * Example 1: `time.elapsed` is `0 millis`.  Tasks `A` and `B` are scheduled to run with a delay of `10 millis` and
      `20 millis`, respectively.  If you now `advance()` the time straight to `50 millis`, then A will be executed first
      and, once A has finished and without any further delay, B will be executed immediately.
    * Example 2: `time.elapsed` is `0 millis`.  First, task `C` is scheduled to run with a delay of `300 millis`, then
      task `D` is scheduled to run with a delay of `300 millis`, too.  If you now `advance()` the time to `300 millis`
      or more, then task `C` will always be run before task `D` (because `C` was registered first).
* Tasks are executed synchronously when the scheduler's `tick()` method is called.


<a name="Development"></a>

# Development

## Running the test spec

```bash
# Runs the tests for the main Scala version only (currently: 2.12.x)
$ ./sbt test

# Runs the tests for all supported Scala versions
$ ./sbt "+ test"
```


## Publishing to Sonatype

### Preparation

Step 1: Ensure that your Sonatype credentials are available to `sbt`.

For sbt 1.x:

```
$ cat ~/.sbt/1.0/sonatype.sbt
credentials += Credentials("Sonatype Nexus Repository Manager",
        "oss.sonatype.org",
        "<sonatype-jira-username>",
        "<sonatype-jira-password>")
```

Step 2: Ensure that your shell environment has set `export GPG_TTY=$(tty)` (e.g. in `~/.bashrc`), otherwise you might
run into the error "gpg: signing failed: Inappropriate ioctl for device".


### Publishing a snaphost

1. Make sure that the version identifier in `version.sbt` has a `-SNAPSHOT` suffix.
2. Sign and publish the snapshot (note: `sonatypeRelease` is not needed for snapshots):

    ```shell
    $ ./sbt "+ test" && ./sbt "+ publish"
    ```


### Publishing a release

1. Make sure that the version identifier in `version.sbt` DOES NOT have a `-SNAPSHOT` suffix.
2. Sign and publish the release artifacts. The commands below will stage, close, and release the artifacts using the
   [sbt-sonatype](https://github.com/xerial/sbt-sonatype) plugin.  See also the Sonatype documentation on
   [how to release a deployment](http://central.sonatype.org/pages/releasing-the-deployment.html).

    ```shell
    $ ./sbt "+ test" && ./sbt "+ publish" && ./sbt "+ sonatypeRelease"
    ```


<a name="Changelog"></a>

# Change log

See [CHANGELOG](CHANGELOG.md).


<a name="License"></a>

# License

Copyright © 2014-2018 Michael G. Noll

See [LICENSE](LICENSE) for licensing information.


<a name="Credits"></a>

# Credits

The code in this project was inspired by
[MockScheduler](https://github.com/apache/kafka/blob/trunk/core/src/test/scala/unit/kafka/utils/MockScheduler.scala)
and [MockTime](https://github.com/apache/kafka/blob/trunk/core/src/test/scala/unit/kafka/utils/MockTime.scala)
in the [Apache Kafka](http://kafka.apache.org/) project.

See also [NOTICE](NOTICE).
