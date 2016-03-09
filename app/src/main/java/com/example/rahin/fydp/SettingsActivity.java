package com.example.rahin.fydp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {

    ToggleButton useExternalAntenna;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        useExternalAntenna = (ToggleButton) findViewById(R.id.UseExternalAntenna);

        Log.e("Use External Antenna: ", String.valueOf(useExternalAntenna.isChecked()));

        useExternalAntenna.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("Use External Antenna: ", String.valueOf(useExternalAntenna.isChecked()));
            }
        });

    }

    @Override
    public void onBackPressed() {
        String useExternalAntennaState = useExternalAntenna.isChecked() ? "ANT1" : "ANT0";

        Intent intent = new Intent();
        intent.putExtra("useExternalAntennaState", useExternalAntennaState);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

}
