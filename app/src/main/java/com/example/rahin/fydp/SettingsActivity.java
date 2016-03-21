package com.example.rahin.fydp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {

    ToggleButton useExternalAntenna;
    ToggleButton useRoger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        useExternalAntenna = (ToggleButton) findViewById(R.id.UseExternalAntenna);
        useRoger = (ToggleButton) findViewById(R.id.useRoger);
        useRoger.setChecked(getIntent().getBooleanExtra("useRoger", false));
        useExternalAntenna.setChecked(getIntent().getBooleanExtra("useExternalAntenna", false));
        Log.e("Use External Antenna: ", String.valueOf(useExternalAntenna.isChecked()));
        Log.e("Use Roger: ", String.valueOf(useRoger.isChecked()));


        useExternalAntenna.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("Use External Antenna: ", String.valueOf(useExternalAntenna.isChecked()));
            }
        });

        useRoger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("Use External Antenna: ", String.valueOf(useRoger.isChecked()));
            }
        });

    }

    @Override
    public void onBackPressed() {
        String useExternalAntennaState = useExternalAntenna.isChecked() ? "ANT1" : "ANT0";
        boolean useRogerState = useRoger.isChecked();
        Intent intent = new Intent();
        intent.putExtra("useExternalAntennaState", useExternalAntennaState);
        intent.putExtra("useExternalAntennaStateBool", useExternalAntenna.isChecked());
        intent.putExtra("useRoger", useRogerState);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

}
