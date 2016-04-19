package com.example.rahin.fydp;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ChannelScroll extends AppCompatActivity {
    String requester;
    String channel_number;
    String privacy_code;
    String privacy_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_scroll);
        Intent intent = getIntent();
        requester = intent.getStringExtra("Requester");
        channel_number = intent.getStringExtra("Channel_Number");
        privacy_code = intent.getStringExtra("Privacy_Number");
        privacy_type = intent.getStringExtra("CTCSS_DCS");
        final ListView listview = (ListView) findViewById(R.id.listView);
        ArrayList<String> values = new ArrayList<String>();
        if (requester.equals("Channel")) {
            for (int i = 1; i < 23; i++) {
                getSupportActionBar().setTitle(requester);
                values.add(String.valueOf(i));
            }
        } else if (privacy_type.equals("CTCSS")) {
            getSupportActionBar().setTitle(privacy_type);
            for (int i = 0; i < 39; i++) {
                values.add(String.valueOf(i));
            }
        } else {
            getSupportActionBar().setTitle(privacy_type);
            for (int i = 0; i < 84; i++) {
                values.add(String.valueOf(i));
            }
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setAdapter(adapter);
        listview.setItemsCanFocus(false);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView selected = ((TextView)view);
                if (requester.equals("Channel")) {
                    channel_number = selected.getText().toString();
                } else {
                    privacy_code = selected.getText().toString();
                }
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("Channel_number", channel_number);
        intent.putExtra("Privacy_number", privacy_code);
        intent.putExtra("CTCSS_DCS", privacy_type);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

}
