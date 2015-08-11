Satellite
=======

Satellite is a simple (for those who are familiar with RxJava) Android library, which allows
to properly connect background tasks with visual parts of an application.

## Introduction

If you've already seen my [Nucleus](https://github.com/konmik/nucleus) library:
Satellite is basically the same but is much simpler because it does NOT utilize MVP pattern
to do the same job.

**WARNING!** This is an early proof-of-concept, please do not use it on production! :)

### Problem

There are some defects in our Android applications and because of these defects we can not call
our application reliable:

1. An application is unable to continue a background task execution after a configuration change.

And, if a developer is smart enough to handle the previous problem, there is a second one:

2. An application does not automatically restart a background task after a process restart.

While most applications work without such capabilities, their absence is an obvious bug that just sits there
and waits for a user who pressed "Login" button while being in a subway and switched to another application
because his network connection was too slow. Bugs that almost any application produce in such cases
are numerous.

Android docs are covering such problems very briefly, take a look at:
[Processes and Threads - 4. Background process](http://developer.android.com/guide/components/processes-and-threads.html#Lifecycle)
*"If an activity implements its lifecycle methods correctly, and saves
its current state, killing its process will not have a visible effect on
the user experience, because when the user navigates back to the activity,
the activity restores all of its visible state."*

This is not true - there WILL be a visible effect because we're not restoring background tasks.
The application will restore it's visual state, but it will forget what it is *doing*.
So, if an application restores a progress bar, but does not restore the background task itself -
a user will see the usual "progress bar forever" bug.

### Satellite

* In case of configuration change Satellite automatically re-attaches
all running background tasks to the new View instance.
The application will not forget what it is doing.

* In case of a process restart Satellite automatically restarts background tasks that
are associated with the restored activity.
Even when running on a low memory device or waiting for a long running background task completion,
the application is still reliable.

* The entire library has been built keeping [The Kiss Principle](https://people.apache.org/~fhanik/kiss.html) in mind.
Anyone who is familiar with RxJava can read and understand it easily.

### Architecture

Satellite is full of cosmic analogies. Why? Because this is fun and because this allows
to construct an OOP model that is very close to what is going on. Reactive satellites are awesome. :)

`Satellite` is any RxJava `Observable` which resides in the background.
It is out of reach of lifecycle events.

`SpaceStation` is a singleton which keeps track of all launched
satellites. It connects satellites with activities and fragments, providing an `Observable` connection.
[SpaceStation](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/SpaceStation.java)

We also have `MissionControlCenter` - this is our land base inside of fragment/activity which manages all
the cosmic stuff and guarantees that the mission will be completed despite of any lifecycle events.
`MissionControlCenter` is the only thing that we need to persist within the activity state bundle.
It implements `Parcelable` interface, so this is a trivial task. [MissionControlCenter](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/MissionControlCenter.java)

For every launch we need to provide a "mission statement". This means that we supply a `Bundle`
with arguments for the launch.

There is `SatelliteFactory` interface - we're extending it to instantiate our satellite code
from a given `Bundle` argument. [SatelliteFactory](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/SatelliteFactory.java)
                                                             
## Installation

This is a development version of the library.
If you want to try it - clone the repository and compile it yourself.

Any feedback is welcome.

