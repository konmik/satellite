package satellite.example;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import satellite.ReconnectableMap;
import satellite.example.base.BaseActivity;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public abstract class BaseLaunchActivity extends BaseActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        findViewById(R.id.button_back).setOnClickListener(v -> onBackPressed());
    }

    @Override
    protected Subscription onConnect() {
        return Subscriptions.from(super.onConnect(),

            Observable.interval(500, 500, TimeUnit.MILLISECONDS, mainThread())
                .subscribe(ignored -> {
                    StringBuilder builder = new StringBuilder();
                    builder.append("connections:\n");
                    for (String key : ReconnectableMap.INSTANCE.keys())
                        builder.append(key).append("\n");
                    TextView report = (TextView)findViewById(R.id.stationReport);
                    report.setText(builder.toString());
                }));
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
