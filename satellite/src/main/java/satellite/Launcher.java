package satellite;

import rx.Notification;
import rx.Observable;
import satellite.io.InputMap;

/**
 * This is the abstraction for the easier implementation of Android view classes that
 * are able to launch satellites.
 */
public interface Launcher {
    <T> Observable<Notification<T>> connection(int id, MissionControlCenter.ConnectionFactory<T> type);
    void launch(int id, InputMap missionStatement);
    void dismiss(int id);
}
