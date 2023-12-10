package com.example.my_bluetooth;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;

public class RSSIActivity extends Activity {

    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private int count = 0;
    private double average_rssi = 0.00;
    private ArrayList<Integer> locationStatusList = new ArrayList<>();
    private String finalLocationStatus = "Unknown";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi);

        // textView1을 layout XML 파일에서 정의한 ID로 변경
        TextView rssi_msg = findViewById(R.id.textView1);

        // Bluetooth 스캔 및 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RSSIActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Button boton = findViewById(R.id.button1);
        boton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                BTAdapter.startDiscovery();

                // 초기화
                count = 0;
                average_rssi = 0.00;
                finalLocationStatus = "Unknown";
                locationStatusList = new ArrayList<>();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater().inflate(R.menu.activity_rssi, menu);
        return true;
    }


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (count >= 5) {
                return;
            }
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = findViewById(R.id.textView1);

                if (name == "CA00208A0213-1A") {
                    locationStatusList.add(rssi);
                    count += 1;
                }

                if (count >= 5) {
                    if (locationStatusList == null || locationStatusList.isEmpty()) {
                        finalLocationStatus = "Unknown";
                    }

                    int sum = 0;
                    for (int value : locationStatusList) {
                        sum += value;
                    }

                    double average_rssi = (double) sum / locationStatusList.size();
                    if (average_rssi < - 80.00) {
                        finalLocationStatus = "현재 강의실 밖입니다.";
                    } else {
                        finalLocationStatus = "현재 강의실 안입니다.";
                    }
                }

                rssi_msg.setText(String.valueOf(average_rssi) + " : " + finalLocationStatus);
            }
        }
    };
}
