package satellite.example;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.internal.util.SubscriptionList;
import satellite.ReconnectableMap;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public abstract class BaseLaunchActivity extends AppCompatActivity {

    private boolean isFirstOnResume = true;
    private boolean isDestroyed = false;
    private SubscriptionList unsubscribeOnDestroy = new SubscriptionList();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        findViewById(R.id.button_back).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
        unsubscribeOnDestroy.unsubscribe();
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public void unsubscribeOnDestroy(Subscription subscription) {
        unsubscribeOnDestroy.add(subscription);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstOnResume) {
            onCreateConnections();
            isFirstOnResume = false;
        }
    }

    protected void onCreateConnections() {
        unsubscribeOnDestroy(Observable.interval(500, 500, TimeUnit.MILLISECONDS, mainThread())
            .subscribe(ignored -> {
                StringBuilder builder = new StringBuilder();
                builder.append("connections:\n");
                for (String key : ReconnectableMap.INSTANCE.keys())
                    builder.append(key).append("\n");
                TextView report = (TextView)findViewById(R.id.stationReport);
                report.setText(builder.toString());
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstOnResume = false;
    }

    protected void log(String message) {
        TextView textView = (TextView)findViewById(R.id.textView);
        String text = textView.getText().toString();
        textView.setText(text + (text.length() == 0 ? "" : "\n") + message);
        final ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    protected void onNext(Integer value) {
        TextView result = (TextView)findViewById(R.id.result);
        result.setText(Integer.toString(value));

        ObjectAnimator.ofInt(result, "backgroundColor", Color.RED, Color.TRANSPARENT)
            .setDuration(500)
            .start();
    }
}
