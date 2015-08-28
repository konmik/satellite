package satellite.connections;

import android.os.Bundle;

import satellite.MissionControlCenter;
import satellite.SatelliteFactory;

public class SingleConnectionFactory<T> implements MissionControlCenter.ConnectionFactory<T> {

    private final SatelliteFactory<T> satelliteFactory;

    public SingleConnectionFactory(SatelliteFactory<T> satelliteFactory) {
        this.satelliteFactory = satelliteFactory;
    }

    @Override
    public MissionControlCenter.Connection<T> call(String key, Bundle bundle) {
        return new SingleConnection<>(key, satelliteFactory, bundle);
    }
}
