package com.miguno.akka.testing

import akka.actor.{Cancellable, Scheduler}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * A scheduler whose `tick` can be triggered manually, which is helpful for testing purposes.
 * Tasks are executed synchronously when `tick` is called.
 *
 * Typically this scheduler is used indirectly via a [[VirtualTime]] instance.
 */
class MockScheduler(time: VirtualTime) extends Scheduler {

  private[this] var id = 0L

  // Tasks are sorted ascendingly by execution time (head is the next task to be executed)
  private[this] val tasks = new collection.mutable.PriorityQueue[Task]()

  /**
   * Runs any tasks that are due at this point in time.  This includes running recurring tasks multiple times if needed.
   * The execution of tasks happens synchronously in the calling thread.
   *
   * Tasks are executed in order based on their scheduled execution time.  We do not define the execution ordering of
   * tasks that are scheduled for the same time.
   *
   * Implementation detail:  If you are using this scheduler indirectly via a [[VirtualTime]] instance, then this method
   * will be called automatically by the [[VirtualTime]] instance, and you should not manually call it.
   */
  def tick(): Unit =
    synchronized {
      while (tasks.nonEmpty && tasks.head.delay <= time.elapsed) {
        val head = tasks.dequeue()
        if (!head.isCancelled) {
          head.runnable.run()
          head.interval match {
            case Some(interval) => tasks += new Task(head.delay + interval, head.id, head.runnable, head.interval)
            case None =>
          }
        }
      }
    }

  override def scheduleOnce(delay: FiniteDuration, runnable: Runnable)
                           (implicit executor: ExecutionContext): Cancellable =
    addToTasks(delay, runnable, None)

  override def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable)
                       (implicit executor: ExecutionContext): Cancellable =
    addToTasks(initialDelay, runnable, Option(interval))

  private def addToTasks(delay: FiniteDuration, runnable: Runnable, interval: Option[FiniteDuration]): Cancellable =
    synchronized {
      id += 1
      val startTime = time.elapsed + delay
      val task = new Task(startTime, id, runnable, interval)
      tasks += task
      MockCancellable(task)
    }

  override val maxFrequency: Double = 1.second / 1.millis

  private case class Task(delay: FiniteDuration, id: Long, runnable: Runnable, interval: Option[FiniteDuration])
      extends Ordered[Task] {

    var isCancelled = false

    def compare(t: Task): Int =
      if (delay > t.delay) -1
      else if (delay < t.delay) 1
      else if (id > t.id) -1
      else if (id < t.id) 1
      else 0

  }

  private case class MockCancellable(task: Task) extends Cancellable {

    override def cancel(): Boolean =
      MockScheduler.this synchronized {
        val wasCancelled = task.isCancelled
        task.isCancelled = true
        !wasCancelled
      }

    override def isCancelled: Boolean = task.isCancelled

  }

}
