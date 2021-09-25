package com.example.vivek;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        TextView tvAppCurrentVersion = (TextView) findViewById(R.id.tvAppCurrentVersion);
        TextView tvPolicy = (TextView) findViewById(R.id.tvPolicy);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.version));
            sb.append(" ");
            sb.append(packageInfo.versionName);
            tvAppCurrentVersion.setText(sb.toString());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}

