package satellite;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class SatelliteLaunch implements Parcelable {

    private final String key;
    private final Bundle arguments;

    public SatelliteLaunch(String key, Bundle arguments) {
        this.key = key;
        this.arguments = arguments;
    }

    public Bundle getArguments() {
        return arguments;
    }

    public String getKey() {
        return key;
    }

    protected SatelliteLaunch(Parcel in) {
        key = in.readString();
        arguments = in.readBundle();
    }

    public static final Creator<SatelliteLaunch> CREATOR = new Creator<SatelliteLaunch>() {
        @Override
        public SatelliteLaunch createFromParcel(Parcel in) {
            return new SatelliteLaunch(in);
        }

        @Override
        public SatelliteLaunch[] newArray(int size) {
            return new SatelliteLaunch[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeBundle(arguments);
    }
}
