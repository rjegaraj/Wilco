package com.example.rahin.fydp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import java.nio.ByteOrder;
import android.content.BroadcastReceiver;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
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
import android.os.SystemClock;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.OggSpeexWriter;
import org.xiph.speex.SpeexEncoder;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    BluetoothSPP bt;
    SpeexEncoder mEncoder = new SpeexEncoder();
    private byte[] mBuffer;
    private AudioRecord mRecorder;
    private boolean mIsRecording;
    private File mRawFile;
    private File mEncodedFile;


    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    Button channel_button;
    Button privacy_button;
    TextView privacy_code_type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button button = (Button) findViewById(R.id.button);
        channel_button = (Button) findViewById(R.id.channel_number);
        privacy_button = (Button) findViewById(R.id.privacy_number);
        privacy_code_type = (TextView) findViewById(R.id.Privacy_Code_Type);

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
//                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//                int event1 = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
//                int event2 = KeyEvent.KEYCODE_MEDIA_PLAY;
//
//                if (mAudioManager.isMusicActive()) {
//                    System.out.println("TEST");
//
//                    long eventtime = SystemClock.uptimeMillis() - 1;
//                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, event1, 0);
//                    mAudioManager.dispatchMediaKeyEvent(downEvent);
//
//                    eventtime++;
//                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, event1, 0);
//                    mAudioManager.dispatchMediaKeyEvent(upEvent);
//                } else {
//
//                    long eventtime = SystemClock.uptimeMillis() - 1;
//                    KeyEvent downEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_DOWN, event2, 0);
//                    mAudioManager.dispatchMediaKeyEvent(downEvent);
//
//                    eventtime++;
//                    KeyEvent upEvent = new KeyEvent(eventtime, eventtime, KeyEvent.ACTION_UP, event2, 0);
//                    mAudioManager.dispatchMediaKeyEvent(upEvent);
//
//                }
                if (!mIsRecording) {
                    mIsRecording = true;
                    mRecorder.startRecording();
                    mRawFile = new File(Environment.getExternalStorageDirectory(), "rawAudio.raw");
                    Log.e("IN RECORD", "IN RECORD");
                    startBufferedWrite(mRawFile);
                }
                else {
                    mIsRecording = false;
                    mRecorder.stop();
                    mEncodedFile = new File(Environment.getExternalStorageDirectory(), "encAudio.spx");
                    Log.e("Read", String.valueOf(mEncodedFile.canRead()));
                    FileInputStream fileInputStream=null;
                    Log.e("Test", "Test");
                    byte[] bFile = new byte[(int) mRawFile.length()];
                    Log.e("test", String.valueOf(mRawFile.length()));



                        ShortBuffer intBuf = ByteBuffer.wrap(bFile).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                        short[] samples16Bit = new short[intBuf.remaining()];
                        intBuf.get(samples16Bit);
                        byte[] data1 = new byte[samples16Bit.length];
                        for (int i = 0; i < samples16Bit.length; i++) {
                            data1[i] = (byte)((samples16Bit[i] / 256)+128);
                        }

//                    try {
//                        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT, data1.length, AudioTrack.MODE_STATIC);
//                        audioTrack.write(data1, 0, data1.length);
//                    } catch (Exception e) {
//
//                    }
                    try {
                        fileInputStream = new FileInputStream(mRawFile);
                        fileInputStream.read(bFile);
                        fileInputStream.close();

                        bt.send(bFile, true);
                    } catch (Exception e) {
                        Log.e("test", e.getMessage());
                    }
                    try {
                        encodeFile(mRawFile, mEncodedFile);
                    } catch (IOException e) {
                        Log.e("test", e.getMessage());
                    }
                }
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
        bt.connect("20:FA:BB:02:1D:77");

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.e("BC127", "Connected to Bluetooth");
                //Prepare BC127 in DATA mode
                Log.e("SENT", "SENT");
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
                String received = new String(data);
                Log.e("Received Data", received);
                //Will be getting ACKs prefixed with type. Can set global states to determine what can be transmitted to bt
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        mRecorder.release();
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
                StringBuilder toSendBuilder = new StringBuilder();
                toSendBuilder.append("H");
                toSendBuilder.append("1");
                toSendBuilder.append(Integer.toString(channel_number));
                toSendBuilder.append(Integer.toString(privacy_code));
                Log.e("sending", toSendBuilder.toString());
                bt.send("COMMAND+SET", true);
                //TODO: Add blocking while ACK is not received, wait
                bt.send(toSendBuilder.toString(), true);
                privacy_button.setText(data.getStringExtra("Privacy_number"));
                privacy_code_type.setText("Privacy Code Type: " + data.getStringExtra("CTCSS_DCS"));
            }
        }
    }

    private void initRecorder() {
        int bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mBuffer = new byte[bufferSize];
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);
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
                    while (mIsRecording) {
                        int readSize = mRecorder.read(mBuffer, 0, mBuffer.length);
                        for (int i = 0; i < readSize; i++) {
                            output.writeByte(mBuffer[i]);
                        }
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

//        private File getFile(final String suffix) {
////            Time time = new Time();
////            time.setToNow();
////            return new File(Environment.getExternalStorageDirectory(), time.format("%Y%m%d%H%M%S") + "." + suffix);
//        }

    }
