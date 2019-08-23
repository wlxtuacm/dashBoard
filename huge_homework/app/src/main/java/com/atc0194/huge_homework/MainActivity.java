package com.atc0194.huge_homework;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DashBoard d = (DashBoard) findViewById(R.id.dash);

        findViewById(R.id.rand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int max = 120;
                int min = 1;
                Random random = new Random();
                int p = random.nextInt(max) % (max - min + 1) + min;
                d.cgangePer(p / 120f);
                FlashHelper.getInstance().startFlick(findViewById(R.id.rand));
            }
        });

        findViewById(R.id.retu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.cgangePer(0);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
