package com.example.rahin.fydp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class ChannelSettings extends ActionBarActivity {

    Button channel_number;
    Button privacy_number;
    ToggleButton CTCSS_DCS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_settings);

        final Button channel_up = (Button) findViewById(R.id.Channel_Up);
        final Button channel_down = (Button) findViewById(R.id.Channel_Down);
        channel_number = (Button) findViewById(R.id.channel_number);
        privacy_number = (Button) findViewById(R.id.privacy_number);
        CTCSS_DCS = (ToggleButton) findViewById(R.id.CTCSS_DCS);
        final Button privacy_up = (Button) findViewById(R.id.Privacy_Up);
        final Button privacy_down = (Button) findViewById(R.id.Privacy_Down);

        Intent intent = getIntent();
        String channel_number_text = intent.getStringExtra("Channel_Number");
        String privacy_number_text = intent.getStringExtra("Privacy_Number");
        String privacy_code_type = intent.getStringExtra("CTCSS_DCS");
        channel_number.setText(channel_number_text);
        privacy_number.setText(privacy_number_text);

        System.out.println("NO TYPE" + privacy_code_type);
        if (privacy_code_type.equals("CTCSS")) {
            System.out.println("CT WE IN HUR");
            CTCSS_DCS.setChecked(false);
        } else if (privacy_code_type.equals("DCS")){
            System.out.println("DC WE IN HUR");

            CTCSS_DCS.setChecked(true);
        }

        channel_up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int current = Integer.parseInt(channel_number.getText().toString());
                if (current < 22) {
                    current++;
                }

                channel_number.setText(Integer.toString(current));
            }
        });

        channel_down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int current = Integer.parseInt(channel_number.getText().toString());
                if (current > 1) {
                    current--;
                }
                channel_number.setText(Integer.toString(current));
            }
        });

        privacy_up.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int current = Integer.parseInt(privacy_number.getText().toString());
                boolean state = CTCSS_DCS.isChecked();
                int threshold;
                //DCS
                if (state) {
                    //Move to preferences
                    threshold = 83;
                } else {
                    //Move to preferences
                    threshold = 38;
                }

                if (current < threshold) {
                    current++;
                }
                privacy_number.setText(Integer.toString(current));
            }
        });

        privacy_down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int current = Integer.parseInt(privacy_number.getText().toString());
                if (current > 1) {
                    current--;
                }
                privacy_number.setText(Integer.toString(current));
            }
        });

        CTCSS_DCS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                privacy_number.setText("0");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_channel_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        String channel_number_text = channel_number.getText().toString();
        String privacy_number_text = privacy_number.getText().toString();
        String CTCSS_DCS_text = CTCSS_DCS.getText().toString();

        Intent intent = new Intent();
        System.out.println("NUMBA " + channel_number_text);
        intent.putExtra("Channel_number", channel_number_text);
        intent.putExtra("Privacy_number", privacy_number_text);
        intent.putExtra("CTCSS_DCS", CTCSS_DCS_text);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

}
