# Reliable Coordinator Example _(WIP)_ #
A reliable coordinator is given a big task divided into a set of little tasks to execute which can potentially fail. 
It must ensure that all the little tasks get executed before it declares it the big task complete and it can move onto 
the next big task. Of course, each little task must be idempotent otherwise you will run into some serious issues. 

The Reliable Coordinator needs to be resilient and must be able to recover in the case of catastrophic failure.  If the 
Reliable Coordinator is interrupted whilst carrying out the process, it continues the tasks that were in progress when 
it comes back up.
