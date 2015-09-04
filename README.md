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
I'm not talking here about an *application* restart. A *process* restart is an event that happens randomly with
applications that are in the background, in which case the entire application gets killed and after
that its activities get restored from the saved state, while all background tasks and static variables
do not exist.

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
are associated with the restored activity. Even when running on a low memory device or
waiting for a long running background task completion, the application is still reliable.

* The entire library has been built keeping [The Kiss Principle](https://people.apache.org/~fhanik/kiss.html) in mind.
Anyone who is familiar with RxJava can read and understand it easily.

## Architecture

Satellite is full of cosmic analogies. Why? Because this is fun and because this allows
to construct an OOP model that is very close to what is going on. Reactive satellites are awesome. :)

![](https://github.com/konmik/satellite/blob/images2/images/satellite.png)

`Satellite` is any RxJava `Observable` which resides in the background.
It is out of reach of lifecycle events.

There is `SatelliteFactory` interface - we're implementing it to instantiate our satellite code
from a given argument. [SatelliteFactory](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/SatelliteFactory.java)
It is a good idea to declare the factory outside of `Activity` to prevent memory leaks.

`SpaceStation` is a singleton which keeps track of all launched
satellites. It connects satellites with activities and fragments, providing an `Observable` connection.
You don't normally need it, but it is nice to know about it.
Sometimes you will want to get some debug information from its `print()` method.
[SpaceStation](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/SpaceStation.java)

We also have `MissionControlCenter` - this is our land base inside of Fragment/Activity which manages all
the cosmic stuff and guarantees that the mission will be completed despite of any lifecycle events.
[MissionControlCenter](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/MissionControlCenter.java)

`EarthBase` is a set of `MissionControlCenter`s that allows to launch more than one satellite.
You may use `EarthBase` or `MissionControlCenter` directly, depending on your preferences.
[EarthBase](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/EarthBase.java)

`EarthBase` and `MissionControlCenter` are things that we need to persist within the activity state bundle.
They both have `instanceState()` methods which return `Parcelable` that can be serialized to be used for restoration later.

For every launch we need to provide a "mission statement". This means that we supply arguments for the launch.
It is recommended to supply arguments in a special `Parcelable` immutable object `InputMap` instead of `Bundle`.
Immutable objects allow to avoid a wide range of problems that can be caused by using mutable
data structures and provide reliable support for multithreading.
[InputMap](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/io/InputMap.java)

## The code

Here is the typical code that is used to launch satellites. Some parts can be extracted into a base activity class
to eliminate the code duplication.

```java
public class SignIn implements SatelliteFactory<InputMap, Boolean> {

    public static InputMap missionStatement(String username, String password) {
        return new InputMap("username", username, "password", password);
    }

    @Override
    public Observable<Integer> call(InputMap in) {
        return serverApi.signIn(in.get("username"), in.get("password"))
            .observerOn(mainThread());
    }
}

public class AnActivity extends Activity {

    private MissionControlCenter<InputMap, Integer> controlCenter;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.launch).setOnClickListener(v ->
            controlCenter.launch(SignIn.missionStatement("user", "password")));

        controlCenter = savedInstanceState == null ?
            new MissionControlCenter<>() :
            new MissionControlCenter<>(savedInstanceState.getParcelable("center"));

        subscription = controlCenter.connection(SubjectFactory.behaviorSubject(), new SignIn())
            .subscribe(RxNotification.split(
                value -> log("onNext " + value),
                throwable -> log("onError " + throwable))));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        if (isFinishing())
            controlCenter.dismiss();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("center", controlCenter.instanceState());
    }
}
```

This looks a little bit bloated now, but when you use `EarthBase` and implement `Launcher` interface on your
base activity this can become as simple as this:

```java
public class AnActivity extends BaseActivity {

    private static final int SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.launch).setOnClickListener(v ->
            launch(SIGN_IN, SignIn.missionStatement("user", "password")));
    }

    @Override
    protected Subscription onConnect() {
        return connection(SIGN_IN, SubjectFactory.behaviorSubject(), new SignIn())
            .subscribe(RxNotification.split(
                value -> log("onNext " + value),
                throwable -> log("onError " + throwable))));
    }
}
```

## Installation

This is a development version of the library.
If you want to try it - clone the repository and compile it yourself.

* Go to `gradle.properties`.
* Change `SNAPSHOT_REPOSITORY_URL` to your local repository.
* Run `gradle clean build uploadArchives`.

In your project:

* `compile 'info.android15.satellite:satellite:0.1.0-SNAPSHOT'`
* Don't forget to mention `mavenLocal()` in the list of your project's repositories.

## Feedback

Any feedback is welcome. :)

