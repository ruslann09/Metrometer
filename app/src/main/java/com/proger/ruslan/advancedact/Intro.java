package com.proger.ruslan.advancedact;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class Intro extends AppCompatActivity {

    private final int REQUEST_CODE_PERMISSION_LOCATION = 10295;
    private final int REQUEST_CODE_PERMISSION_STORAGE = 34295;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        getSupportActionBar().hide();

        //сплэсш скрин для стабилизации приложения

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(Intro.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_LOCATION);
            }
        }, 500);

        accessAllowed();
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_have_permission_enabled), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

        accessAllowed();
    }

    //разрешение всех прав приложения

    private void accessAllowed () {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            long timeDelay = getSharedPreferences(getApplicationContext().getString(R.string.APP_PREFERENCES),
                    Context.MODE_PRIVATE).getBoolean("isInit", true) ? 400 : 850;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getSharedPreferences(getApplicationContext().getString(R.string.APP_PREFERENCES),
                            Context.MODE_PRIVATE).getBoolean("isInit", true)) {
                        startActivity(new Intent(getApplicationContext(), Introducing.class));
                    } else
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    finish();
                }
            }, timeDelay);
        }
    }
}
