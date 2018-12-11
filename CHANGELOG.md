# 0.5.4 (December 11, 2018)

* Upgrade Akka to 2.5.19.
* Built against Scala versions 2.12.8 and 2.11.12.

# 0.5.1 (January 17, 2017)

BUG FIXES

* [GH-8] Cancelled recurring task keeps executing. (thanks ivashin)


# 0.5.0 (December 07, 2016)

This release adds support for Scala 2.12.

IMPROVEMENTS

* [GH-7] Update dependency versions including Akka (now using Akka 2.4.14).  Add support for Scala 2.12.
  (thanks koshelev)

BREAKING CHANGES

* Supports only Scala 2.11 and Scala 2.12, built against Java 8.
* Support for Scala 2.10 and Java 7 was dropped.  Use version `0.4.0` if you still need to work against those.


# 0.4.0 (December 14, 2015)

IMPROVEMENTS

* [GH-5] Tasks are now cancellable.  When scheduling a task via e.g. `scheduler.scheduleOnce()`, the returned
  [Cancellable](http://doc.akka.io/api/akka/2.3.9/index.html#akka.actor.Cancellable) can now be cancelled;
  if it is cancelled before its execution time, then the `MockScheduler` will not run the task.
  (thanks mmacfadden)


# 0.3.1 (August 05, 2015)

BUG FIXES

* [GH-4] Fix deadlock involving `MockScheduler#schedule()` and `VirtualTime#advance()`. (thanks emrecelikten)


# 0.3.0 (March 23, 2015)

BREAKING CHANGES / BUG FIXES

* [GH-1] Tasks scheduled to run at the same time will be run in registration order.
  This is primarily a bug fix to ensure that "conflicting" tasks are run in a well-defined, deterministic order.
  The breaking change is that unlike the previous version we will not run one-time tasks always before recurring tasks.
  If there is a scheduling "conflict", then the tasks -- whether one-time or recurring -- will always be run in the
  order of registration with the scheduler. (thanks DylanArnold)


# 0.2.0 (January 23, 2015)

* Initial release
