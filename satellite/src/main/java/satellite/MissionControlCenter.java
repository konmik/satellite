package satellite;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Printer;

import java.util.HashMap;

import rx.Notification;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.subjects.Subject;

public class MissionControlCenter implements Parcelable {

    private DataCenter dataCenter;

    private HashMap<String, SatelliteFactory> factories = new HashMap<>();

    public MissionControlCenter() {
        dataCenter = new DataCenter();
    }

    public <T> void satelliteFactory(Integer id, SatelliteFactory<T> factory) {
        factories.put(dataCenter.provideKey(id), factory);
    }

    public <T> Observable<Notification<T>> connection(final Integer id, final SessionType sessionType) {
        return SpaceStation.INSTANCE.<T>connection(
            dataCenter.provideKey(id),
            new Func0<Subject>() {
                @Override
                public Subject call() {
                    return sessionType.createSubject();
                }
            })
            .doOnNext(new Action1<Notification<T>>() {
                @Override
                public void call(Notification<T> tNotification) {
                    if (sessionType == SessionType.SINGLE)
                        dropSatellite(id);
                }
            });
    }

    public void launch(Integer id, final Bundle missionStatement) {
        dropSatellite(id);
        final String key = dataCenter.provideKey(id);
        dataCenter.registerLaunch(id, new SatelliteLaunch(key, missionStatement));
        SpaceStation.INSTANCE.connectSatellite(key, new Func0<Observable>() {
            @Override
            public Observable call() {
                return factories.get(key).call(missionStatement);
            }
        });
    }

    public void restoreSatellites() {
        for (final SatelliteLaunch launch : dataCenter.launches()) {
            final String key = launch.getKey();
            SpaceStation.INSTANCE.connectSatellite(key, new Func0<Observable>() {
                @Override
                public Observable call() {
                    return factories.get(key).call(launch.getArguments());
                }
            });
        }
    }

    public void dismiss() {
        SpaceStation.INSTANCE.clear(dataCenter.keys());
    }

    public void dropSatellite(Integer id) {
        SpaceStation.INSTANCE.disconnectSatellite(dataCenter.provideKey(id));
        dataCenter.deleteLaunch(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {dest.writeParcelable(dataCenter, flags);}

    protected MissionControlCenter(Parcel in) {
        dataCenter = in.readParcelable(DataCenter.class.getClassLoader());
    }

    public static final Creator<MissionControlCenter> CREATOR = new Creator<MissionControlCenter>() {
        @Override
        public MissionControlCenter createFromParcel(Parcel in) {
            return new MissionControlCenter(in);
        }

        @Override
        public MissionControlCenter[] newArray(int size) {
            return new MissionControlCenter[size];
        }
    };

    public void printSpaceStation(Printer printer) {
        SpaceStation.INSTANCE.print(printer);
    }
}
