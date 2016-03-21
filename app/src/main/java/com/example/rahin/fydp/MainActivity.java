package com.example.rahin.fydp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteOrder;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.*;
import android.os.Process;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.widget.CompoundButton;
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
import android.widget.ToggleButton;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channel;
import java.util.ArrayList;
import java.util.Arrays;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.OggSpeexWriter;
import org.xiph.speex.SpeexEncoder;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    static public BluetoothSPP bt;
    SpeexEncoder mEncoder = new SpeexEncoder();
    private byte[] mBuffer;
    private AudioRecord mRecorder;
private AudioTrack mAudioPlayer;
    private boolean mIsRecording;
    private File mRawFile;
    private File mEncodedFile;
    boolean useRoger = false;
    boolean useExternalAntenna = false;
    byte[] buffer = new byte[320];


    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    Button channel_button;
    Button privacy_button;
    ToggleButton privacy_type_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Log.e("Permission", String.valueOf(checkWriteExternalPermission()));
        Log.e("HAHAHAHA", String.valueOf(mgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)));
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        channel_button = (Button) findViewById(R.id.channel_number);
        privacy_button = (Button) findViewById(R.id.privacy_number);
        privacy_type_button = (ToggleButton) findViewById(R.id.CTCSS_DCS);

        initRecorder();
        initEncoder();
        //Bluetooth -----------------------------
        bt = new BluetoothSPP(getApplicationContext());
        if (!bt.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, BluetoothState.REQUEST_CONNECT_DEVICE);
        } else {
            bluetooth_init();
        }

    channel_button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            Intent myIntent = new Intent(MainActivity.this, ChannelScroll.class);
            myIntent.putExtra("Channel_Number", channel_button.getText().toString());
            myIntent.putExtra("Privacy_Number", privacy_button.getText().toString());
            myIntent.putExtra("CTCSS_DCS", privacy_type_button.getText());
            myIntent.putExtra("Requester", "Channel");
            //Ew, refactor later.
            MainActivity.this.startActivityForResult(myIntent, 1);
        }
    });

        privacy_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, ChannelScroll.class);
                myIntent.putExtra("Channel_Number", channel_button.getText().toString());
                myIntent.putExtra("Privacy_Number", privacy_button.getText().toString());
                myIntent.putExtra("CTCSS_DCS", privacy_type_button.getText());
                myIntent.putExtra("Requester", "Privacy");
                MainActivity.this.startActivityForResult(myIntent, 1);
            }
        });

        privacy_type_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int channel_number = Integer.parseInt(channel_button.getText().toString());
                int privacy_code = Integer.parseInt(privacy_button.getText().toString());
                if (privacy_code > 38) {
                    privacy_code = 38;
                }
                privacy_button.setText("38");
                StringBuilder toSendBuilder = new StringBuilder();
                toSendBuilder.append("H");
                toSendBuilder.append("1");
                if (channel_number < 10) {
                    toSendBuilder.append("0");
                }
                toSendBuilder.append(Integer.toString(channel_number));
                if (privacy_type_button.getText().equals("DCS")) {
                    toSendBuilder.append("1");
                } else {
                    toSendBuilder.append("0");
                }

                if (privacy_code < 10) {
                    toSendBuilder.append("0");
                }
                toSendBuilder.append(Integer.toString(privacy_code));

                Log.e("sending", toSendBuilder.toString());
                bt.send("CMDSEQ", true);
                bt.send("COMMAND+SET", true);
                //TODO: Add blocking while ACK is not received, wait
                bt.send(toSendBuilder.toString(), true);
            }
        });


        button.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("NewApi")
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (!mIsRecording) {
                            mIsRecording = true;
                            mAudioPlayer.stop();
                            Log.e("RECORDING", "RECORDING");
                            bt.send("CMDSEQ", true);
                            bt.send("AUDIOS", true);
                            mRecorder.startRecording();
                            mRawFile = new File(Environment.getExternalStorageDirectory(), "rawAudio.raw");
                            startBufferedWrite(mRawFile);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (mIsRecording) {
                            mIsRecording = false;
                            mAudioPlayer.play();
                            mRecorder.stop();
                            /*Roger signal*/
                            try {
//                                InputStream is = getApplicationContext().openFileInput("/res/raw/roger.wav");
                                InputStream is = getResources().openRawResource(getResources().getIdentifier("roger", "raw", getPackageName()));
                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                byte[] test = new byte[1024];
//                                while (is.available() > 0) {
                                int bytesRead;
                                while ((bytesRead = is.read(test)) != -1){
//                                    int read = is.read();
//                                    bt.send(new byte[] {(byte)read}, false);
                                    bos.write(test, 0, bytesRead);
                                }
                                byte[] toOut = bos.toByteArray();
                                bt.send(toOut, false);
                            } catch (FileNotFoundException e) {
                                Log.e("FILE NOT FOUND", "FILE NOT FOUND");
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            bt.send("CMDSEQ", true);
                            bt.send("AUDSTP", true);
                            mEncodedFile = new File(Environment.getExternalStorageDirectory(), "encAudio.spx");
                            Log.e("Read", String.valueOf(mEncodedFile.canRead()));
                            FileInputStream fileInputStream = null;
                            Log.e("Test", "Test");
                            byte[] bFile = new byte[(int) mRawFile.length()];
                            Log.e("test", String.valueOf(mRawFile.length()));
                        }
                        return true;
                }
//                if (!mIsRecording) {
//                    mIsRecording = true;
//                    mAudioPlayer.stop();
//                    Log.e("RECORDING", "RECORDING");
//                    bt.send("CMDSEQ", true);
//                    bt.send("AUDIOS", true);
//                    mRecorder.startRecording();
//                    mRawFile = new File(Environment.getExternalStorageDirectory(), "rawAudio.raw");
//                    startBufferedWrite(mRawFile);
//                } else {
//                    mIsRecording = false;
//                    mAudioPlayer.play();
//                    mRecorder.stop();
//                    bt.send("CMDSEQ", true);
//                    bt.send("AUDSTP", true);
//                    mEncodedFile = new File(Environment.getExternalStorageDirectory(), "encAudio.spx");
//                    Log.e("Read", String.valueOf(mEncodedFile.canRead()));
//                    FileInputStream fileInputStream = null;
//                    Log.e("Test", "Test");
//                    byte[] bFile = new byte[(int) mRawFile.length()];
//                    Log.e("test", String.valueOf(mRawFile.length()));
//
//                    try {
//                        fileInputStream = new FileInputStream(mRawFile);
//                        fileInputStream.read(bFile);
//                        fileInputStream.close();
//                    } catch (Exception e) {
//                        Log.e("test", e.getMessage());
//                    }
//                    try {
//                        encodeFile(mRawFile, mEncodedFile);
//                    } catch (IOException e) {
//                        Log.e("test", e.getMessage());
//                    }
//                }
                return false;
            }
        });
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Intent newIntent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(newIntent, BluetoothState.REQUEST_CONNECT_DEVICE);
//                startActivity(newIntent);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                Toast.makeText(getApplicationContext(), "Found device " + device.getName(), Toast.LENGTH_LONG).show();
            }
        }
    };

    private void bluetooth_init() {
        bt.setupService(); // setup bluetooth service
        bt.startService(BluetoothState.DEVICE_OTHER); // start bluetooth service

        //Device MAC
        //bt.connect("20:FA:BB:02:1D:77");

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.e("BC127", "Connected to Bluetooth");
                //Prepare BC127 in DATA mode
                Log.e("SENT", "SENT");
                bt.send("CMDSEQ", true);
                bt.send("COMMAND+PAIR", true);
            }

            public void onDeviceDisconnected() {
                Log.e("Disconnected", "DEVICE HAS DISCONNECTED");
            }

            public void onDeviceConnectionFailed() {
                Log.e("FAILED", "Device has failed to connect");
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
//                Log.e("Data.len", String.valueOf(data.length));
                playAudio(data);
//                CharSequence text = received;
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(getApplicationContext(), text, duration);
//                toast.show();
            }
        });
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {

        }
        mRecorder.release();
        mAudioPlayer.release();
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
        if (id == R.id.choose_antenna) {
//            return true;
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            myIntent.putExtra("useRoger", useRoger);
            myIntent.putExtra("useExternalAntenna", useExternalAntenna);
            MainActivity.this.startActivityForResult(myIntent, 2);
        } else if (id == R.id.channel_settings) {
            Intent myIntent = new Intent(MainActivity.this, ChannelSettings.class);
            myIntent.putExtra("Channel_Number", channel_button.getText().toString());
            myIntent.putExtra("Privacy_Number", privacy_button.getText().toString());
            //Ew, refactor later.
            myIntent.putExtra("CTCSS_DCS", privacy_type_button.getText());
            MainActivity.this.startActivityForResult(myIntent,1);
        } else if (id == R.id.bluetooth_settings) {
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        BroadcastReceiver mReceiver;
            if (mBluetoothAdapter == null) {
                System.out.println("Not supported");
            }
            Intent newIntent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(newIntent, BluetoothState.REQUEST_CONNECT_DEVICE);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        //THIS IS CURRENTLY NOT BEING USED AS WE DON'T HAVE A BT LIST
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                if (data != null)
                    bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                Log.e("BLAH", "BLAH");
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.e("Channel Settings", "Returned from Channel Settings");
                channel_button.setText(data.getStringExtra("Channel_number"));
                int channel_number = Integer.parseInt(data.getStringExtra("Channel_number"));
                int privacy_code = Integer.parseInt(data.getStringExtra("Privacy_number"));
                String isCTCSS = data.getStringExtra("CTCSS_DCS");
                StringBuilder toSendBuilder = new StringBuilder();
                toSendBuilder.append("H");
                toSendBuilder.append("1");
                if (channel_number < 10) {
                    toSendBuilder.append("0");
                }
                toSendBuilder.append(Integer.toString(channel_number));
                if (isCTCSS.equals("CTCSS")) {
                    toSendBuilder.append("1");
                } else {
                    toSendBuilder.append("0");
                }

                if (privacy_code < 10) {
                    toSendBuilder.append("0");
                }
                toSendBuilder.append(Integer.toString(privacy_code));

                Log.e("sending", toSendBuilder.toString());
                bt.send("CMDSEQ", true);
                bt.send("COMMAND+SET", true);
                //TODO: Add blocking while ACK is not received, wait
                bt.send(toSendBuilder.toString(), true);
                privacy_button.setText(data.getStringExtra("Privacy_number"));
            }
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                String state = data.getStringExtra("useExternalAntennaState");
                useRoger = data.getBooleanExtra("useRoger", false);
                useExternalAntenna = data.getBooleanExtra("useExternalAntennaStateBool", false);
                bt.send("CMDSEQ", true);
                bt.send(state, true);
            }
        }
    }

    private void initRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        Log.e("buffer", String.valueOf(bufferSize));
        mBuffer = new byte[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        int minSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        Log.e("BUFFER SIZE", String.valueOf(minSize));
//        mAudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 7900, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, minSize * 9, AudioTrack.MODE_STREAM);
        mAudioPlayer = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, minSize * 4, AudioTrack.MODE_STREAM);
        mAudioPlayer.setVolume(1.0f);
        mAudioPlayer.play();
