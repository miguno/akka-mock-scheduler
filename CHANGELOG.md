# 0.3.0 (March 23, 2015)

BREAKING CHANGES / BUG FIXES

* Tasks scheduled to run at the same time will be run in registration order.
  This is primarily a bug fix to ensure that "conflicting" tasks are run in a well-defined, deterministic order.
  The breaking change is that unlike the previous version we will not run one-time tasks always before recurring tasks.
  If there is a scheduling "conflict", then the tasks -- whether one-time or recurring -- will always be run in the
  order of registration with the scheduler. [GH-1] (thanks DylanArnold)


# 0.2.0 (January 23, 2015)

* Initial release
