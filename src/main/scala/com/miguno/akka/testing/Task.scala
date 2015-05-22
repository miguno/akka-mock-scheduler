package com.miguno.akka.testing

import scala.concurrent.duration.FiniteDuration

/**
 * A task that can be scheduled and run by [[MockScheduler]].
 *
 * Note: You should not work with class directly.  It is only visible for testing.
 */
private[testing] case class Task(delay: FiniteDuration, id: Long, runnable: Runnable, interval: Option[FiniteDuration])
    extends Ordered[Task] {

  override def compare(t: Task): Int =
    if (delay > t.delay) -1
    else if (delay < t.delay) 1
    else if (id > t.id) -1
    else if (id < t.id) 1
    else 0

}