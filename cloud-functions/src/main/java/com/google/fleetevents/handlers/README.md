Fleet Event Handlers take in internal events from the Fleet Event Creator class
and use that information to interpret each event as an alert. Some example
handlers are already implemented and can be further modified for custom
purposes.

During processing, each event being responded to, and a transaction object are
passed to the handler. The methods, `respondsTo` and `handleEvent`, are
separated to allow for adding `get` and `set` Firestore calls to the transaction
object.

Firestore transactions that call get after a set will cause a failure and roll
back. All custom Firestore get calls should be added to the respondsTo method
storing any needed information for the handleEvent in the class. Any custom
Firestore set or update calls should be made in the handleEvent method.

More information about Firestore transactions can be found here:
`[https://firebase.google.com/docs/firestore/manage-data/transactions]`(https://firebase.google.com/docs/firestore/manage-data/transactions).

## ETAChangeHandler

This handler returns ETA changes above a configurable threshold. This notifies
per-task.

- Types
    - ETA - Measures an ETA change. This triggers when the Task ETA changes
      `THRESHOLD`</sub> above or below the original estimate.
    - RELATIVE<sub>ETA</sub> - Measures a relative ETA change. This triggers
      when the Task
      duration changes a percentage `THRESHOLD`</sub> above or
      below the original
      estimate.

Here are some scenarios that demonstrate when outputs are triggered.

**ETA changes above threshold**

1. Vehicle V<sub>1</sub> starts a task T<sub>1</sub> at time `t1`, with
   original ETA `t2`. Its
   next task T<sub>2</sub> has an ETA of `t3`.
1. V<sub>1</sub> is delayed, causing the ETA for task T<sub>1</sub> and T<sub>
   2</sub> to be delayed to `[t2 +
   THRESHOLD + 1]`, and `[t3 + THRESHOLD + 1]`.
1. 2 EtaOutputEvent of type ETA are written. The delta is `[THRESHOLD + 1]`.

**ETA does not recover**

1. The same V<sub>1</sub> continues to T<sub>1</sub>, but ETA does not recover
   from the delay, and
   remains `[THRESHOLD + 1]`. The handler continues to notify for both tasks.

**Task completed**

1. The same V<sub>1</sub> completes T<sub>1</sub> and is on its way to T<sub>
   2</sub>. The handler continues to
   notify for T<sub>2</sub>.

Relative ETA will alert similarly, but instead measures the percent change from
original duration.

## TaskOutcomeHandler

This handler returns the change in Task Outcome per task.

- Possible Task Outcomes
    - UNSPECIFIED (or null) - task outcome before the value is set.
    - SUCCEEDED - the task ended in a successful outcome
    - FAILED - the task failed

Here are some scenarios that demonstrate when outputs are triggered.

1. Task T<sub>1</sub> is created with TaskOutcome{UNSPECIFIED}. Handler does not
   output
   anything.
1. Task T<sub>1</sub>'s outcome changes to TaskOutcome{SUCCEEDED}. Handler
   outputs a
   TaskOutcomeOutputEvent with the change in outcome.

## TimeRemainingHandler

This handler notifies when the time remaining on a task has passed the
configured threshold.

Here are some scenarios that demonstrate when outputs are triggered.

1. Vehicle V<sub>1</sub> moves towards its destination, where it'll complete
   task T<sub>1</sub> with
   ETA `t1`. Another task T<sub>2</sub> has been scheduled with ETA `t2`.
1. V<sub>1</sub> is now `[THRESHOLD - 1]` away from T<sub>1</sub>. The handler
   will trigger an output
   for T<sub>1</sub>.
1. V<sub>1</sub> is delayed, and is `[THRESHOLD + 1]` away from T<sub>1</sub>.
   The
   handler does not
   respond.
1. V<sub>1</sub> is again `[THRESHOLD - 1]` away from T<sub>1</sub>. The handler
   will trigger another
   output for T<sub>1</sub>.
1. V<sub>1</sub> reaches T<sub>1</sub> and is now on its way to T<sub>2</sub>.
   ETA `t2` is `[THRESHOLD + 5]`
   away. The handler does not respond.

## Contributors

Google maintains this article. The following contributors originally wrote it.

Principal authors:

- Ethel Bao | Software Engineer, Google Maps Platform
- Mohanad Almiski | Software Engineer, Google Maps Platform
- Naoya Moritani | Solutions Engineer, Google Maps Platform
