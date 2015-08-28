package satellite.connections;

import android.os.Bundle;

import satellite.MissionControlCenter;
import satellite.SatelliteFactory;

public class ReplayConnectionFactory<T> implements MissionControlCenter.ConnectionFactory<T> {

    private final SatelliteFactory<T> satelliteFactory;

    public ReplayConnectionFactory(SatelliteFactory<T> satelliteFactory) {
        this.satelliteFactory = satelliteFactory;
    }

    @Override
    public MissionControlCenter.Connection<T> call(String s, Bundle bundle) {
        return new ReplayConnection<>(s, satelliteFactory, bundle);
    }
}
