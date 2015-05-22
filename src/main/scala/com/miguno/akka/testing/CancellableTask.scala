package com.miguno.akka.testing

import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.Cancellable

/**
 * A wrapper for [[Task]] that supports cancellation of the task.
 *
 * Note: You should not work with class directly.  It is only visible for testing.
 *
 * Implementation detail: When the [[MockScheduler]] returns [[Cancellable]], it actually returns instances of this
 * class.
 */
private[testing] class CancellableTask(val task: Task) extends Cancellable with Ordered[CancellableTask] {

  private val wasCancelled = new AtomicBoolean(false)

  override def cancel(): Boolean = wasCancelled.getAndSet(true)

  override def isCancelled: Boolean = wasCancelled.get

  override def compare(c: CancellableTask): Int = task.compare(c.task)

}

private[testing] object CancellableTask {

  def apply(task: Task): CancellableTask = new CancellableTask(task)

}