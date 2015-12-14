package com.miguno.akka.testing

import akka.actor.Cancellable

private[testing] case class MockCancellable(scheduler: MockScheduler, task: Task) extends Cancellable {

  private[this] var canceled: Boolean = false

  /**
    * Possibly cancels this Cancellable.  If the Cancellable has not already
    * been canceled, or terminated after a single execution, then the
    * cancellable will be canceled.  If cancel has already been called or
    * the task has already terminated, then no action will be taken.
    *
    * @return True if the Cancellable was canceled by THIS invocation of the
    *         cancel method, false otherwise.
    */
  override def cancel(): Boolean = {
    this synchronized {
      canceled match {
        case true => false
        case false => {
          canceled = true
          scheduler.cancelTask(task)
          true
        }
      }
    }
  }

  /**
    * True if this Cancellable has been canceled.
    *
    * @return Returns true if this cancellable has been canceled, false
    *         otherwise.
    */
  override def isCancelled: Boolean =
    this synchronized {
      canceled
    }

}