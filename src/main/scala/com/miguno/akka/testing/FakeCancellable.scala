package com.miguno.akka.testing

import akka.actor.Cancellable

private case class FakeCancellable() extends Cancellable {

  /**
   * No-op cancel, i.e. this method will do nothing.
   *
   * We decided to use such a noop-always-returns-false strategy instead of throwing NotImplementedErrors to
   * make the use of [[FakeCancellable]] more convenient in "typical" testing code.
   *
   * @return Always returns false.
   */
  override def cancel(): Boolean = false

  /**
   * Because `cancel()` is currently a no-op this method will always return false.
   *
   * @return Always returns false.
   */
  override def isCancelled: Boolean = false

}