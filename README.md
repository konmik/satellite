Satellite
=======

Satellite is a simple (for those who are familiar with RxJava) Android library, which allows
to properly connect background tasks with visual parts of an application.

![](https://github.com/konmik/satellite/blob/images/images/satellite_logo_small.png)

## Introduction

If you've already seen my [Nucleus](https://github.com/konmik/nucleus) library:
Satellite is basically the same but is much simpler because it does NOT utilize MVP pattern
to do the same job.

**WARNING!** The project is in Alpha stage, the API is a subject to change.

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

Android docs are covering these problems very briefly, take a look at:
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

* In case of a configuration change Satellite automatically re-connects
all running background tasks to the new Activity/Fragment/View instance.
The application will not forget what it is doing.

* In case of a process restart Satellite automatically restarts background tasks that
are associated with the restored activity. Even when running on a low memory device or
waiting for a long running background task completion, the application is still reliable.

* The entire library has been built keeping [The Kiss Principle](https://people.apache.org/~fhanik/kiss.html) in mind.
Anyone who is familiar with RxJava can read and understand it easily.

* The library is extremely tiny: it consists of only 20Kb jar.

## Architecture

Satellite is full of cosmic analogies. Why? Because this is fun and because this allows
to construct an OOP model that is very close to what is going on. Reactive satellites are awesome. :)

![](https://github.com/konmik/satellite/blob/images/images/satellite.png)

`Satellite` is any RxJava `Observable` which resides in the background.
It is out of reach of lifecycle events.

There is
[SatelliteFactory](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/SatelliteFactory.java)
interface - we're implementing it to instantiate our satellite code
from a given argument. It is a good idea to declare the factory outside of `Activity` to
prevent memory leaks during long requests and time consuming operations.

[SpaceStation](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/SpaceStation.java)
is a singleton which keeps track of all launched
satellites. It connects satellites with activities and fragments, providing an `Observable` connection.
You don't normally need to use `SpaceStation` directly, but it is nice to know about it.
Sometimes you will want to get some debug information from its `keys()` method.

We also have
[MissionControlCenter](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/MissionControlCenter.java) -
this is our land base inside of Fragment/Activity which manages all
the cosmic stuff and guarantees that the mission will be completed despite of any lifecycle events.

[EarthBase](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/EarthBase.java)
is a set of `MissionControlCenter`s that allows to launch more than one satellite.
You may use `EarthBase` or `MissionControlCenter` directly, depending on your preferences.

`EarthBase` and `MissionControlCenter` are things that we need to persist within the activity state bundle.
They both have `instanceState()` methods which return `Parcelable` that can be serialized to be used for restoration later.

For every launch we need to provide a "mission statement". This means that we supply arguments for the launch.
It is recommended to supply arguments in a special `Parcelable` immutable object
[InputMap](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/io/InputMap.java)
instead of `Bundle`.
Immutable objects allow to avoid a wide range of problems that can be caused by using mutable
data structures. Immutable objects also provide reliable support for multithreading. If you want to
go deeper with immutability and functional juice on Android, take a look at
[Solid](https://github.com/konmik/solid) libraries.
and
[AutoParcel](https://github.com/frankiesardo/auto-parcel)
libraries.

## The code

Here is the typical code that is used to launch satellites. Some parts can be extracted into a base activity class
to eliminate code duplication.

```java
public class SignIn implements SatelliteFactory<InputMap, Boolean> {

    public static InputMap missionStatement(String username, String password) {
        return new InputMap("username", username, "password", password);
    }

    @Override
    public Observable<Boolean> call(InputMap in) {
        return serverApi.signIn(in.get("username"), in.get("password"))
            .observeOn(mainThread());
    }
}

public class SignInActivity extends Activity {

    private MissionControlCenter<InputMap, Boolean> controlCenter;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.button_sign_in).setOnClickListener(v ->
            controlCenter.launch(SignIn.missionStatement("user", "password")));

        controlCenter = savedInstanceState == null ?
            new MissionControlCenter<>() :
            new MissionControlCenter<>(savedInstanceState.getParcelable("center"));

        subscription = controlCenter.connection(SubjectFactory.behaviorSubject(), new SignIn())
            .subscribe(split(
                value -> Log.v("SignIn", "onNext " + value),
                throwable -> Log.v("SignIn", "onError " + throwable)));
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

##### split

You may noticed the [split](https://github.com/konmik/satellite/blob/master/satellite/src/main/java/satellite/util/RxNotification.java)
magic method. What it does?

All events come from `MissionControlCenter` in the materialized state
[materialize-dematerialize](http://reactivex.io/documentation/operators/materialize-dematerialize.html).
`split` dematerializes events and returns them into `onNext`, `onError`, `onComplete` lambdas.

##### SubjectFactory

There is also `SubjectFactory`. What *it* does? Basically, we need to be able to reconnect to background task
and receive the `onNext` value that has been emitted while the activity was destroyed. So, we need a subject
that will keep the value and re-emit it on connection. We use `BehaviorSubject` to replay the latest value,
we use `ReplaySubject` to replay all received values. Replaying of all received values makes it easy to
organize data paging and one-by-one streaming.

The example looks a little bit bloated now, but when you use `EarthBase` and implement `Launcher` interface on your
base activity
[BaseActivity](https://github.com/konmik/satellite/blob/master/example/src/main/java/satellite/example/BaseActivity.java)
it can become as simple as this:

```java
public class SignInActivity extends BaseActivity {

    private static final int SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.button_sign_in).setOnClickListener(v ->
            launch(SIGN_IN, SignIn.missionStatement("user", "password")));
    }

    @Override
    protected Subscription onConnect() {
        return connection(SIGN_IN, new SignIn())
            .subscribe(split(
                value -> Log.v("SignIn", "onNext " + value),
                throwable -> Log.v("SignIn", "onError " + throwable)));
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

