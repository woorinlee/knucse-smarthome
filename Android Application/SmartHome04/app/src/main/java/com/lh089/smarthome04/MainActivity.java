package com.lh089.smarthome04;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    TextView stateTv, searchTv, dlFingerPrintTv;
    Button searchBtn, dlSendBtn, dlBtn, dlFingerPrintBtn, lightSendBtn, lightBtn, clockSendBtn, clockBtn;
    EditText dlEt, lightEt, clockEt;
    ListView pairedDeviceList, deviceList;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> pairedDevices;
    ArrayAdapter<String> btArrayAdapter, btPairedArrayAdapter;
    ArrayList<String> deviceAddressArray;

    private final static int REQUEST_ENEBLE_BT = 1;
    BluetoothSocket btSocket = null;
    ConnectedThread connectedThread;

    public static Context context_main;

    BottomNavigationView bottomNavigationView;
    View frame1, frame2, frame3, frame4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context_main = this;

        frame1 = findViewById(R.id.frame1);
        frame2 = findViewById(R.id.frame2);
        frame3 = findViewById(R.id.frame3);
        frame4 = findViewById(R.id.frame4);

        stateTv = findViewById(R.id.stateTv);
        searchTv = findViewById(R.id.searchTv);
        dlFingerPrintTv = findViewById(R.id.dlFingerPrintTv);

        searchBtn = findViewById(R.id.searchBtn);
        dlSendBtn = findViewById(R.id.dlSendBtn);
        dlBtn = findViewById(R.id.dlBtn);
        dlFingerPrintBtn = findViewById(R.id.dlFingerPrintBtn);
        lightSendBtn = findViewById(R.id.lightSendBtn);
        lightBtn = findViewById(R.id.lightBtn);
        clockSendBtn = findViewById(R.id.clockSendBtn);
        clockBtn = findViewById(R.id.clockBtn);

        dlEt = findViewById(R.id.dlEt);
        lightEt = findViewById(R.id.lightEt);
        clockEt = findViewById(R.id.clockEt);

        pairedDeviceList = findViewById(R.id.pairedDeviceList);
        deviceList = findViewById(R.id.deviceList);

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

        dlSendBtn.setOnClickListener(new View.OnClickListener() {
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
        dlFingerPrintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                    keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                    if(!fingerprintManager.isHardwareDetected()){ //Manifest에 Fingerprint 퍼미션을 추가해 워야 사용가능
                        dlFingerPrintTv.setText("지문을 사용할 수 없는 디바이스 입니다.");
                    } else if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                        dlFingerPrintTv.setText("지문사용을 허용해 주세요.");
                        /*잠금화면 상태를 체크한다.*/
                    } else if(!keyguardManager.isKeyguardSecure()){
                        dlFingerPrintTv.setText("잠금화면을 설정해 주세요.");
                    } else if(!fingerprintManager.hasEnrolledFingerprints()){
                        dlFingerPrintTv.setText("등록된 지문이 없습니다.");
                    } else {//모든 관문을 성공적으로 통과(지문인식을 지원하고 지문 사용이 허용되어 있고 잠금화면이 설정되었고 지문이 등록되어 있을때)
                        dlFingerPrintTv.setText("손가락을 홈버튼에 대 주세요.");
                        generateKey();
                        if(cipherInit()){
                            cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            //핸들러실행
                            FingerprintHandler fingerprintHandler = new FingerprintHandler(MainActivity.this);
                            fingerprintHandler.startAutho(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        });

        lightSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 리스트에서 조명 이름의 BT 기기 연결
                // 작동 신호 전송
                String lightName = "LHSS01";
                int lightNum = 0;
                TextView test2Tv = (TextView) findViewById(R.id.test2Tv);
                int tempArrayCount = btPairedArrayAdapter.getCount();
                for (int i = 0; i < tempArrayCount ; i++) {
                    if (btPairedArrayAdapter.getItem(i).equals(lightName)) {
                        lightNum = i;
                    }
                }
                test2Tv.setText(lightName + "의 번호는 " + Integer.toString(lightNum + 1) + "입니다.");
                searchDevice(lightNum, 1);
            }
        });
        lightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lightValue = lightEt.getText().toString();
                if (!lightValue.equals("")) {
                    lightValue = lightValue.replaceAll(" ", "");
                    Toast.makeText(MainActivity.this, lightValue, Toast.LENGTH_SHORT).show();
                    lightEt.setText("");
                    if (connectedThread != null) {
                        connectedThread.write(lightValue + "\n");
                    }
                }
            }
        });

        clockSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 리스트에서 시계 이름의 BT 기기 연결
                // 기기로부터 신호 전달받음
                // 전달받은 신호 TextView에 출력
                String clockName = "LHTC01";
                int clockNum = 0;
                TextView test3Tv = (TextView) findViewById(R.id.test3Tv);
                int tempArrayCount = btPairedArrayAdapter.getCount();
                for (int i = 0; i < tempArrayCount ; i++) {
                    if (btPairedArrayAdapter.getItem(i).equals(clockName)) {
                        clockNum = i;
                    }
                }
                test3Tv.setText(clockName + "의 번호는 " + Integer.toString(clockNum + 1) + "입니다.");
                searchDevice(clockNum, 1);
            }
        });
        clockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clockValue = clockEt.getText().toString();
                if (!clockValue.equals("")) {
                    clockValue = clockValue.replaceAll(" ", "");
                    Toast.makeText(MainActivity.this, clockValue, Toast.LENGTH_SHORT).show();
                    clockEt.setText("");
                    if (connectedThread != null) {
                        connectedThread.write(clockValue + "\n");
                    }
                }
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

    // 지문인식 관련 함수

    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }
        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e){
            throw new RuntimeException(e);
        }
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
        switch (frameNum) {
            case 1:
                TextView dlStateTv = (TextView) findViewById(R.id.dlStateTv);
                onConnectDevice(position, dlStateTv);
            case 2:
                TextView lightStateTv = (TextView) findViewById(R.id.lightStateTv);
                onConnectDevice(position, lightStateTv);
            case 3:
                TextView clockStateTv = (TextView) findViewById(R.id.clockStateTv);
                onConnectDevice(position, clockStateTv);
        }
    }

    // 블루투스 기기 연결 함수

    public void onConnectDevice(int position, TextView tv) {
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
            tv.setText("연결에 실패했습니다.");
            e.printStackTrace();
        }
        if (flag) {
            tv.setText(name + " 기기와 연결되었습니다.");
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