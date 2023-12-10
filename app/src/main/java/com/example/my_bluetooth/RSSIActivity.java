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

public class RSSIActivity extends Activity {

    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssi);

        // textView1을 layout XML 파일에서 정의한 ID로 변경
        TextView rssi_msg = findViewById(R.id.textView1);

        // Bluetooth 스캔 및 위치 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RSSIActivity.this,
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Button boton = findViewById(R.id.button1);
        boton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(RSSIActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                BTAdapter.startDiscovery();
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
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                TextView rssi_msg = findViewById(R.id.textView1);
                rssi_msg.setText(rssi_msg.getText() + name + " => " + rssi + "dBm\n");
            }
        }
    };
}
