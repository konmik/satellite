package info.android15.valuemap.benchmark;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import valuemap.ParcelFnBenchmark;

public class MainActivity extends AppCompatActivity {

    public static final int ITERATIONS = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                benchmark();
            }
        });
    }

    private void benchmark() {

        TextView result = (TextView)findViewById(R.id.result);

        long time0 = System.nanoTime() / 1000000;
        for (int i = 0; i < ITERATIONS; i++)
            ParcelFnBenchmark.testNoop();

        printResult("noop", time0, result);

        long time1 = System.nanoTime() / 1000000;
        for (int i = 0; i < ITERATIONS; i++)
            ParcelFnBenchmark.testString();

        printResult("string", time1, result);

        long time2 = System.nanoTime() / 1000000;
        for (int i = 0; i < ITERATIONS; i++)
            ParcelFnBenchmark.testInteger();

        printResult("integer", time2, result);

        long time3 = System.nanoTime() / 1000000;
        for (int i = 0; i < ITERATIONS; i++)
            ParcelFnBenchmark.testCombinedMap();

        printResult("combinedMap", time3, result);

        long time4 = System.nanoTime() / 1000000;
        for (int i = 0; i < ITERATIONS; i++)
            ParcelFnBenchmark.testJavaMap();

        printResult("javaMap", time4, result);

        long time5 = System.nanoTime() / 1000000;
        for (int i = 0; i < ITERATIONS; i++)
            ParcelFnBenchmark.testImmutableMap();

        printResult("immutableMap", time5, result);
    }

    private void printResult(String prefix, long time1, TextView result) {
        long timeTotal = System.nanoTime() / 1000000 - time1;
        long nsPerIteration = timeTotal * 1000000 / ITERATIONS;
        result.setText(result.getText().toString() + "\n" +
            prefix + ": " + nsPerIteration + " ns");
    }
}
