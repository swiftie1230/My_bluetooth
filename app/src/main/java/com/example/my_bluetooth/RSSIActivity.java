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
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import java.util.ArrayList;

public class RSSIActivity extends Activity {

    private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private double average_rssi = 0.00;
    private ArrayList<Integer> locationStatusList = new ArrayList<>();
    private String finalLocationStatus = "현재 강의실 밖입니다.";

    private double outsideThreshold = -90.00;
    private double insideThreshold = -70.00;

    private final Handler handler = new Handler();
    private final Runnable stopDiscoveryRunnable = new Runnable() {
        @Override
        public void run() {
            stopBluetoothDiscovery();
        }
    };

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

                TextView rssi_msg = findViewById(R.id.textView1);

                // 브로드캐스트 리시버 등록
                registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

                // 초기화
                average_rssi = 0.00;
                finalLocationStatus = "현재 강의실 밖입니다.";
                locationStatusList = new ArrayList<>();

                BTAdapter.startDiscovery();

                // 5초 후에 스캔 중지 및 리시버 해제
                handler.postDelayed(stopDiscoveryRunnable, 5000);

                // 강의실 안인지 밖인지 판단
                if (locationStatusList == null || locationStatusList.isEmpty()) {
                    finalLocationStatus = "현재 강의실 밖입니다.";
                } else {
                    int sum = 0;
                    for (int value : locationStatusList) {
                        sum += value;
                    }

                    average_rssi = (double) sum / locationStatusList.size();
                    if (average_rssi < outsideThreshold) {
                        finalLocationStatus = "현재 강의실 밖입니다.";
                    } else if (average_rssi > insideThreshold) {
                        finalLocationStatus = "현재 강의실 안입니다.";
                    } else {
                        // wifi 코드 실행
                        finalLocationStatus = "현재 강의실 안입니다.";
                    }
                }

                rssi_msg.setText(String.valueOf(average_rssi) + " : " + finalLocationStatus);

            }
        });
    }

    // 브로드캐스트 리시버 해제 메서드
    private void stopBluetoothDiscovery() {
        unregisterReceiver(receiver);
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

                // CA00208A0213-1A 가 아닌 다른 블루투스 신호 제외
                if (name == null){
                    System.out.println("nono");
                    return;
                }

                // CA00208A0213-1A 블루투스 신호일 때
                if (name.equals("CA00208A0213-1A")) {
                    locationStatusList.add(rssi);
                    System.out.println("CA00208A0213-1A");
                }

                // 강의실 안인지 밖인지 판단
                /*
                if (locationStatusList == null || locationStatusList.isEmpty()) {
                    finalLocationStatus = "현재 강의실 밖입니다.";
                } else {
                    int sum = 0;
                    for (int value : locationStatusList) {
                        sum += value;
                    }

                    average_rssi = (double) sum / locationStatusList.size();
                    if (average_rssi < outsideThreshold) {
                        finalLocationStatus = "현재 강의실 밖입니다.";
                    } else if (average_rssi > insideThreshold) {
                        finalLocationStatus = "현재 강의실 안입니다.";
                    } else {
                        // wifi 코드 실행
                        finalLocationStatus = "현재 강의실 안입니다.";
                    }
                }

                rssi_msg.setText(String.valueOf(average_rssi) + " : " + finalLocationStatus);
                */
            }
        }
    };
}
