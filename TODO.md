## Remaining Tasks ##
- Implement the request-response pattern. You have the option of using a mutable `var` to store the `sender` reference. 
You will have to send a response to the user before shutting down. In the case of recovery, this gets trickier. You 
will need to handle the `StartBigTask` message in all `Receive` blocks. When you get this message, you must update 
the `var` holding the `sender` reference. So if the process got stopped when it completed Task B, you would expect to
receive a `StartBigTask` in `awaitTaskB` where the sender reference would be updated. 

- Implement the Saga pattern. `Task A`, `Task B` and `Task C` each have __compensating actions__ that must be executed if `Task A`, 
`Task B`, or `Task` C fail. Since we have implied that `Task C` is _dependent_ on `Task B` and `Task B` is _dependent_ on `Task A`. 
You must execute the compensating actions of each of the Tasks that you have executed. For example, if `Task C` were to fail, then
you must execute the compensating action on `Task B` and the compensating action on `Task A`.
  - Do not retry infinitely anymore
  - Decrease the failure threshold so there is a high chance of passing in most cases so you can observe rollback 
  behavior

### Notes ###
If the actor experienced catastrophic failure and is resuming, `receiveRecover` will take place first and at the end,
the `RecoveryCompleted` is used to indicate the end of the recovery process then the requestor's message will be 
processed. This is an important fact to take note of because we change the `Receive` blocks and we need to account for
this in order to capture the `sender` reference of the requestor.

### References ###
- Roland Kuhn's [videos](https://www.reactivedesignpatterns.com/videos.html)
- Roland Kuhn's [notes](https://www.reactivedesignpatterns.com/categories.html)
- [Reactive Design Patterns](https://www.manning.com/books/reactive-design-patterns)
