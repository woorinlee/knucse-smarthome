package com.lh089.smarthome04;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView stateTv, searchTv;
    Button searchBtn, doorlockBtn, lightBtn, clockBtn, dlBtn;
    EditText dlEt;
    ListView pairedDeviceList, deviceList;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter, btPairedArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENEBLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;

    BottomNavigationView bottomNavigationView;
    View frame1, frame2, frame3, frame4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frame1 = (LinearLayout) findViewById(R.id.frame1);
        frame2 = (LinearLayout) findViewById(R.id.frame2);
        frame3 = (LinearLayout) findViewById(R.id.frame3);
        frame4 = (LinearLayout) findViewById(R.id.frame4);

        stateTv = (TextView) findViewById(R.id.stateTv);
        searchTv = (TextView) findViewById(R.id.searchTv);

        searchBtn = (Button) findViewById(R.id.searchBtn);
        doorlockBtn = (Button) findViewById(R.id.doorlockBtn);
        lightBtn = (Button) findViewById(R.id.lightBtn);
        clockBtn = (Button) findViewById(R.id.clockBtn);
        dlBtn = (Button) findViewById(R.id.dlBtn);

        dlEt = (EditText) findViewById(R.id.dlEt);

        pairedDeviceList = (ListView) findViewById(R.id.pairedDeviceList);
        deviceList = (ListView) findViewById(R.id.deviceList);

        btArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        btPairedArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        deviceAddressArray = new ArrayList<>();

        String[] permission_list = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, permission_list, 1);

        IntentFilter startFilter = new IntentFilter();
        startFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, startFilter);

        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND);
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, searchFilter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            startActivityForResult(enableBtIntent, REQUEST_ENEBLE_BT);
        }

        deviceList.setAdapter(btArrayAdapter);
        pairedDeviceList.setAdapter(btPairedArrayAdapter);
        deviceList.setOnItemClickListener(new myOnItemClickListener());
        pairedDeviceList.setOnItemClickListener(new myOnPairedItemClickListener());

        // 초기설정
        onSearchPairedBluetoothDevice();
        frame1.setVisibility(View.VISIBLE);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchPairedBluetoothDevice();
                searchBtn.setEnabled(false);
                searchTv.setVisibility(View.VISIBLE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    onBluetoothScanAccess();
                    return;
                }
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                btAdapter.startDiscovery();
            }
        });
        
        doorlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 리스트에서 도어락 이름의 BT 기기 연결
                // 열림 신호 전송
                // 지문인식 수행
                String dlName = "LHDR01";
                int dlNum = 0;
                TextView testTv = (TextView) findViewById(R.id.testTv);
                int tempArrayCount = btPairedArrayAdapter.getCount();
                for (int i = 0; i < tempArrayCount ; i++) {
                    if (btPairedArrayAdapter.getItem(i).equals(dlName)) {
                        dlNum = i;
                    }
                }
                testTv.setText(dlName + "의 번호는 " + Integer.toString(dlNum + 1) + "입니다.");
                searchDevice(dlNum, 1);
            }
        });
        dlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dlValue = dlEt.getText().toString();
                if (!dlValue.equals("")) {
                    dlValue = dlValue.replaceAll(" ", "");
                    Toast.makeText(MainActivity.this, dlValue, Toast.LENGTH_SHORT).show();
                    dlEt.setText("");
                    if (connectedThread != null) {
                        connectedThread.write(dlValue + "\n");
                    }
                }
            }
        });
        
        lightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "조명", Toast.LENGTH_SHORT).show();
                // 리스트에서 조명 이름의 BT 기기 연결
                // 작동 신호 전송
            }
        });
        
        clockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "시계", Toast.LENGTH_SHORT).show();
                // 리스트에서 시계 이름의 BT 기기 연결
                // 기기로부터 신호 전달받음
                // 전달받은 신호 TextView에 출력
            }
        });

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab1:
                        frame1.setVisibility(View.VISIBLE);
                        frame2.setVisibility(View.GONE);
                        frame3.setVisibility(View.GONE);
                        frame4.setVisibility(View.GONE);
                        return true;
                    case R.id.tab2:
                        // 도어락 기기 연결
                        frame1.setVisibility(View.GONE);
                        frame2.setVisibility(View.VISIBLE);
                        frame3.setVisibility(View.GONE);
                        frame4.setVisibility(View.GONE);
                        return true;
                    case R.id.tab3:
                        // 조명 기기 연결
                        frame1.setVisibility(View.GONE);
                        frame2.setVisibility(View.GONE);
                        frame3.setVisibility(View.VISIBLE);
                        frame4.setVisibility(View.GONE);
                        return true;
                    case R.id.tab4:
                        // 시계 기기 연결
                        frame1.setVisibility(View.GONE);
                        frame2.setVisibility(View.GONE);
                        frame3.setVisibility(View.GONE);
                        frame4.setVisibility(View.VISIBLE);
                        return true;
                }
                return false;
            }
        });
    }

    // 권한 관리 함수
    public void onBluetoothScanAccess() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스에 대한 액세스가 필요합니다");
        builder.setMessage("어플리케이션이 블루투스를 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
            }
        });
        builder.show();
    }

    public void onBluetoothConnectAccess() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스에 대한 액세스가 필요합니다");
        builder.setMessage("어플리케이션이 블루투스를 연결 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3);
            }
        });
        builder.show();
    }

    // 등록된 기기 목록에서 찾기 함수

    public void searchDevice(int position, int frameNum) {
        TextView tempStateTv = (TextView) findViewById(R.id.dlStateTv);
        switch (frameNum) {
            case 1:
                tempStateTv = (TextView) findViewById(R.id.dlStateTv);
            case 2:
                //tempStateTv = (TextView) findViewById(R.id.dlStateTv);
            case 3:
                //tempStateTv = (TextView) findViewById(R.id.dlStateTv);
        }

        final String name = btPairedArrayAdapter.getItem(position);
        final String address = deviceAddressArray.get(position);
        boolean flag = true;
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
            }
            btSocket.connect();
        } catch (IOException e) {
            flag = false;
            tempStateTv.setText("연결에 실패했습니다.");
            e.printStackTrace();
        }
        if (flag) {
            tempStateTv.setText(name + " 기기와 연결되었습니다.");
            connectedThread = new ConnectedThread(btSocket);
            connectedThread.start();
            onSearchPairedBluetoothDevice();
        }
    }

    // 초기 페어링 기기 출력 함수
    
    private void onSearchPairedBluetoothDevice() {
        btPairedArrayAdapter.clear();
        if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
            deviceAddressArray.clear();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            onBluetoothConnectAccess();
            return;
        }
        pairedDevices = btAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                btPairedArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
            }
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    btArrayAdapter.clear();
                    searchBtn.setText("검색 중..");
                    break;
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        onBluetoothConnectAccess();
                        return;
                    }
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    if (deviceName != null) {
                        btArrayAdapter.add(deviceName + "  (" + deviceHardwareAddress + ")");
                        deviceAddressArray.add(deviceHardwareAddress);
                        btArrayAdapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(MainActivity.this, "검색 완료", Toast.LENGTH_SHORT).show();
                    searchBtn.setText("검색");
                    searchBtn.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public class myOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this, "연결 중..", Toast.LENGTH_SHORT).show();
            final String name = btArrayAdapter.getItem(position);
            final String address = deviceAddressArray.get(position);
            boolean flag = true;
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                btSocket = createBluetoothSocket(device);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    onBluetoothConnectAccess();
                }
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
                stateTv.setText("대기 중");
                Toast.makeText(MainActivity.this, "연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            if (flag) {
                stateTv.setText(name + " 기기와 연결되었습니다.");
                connectedThread = new ConnectedThread(btSocket);
                connectedThread.start();
                onSearchPairedBluetoothDevice();
            }
        }
    }

    public class myOnPairedItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(MainActivity.this, "연결 중..", Toast.LENGTH_SHORT).show();
            final String name = btPairedArrayAdapter.getItem(position);
            final String address = deviceAddressArray.get(position);
            boolean flag = true;
            BluetoothDevice device = btAdapter.getRemoteDevice(address);
            try {
                btSocket = createBluetoothSocket(device);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    onBluetoothConnectAccess();
                }
                btSocket.connect();
            } catch (IOException e) {
                flag = false;
                stateTv.setText("연결에 실패했습니다.");
                e.printStackTrace();
            }
            if (flag) {
                stateTv.setText(name + " 기기와 연결되었습니다.");
                connectedThread = new ConnectedThread(btSocket);
                connectedThread.start();
                onSearchPairedBluetoothDevice();
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            onBluetoothScanAccess();
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
}