//        mAudioPlayer.setVolume(0.2f);
    }

    private void initEncoder() {
        mEncoder = new SpeexEncoder();
        mEncoder.init(0, 8, 8000, 1);
    }

    private void startBufferedWrite(final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DataOutputStream output = null;

                try {
                    output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
//                    if (mAudioPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING)
//                        mAudioPlayer.play();
                    while (mIsRecording) {
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                            bt.send(mBuffer, false);
//                            mAudioPlayer.write(mBuffer,0,mBuffer.length);
                    }
                } catch (IOException e) {
                    Log.e("IOException", e.getMessage());
                } finally {
                    if (output != null) {
                        try {
                            output.flush();
                        } catch (IOException e) {
                            Log.e("IOException", e.getMessage());
                        } finally {
                            try {
                                output.close();
                            } catch (IOException e) {
                                Log.e("IOException", e.getMessage());
                            }
                        }
                    }
                }
            }
        }).start();
    }

    private void encodeFile(final File inputFile, final File outputFile) throws IOException {
        DataInputStream input = null;
        AudioFileWriter output = null;
        try {
            input = new DataInputStream(new FileInputStream(inputFile));
            output = new OggSpeexWriter(0, 8000, 1, 1, false);
            output.open(outputFile);
            output.writeHeader("Encoded with: " + SpeexEncoder.VERSION);

            byte[] buffer = new byte[2560]; // 2560 is the maximum needed value (stereo UWB)
            int packetSize = 2 * 1 * mEncoder.getFrameSize();

            while (true) {
                input.readFully(buffer, 0, packetSize);
                mEncoder.processData(buffer, 0, packetSize);
                int encodedBytes = mEncoder.getProcessedData(buffer, 0);
                if (encodedBytes > 0) {
                    output.writePacket(buffer, 0, encodedBytes);
                }
            }
        } catch (EOFException e) {
            // This exception just provides exit from the loop
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        }
    }

    public void playAudio(byte[] data) {
        final byte[] data_cpy = data;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
//        Log.e("In PlayAudio", "");
//        mAudioPlayer.play();

        mAudioPlayer.write(data_cpy, 0, data_cpy.length);
//                mAudioPlayer.write(data_cpy, 0, data_cpy.length);
//                mAudioPlayer.stop();
            }
        });
        t.start();
    }


    private boolean checkWriteExternalPermission()
    {
        String permission = "android.permission.RECORD_AUDIO";
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            //Do something
            if (!mIsRecording) {
                mIsRecording = true;
                mAudioPlayer.stop();
                Log.e("RECORDING", "RECORDING");
                bt.send("CMDSEQ", true);
                bt.send("AUDIOS", true);
                mRecorder.startRecording();
                mRawFile = new File(Environment.getExternalStorageDirectory(), "rawAudio.raw");
                startBufferedWrite(mRawFile);
                return true;
            }
        }
