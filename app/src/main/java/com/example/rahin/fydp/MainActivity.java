package com.example.rahin.fydp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import android.view.KeyEvent;
import java.nio.channels.Channel;
import java.util.ArrayList;
import android.os.SystemClock;



public class MainActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

    Button channel_button;
    Button privacy_button;
    TextView privacy_code_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Test");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        channel_button = (Button) findViewById(R.id.channel_number);
        privacy_button = (Button) findViewById(R.id.privacy_number);
        privacy_code_type = (TextView) findViewById(R.id.Privacy_Code_Type);
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BroadcastReceiver mReceiver;
        if (mBluetoothAdapter == null) {
            System.out.println("Not supported");
        }

        channel_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ChannelSettings.class);
                myIntent.putExtra("Channel_Number", channel_button.getText().toString());
                myIntent.putExtra("Privacy_Number", privacy_button.getText().toString());
                //Ew, refactor later.
                myIntent.putExtra("CTCSS_DCS", privacy_code_type.getText().toString().substring(19, privacy_code_type.getText().toString().length()));
                MainActivity.this.startActivityForResult(myIntent, 1);
            }
        });

        privacy_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ChannelSettings.class);
                myIntent.putExtra("Channel_Number", channel_button.getText().toString());
                myIntent.putExtra("Privacy_Number", privacy_button.getText().toString());
                //Ew, refactor later.
                myIntent.putExtra("CTCSS_DCS", privacy_code_type.getText().toString().substring(19, privacy_code_type.getText().toString().length()));
                MainActivity.this.startActivityForResult(myIntent, 1);
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            public void onClick(View v) {
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int event1 = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
                int event2 = KeyEvent.KEYCODE_MEDIA_PLAY;

                if (mAudioManager.isMusicActive()) {
                    System.out.println("TEST");

                    long eventtime = SystemClock.uptimeMillis() - 1;
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, event1, 0);
                    mAudioManager.dispatchMediaKeyEvent(downEvent);

                    eventtime++;
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, event1, 0);
                    mAudioManager.dispatchMediaKeyEvent(upEvent);
                } else {

                    long eventtime = SystemClock.uptimeMillis() - 1;
                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, event2, 0);
                    mAudioManager.dispatchMediaKeyEvent(downEvent);

                    eventtime++;
                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, event2, 0);
                    mAudioManager.dispatchMediaKeyEvent(upEvent);

                }
            }
        });
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(newIntent);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                Toast.makeText(getApplicationContext(), "Found device " + device.getName(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
        } else if (id == R.id.channel_settings) {
            Intent myIntent = new Intent(MainActivity.this, ChannelSettings.class);
            myIntent.putExtra("Channel_Number", channel_button.getText().toString());
            myIntent.putExtra("Privacy_Number", privacy_button.getText().toString());
            //Ew, refactor later.
            myIntent.putExtra("CTCSS_DCS", privacy_code_type.getText().toString().substring(19, privacy_code_type.getText().toString().length()));
            MainActivity.this.startActivityForResult(myIntent,1);
        } else if (id == R.id.bluetooth_settings) {
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BroadcastReceiver mReceiver;
            if (mBluetoothAdapter == null) {
                System.out.println("Not supported");
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                mBluetoothAdapter.startDiscovery();
                mDeviceList.clear();
                registerReceiver(mReceiver, filter);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                channel_button.setText(data.getStringExtra("Channel_number"));
                privacy_button.setText(data.getStringExtra("Privacy_number"));
                privacy_code_type.setText("Privacy Code Type: " + data.getStringExtra("CTCSS_DCS"));
            }
        }
    }


    }
