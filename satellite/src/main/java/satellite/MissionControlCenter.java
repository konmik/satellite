package satellite;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

import rx.Notification;
import rx.Observable;

public class MissionControlCenter implements Parcelable {

    private DataCenter dataCenter;

    private HashMap<String, SatelliteFactory> factories = new HashMap<>();

    public MissionControlCenter() {
        dataCenter = new DataCenter();
    }

    public <T> void satelliteFactory(Integer id, SatelliteFactory<T> factory) {
        factories.put(dataCenter.provideKey(id), factory);
    }

    public <T> Observable<Notification<T>> connection(final Integer id) {
        return SpaceStation.INSTANCE.connection(dataCenter.provideKey(id));
    }

    public void launch(Integer id, Bundle missionStatement) {
        dropSatellite(id);
        String key = dataCenter.provideKey(id);
        dataCenter.registerLaunch(id, new SatelliteLaunch(key, missionStatement));
        SpaceStation.INSTANCE.connectWithSatellite(key, factories.get(key).call(missionStatement));
    }

    public void restoreSatellites() {
        for (SatelliteLaunch launch : dataCenter.launches()) {
            String key = launch.getKey();
            if (!SpaceStation.INSTANCE.satelliteConnectionExist(key))
                SpaceStation.INSTANCE.connectWithSatellite(key, factories.get(key).call(launch.getArguments()));
        }
    }

    public void dismiss() {
        SpaceStation.INSTANCE.clear(dataCenter.keys());
    }

    public void dropSatellite(Integer id) {
        SpaceStation.INSTANCE.disconnectFromSatellite(dataCenter.provideKey(id));
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
}