//        return super.onKeyDown(keyCode, event);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            if (mIsRecording) {
                mIsRecording = false;
                mAudioPlayer.play();
                mRecorder.stop();
                if (useRoger) {
                    try {
//                                InputStream is = getApplicationContext().openFileInput("/res/raw/roger.wav");
                        InputStream is = getResources().openRawResource(getResources().getIdentifier("roger", "raw", getPackageName()));
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] test = new byte[1024];
//                                while (is.available() > 0) {
                        int bytesRead;
                        while ((bytesRead = is.read(test)) != -1) {
//                                    int read = is.read();
//                                    bt.send(new byte[] {(byte)read}, false);
                            bos.write(test, 0, bytesRead);
                        }
                        byte[] toOut = bos.toByteArray();
                        bt.send(toOut, false);
                    } catch (FileNotFoundException e) {
                        Log.e("FILE NOT FOUND", "FILE NOT FOUND");
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                bt.send("CMDSEQ", true);
                bt.send("AUDSTP", true);
                mEncodedFile = new File(Environment.getExternalStorageDirectory(), "encAudio.spx");
                Log.e("Read", String.valueOf(mEncodedFile.canRead()));
                FileInputStream fileInputStream = null;
                Log.e("Test", "Test");
                byte[] bFile = new byte[(int) mRawFile.length()];
                Log.e("test", String.valueOf(mRawFile.length()));
            }
        }
        return true;
    }
}
