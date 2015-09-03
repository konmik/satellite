package satellite.example;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import satellite.example.cache.CacheConnectionActivity;
import satellite.example.replay.ReplayConnectionActivity;
import satellite.example.single.SingleConnectionActivity;

public class LauncherActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        findViewById(R.id.button_single).setOnClickListener(v -> startActivity(new Intent(LauncherActivity.this, SingleConnectionActivity.class)));
        findViewById(R.id.button_cache).setOnClickListener(v -> startActivity(new Intent(LauncherActivity.this, CacheConnectionActivity.class)));
        findViewById(R.id.button_replay).setOnClickListener(v -> startActivity(new Intent(LauncherActivity.this, ReplayConnectionActivity.class)));
    }
}
