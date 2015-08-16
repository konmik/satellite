package satellite.example;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.internal.util.SubscriptionList;
import satellite.MissionControlCenter;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public abstract class BaseLaunchActivity<T> extends AppCompatActivity {

    private MissionControlCenter<T> controlCenter;
    private boolean isFirstOnCreate = true;
    private boolean isFirstOnResume = true;
    private boolean isDestroyed = false;
    private SubscriptionList subscriptions = new SubscriptionList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlCenter = new MissionControlCenter<>(getSessionType(), savedInstanceState);
        isFirstOnCreate = savedInstanceState == null;
    }

    protected abstract MissionControlCenter.SessionType getSessionType();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        controlCenter.saveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        isDestroyed = true;
        super.onDestroy();
        subscriptions.unsubscribe();
        if (isFinishing())
            controlCenter.dismiss();
    }

    public boolean isFirstOnCreate() {
        return isFirstOnCreate;
    }

    public boolean isFirstOnResume() {
        return isFirstOnResume;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public MissionControlCenter<T> controlCenter() {
        return controlCenter;
    }

    public void add(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstOnResume) {
            add(Observable.interval(500, 500, TimeUnit.MILLISECONDS, mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long ignored) {
                        StringBuilder builder = new StringBuilder();
                        controlCenter().printSpaceStation(new StringBuilderPrinter(builder));
                        TextView report = (TextView)findViewById(R.id.stationReport);
                        report.setText(builder.toString());
                    }
                }));

            findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
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
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    protected void onNext(Integer value) {
        TextView result = (TextView)findViewById(R.id.result);
        result.setText(Integer.toString(value));

        ObjectAnimator.ofInt(result, "backgroundColor", Color.RED, Color.TRANSPARENT)
            .setDuration(500)
            .start();
    }
}
