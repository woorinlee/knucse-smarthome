package com.lh089.smarthome05;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.SystemClock;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    BottomNavigationView bottomNavigationView;

    View frame1, frame2, frame3;
    TextView bluetoothStatusTv, dsStatusTv, lsStatusTv, tsStatusTv, dsFPTv;
    String dsName, lsName, tsName;
    Button dsFPBtn, lsUpBtn, lsDownBtn;

    TextView test31tv, test32tv, test33tv;
    Button test3btn;
    EditText test3et;
    Handler mBluetoothHandler3;
    BluetoothDevice mBluetoothDevice1, mBluetoothDevice2, mBluetoothDevice3;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket, mBluetoothSocket1, mBluetoothSocket2, mBluetoothSocket3;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    String roomInfo[];
    String roomInfoData[];
    String humi, temp, dust, infoResult;

    String TAG = "MainActivity";
    CancellationSignal cancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);

        frame1 = findViewById(R.id.frame1);
        frame2 = findViewById(R.id.frame2);
        frame3 = findViewById(R.id.frame3);

        bluetoothStatusTv = findViewById(R.id.bluetoothStatusTv);
        dsStatusTv = findViewById(R.id.dsStatusTv);
        dsFPTv = findViewById(R.id.dsFPTv);
        lsStatusTv = findViewById(R.id.lsStatusTv);
        tsStatusTv = findViewById(R.id.tsStatusTv);

        test31tv = findViewById(R.id.test31tv);
        test32tv = findViewById(R.id.test32tv);
        test33tv = findViewById(R.id.test33tv);

        dsFPBtn = findViewById(R.id.dsFPBtn);
        lsUpBtn = findViewById(R.id.lsUpBtn);
        lsDownBtn = findViewById(R.id.lsDownBtn);

        dsName = "LHDR01";
        lsName = "LHSS01";
        tsName = "LHTC01";

        setSupportActionBar(toolbar);
        frame1.setVisibility(View.VISIBLE);
        frame2.setVisibility(View.GONE);
        frame3.setVisibility(View.GONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mListPairedDevices = new ArrayList<>();

        if (mListPairedDevices != null) {
            mListPairedDevices.clear();
        }

        bluetoothOn();

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab1:
                        frame1.setVisibility(View.VISIBLE);
                        frame2.setVisibility(View.GONE);
                        frame3.setVisibility(View.GONE);
                        connectSelectedDevice1(dsName);
                        lsStatusTv.setText("?????? ????????? ?????? ??????");
                        tsStatusTv.setText("???????????? ????????? ?????? ??????");
                        return true;
                    case R.id.tab2:
                        frame1.setVisibility(View.GONE);
                        frame2.setVisibility(View.VISIBLE);
                        frame3.setVisibility(View.GONE);
                        connectSelectedDevice2(lsName);
                        dsStatusTv.setText("????????? ????????? ?????? ??????");
                        tsStatusTv.setText("???????????? ????????? ?????? ??????");
                        return true;
                    case R.id.tab3:
                        frame1.setVisibility(View.GONE);
                        frame2.setVisibility(View.GONE);
                        frame3.setVisibility(View.VISIBLE);
                        connectSelectedDevice3(tsName);
                        dsStatusTv.setText("????????? ????????? ?????? ??????");
                        lsStatusTv.setText("?????? ????????? ?????? ??????");
                        return true;
                }
                return false;
            }
        });

        dsFPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                    keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                    if(!fingerprintManager.isHardwareDetected()){ //Manifest??? Fingerprint ???????????? ????????? ?????? ????????????
                        dsFPTv.setText("????????? ????????? ??? ?????? ???????????? ?????????.");
                    } else if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                        dsFPTv.setText("??????????????? ????????? ?????????.");
                        /*???????????? ????????? ????????????.*/
                    } else if(!keyguardManager.isKeyguardSecure()){
                        dsFPTv.setText("??????????????? ????????? ?????????.");
                    } else if(!fingerprintManager.hasEnrolledFingerprints()){
                        dsFPTv.setText("????????? ????????? ????????????.");
                    } else {//?????? ????????? ??????????????? ??????(??????????????? ???????????? ?????? ????????? ???????????? ?????? ??????????????? ??????????????? ????????? ???????????? ?????????)
                        dsFPTv.setText("???????????? ???????????? ??? ?????????.");
                        generateKey();
                        if(cipherInit()){
                            cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            //???????????????
                            FingerprintHandler fingerprintHandler = new FingerprintHandler(MainActivity.this);
                            fingerprintHandler.startAuto(fingerprintManager, cryptoObject);
                        }
                    }
                }
            }
        });

        lsUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("SWSTUP");
                }
            }
        });
        lsDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("SWSTDW");
                }
            }
        });

        mBluetoothHandler3 = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (readMessage != "Receive" && readMessage != "" && readMessage != null) {
                        roomInfo = readMessage.split(",");

                        for (int i = 0; i < 3; i++) {
                            roomInfoData = roomInfo[i].split(":");
                        }
                        humi = roomInfo[0].split(":")[1];
                        temp = roomInfo[1].split(":")[1];
                        dust = roomInfo[2].split(":")[1];
                        infoResult = "?????? : " + humi + "%\n?????? : " + temp + "??C\n????????????(PM25) : " + dust + "???/???";
                        test31tv.setText("?????? : " + humi + "%");
                        test32tv.setText("?????? : " + temp + "??C");
                        test33tv.setText("????????????(PM25) : " + dust + "???/???");
                    }
                }
            }
        };
    }

    // ?????? ?????? ?????? ??? ?????? ??????
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_active:
                listPairedDevices();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    // ???????????? ?????? ?????? ??????
    public void onBluetoothScanAccess() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("??????????????? ?????? ???????????? ???????????????");
        builder.setMessage("????????????????????? ??????????????? ?????? ??? ??? ????????? ?????? ?????? ????????? ????????? ??????????????????.");
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
        builder.setTitle("??????????????? ?????? ???????????? ???????????????");
        builder.setMessage("????????????????????? ??????????????? ?????? ??? ??? ????????? ?????? ?????? ????????? ????????? ??????????????????.");
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

    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            bluetoothStatusTv.setText("??????????????? ???????????? ????????????.");
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                bluetoothStatusTv.setText("??????????????? ?????? ????????????.");
                getPairedDeviceList();
            } else {
                bluetoothStatusTv.setText("??????????????? ?????? ????????????.");
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    onBluetoothConnectAccess();
                    return;
                }
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
                bluetoothStatusTv.setText("??????????????? ?????? ????????????.");
                getPairedDeviceList();
            }
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler3.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }
        }
    }

    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("?????? ??????");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        Toast.makeText(MainActivity.this, items[item].toString(), Toast.LENGTH_SHORT).show();
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "???????????? ????????? ????????????.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    void getPairedDeviceList() {
        if (mBluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (mPairedDevices.size() > 0) {
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
            }
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            onBluetoothConnectAccess();
            return;
        }
        getName(selectedDeviceName);
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "???????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
        }
    }

    void connectSelectedDevice1(String selectedDeviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            onBluetoothConnectAccess();
            return;
        }
        getName1(selectedDeviceName);
        try {
            mBluetoothSocket1 = mBluetoothDevice1.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket1.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket1);
            mThreadConnectedBluetooth.start();
            //mBluetoothHandler1.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
            dsStatusTv.setText("????????? ???????????? ?????????????????????.");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "???????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
        }
    }

    void connectSelectedDevice2(String selectedDeviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            onBluetoothConnectAccess();
            return;
        }
        getName2(selectedDeviceName);
        try {
            mBluetoothSocket2 = mBluetoothDevice2.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket2.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket2);
            mThreadConnectedBluetooth.start();
            //mBluetoothHandler2.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
            lsStatusTv.setText("?????? ???????????? ?????????????????????.");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "???????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
        }
    }

    void connectSelectedDevice3(String selectedDeviceName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            onBluetoothConnectAccess();
            return;
        }
        getName3(selectedDeviceName);
        try {
            mBluetoothSocket3 = mBluetoothDevice3.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket3.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket3);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler3.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
            tsStatusTv.setText("???????????? ???????????? ?????????????????????.");
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "???????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
        }
    }

    void getName(String str) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            if (str.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
    }

    void getName1(String str) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            if (str.equals(tempDevice.getName())) {
                mBluetoothDevice1 = tempDevice;
                break;
            }
        }
    }
    void getName2(String str) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            if (str.equals(tempDevice.getName())) {
                mBluetoothDevice2 = tempDevice;
                break;
            }
        }
    }void getName3(String str) {
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                onBluetoothConnectAccess();
                return;
            }
            if (str.equals(tempDevice.getName())) {
                mBluetoothDevice3 = tempDevice;
                break;
            }
        }
    }

    // ???????????? ?????? ??????

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

    private class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
        private Context context;
        public FingerprintHandler(Context context) {
            this.context = context;
        }

        public void startAuto(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
            cancellationSignal = new CancellationSignal();
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            this.update("?????? ?????? ??????" + errString, false);
        }

        @Override
        public void onAuthenticationFailed() {
            this.update("?????? ??????", false);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            this.update("Error: " + helpString, false);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            this.update("??? ????????? ?????????????????????.", true);
        }

        public void stopFingerAuth() {
            if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                cancellationSignal.cancel();
            }
        }

        private void update(String s, boolean b) {
            //?????? ????????? ??????
            dsFPTv.setText(s);
            if (b == false) {
                dsFPTv.setText("??????????????? ?????????????????????.");
            } else {//???????????? ??????
                dsFPTv.setText("????????? ?????????????????????.");
                if(mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("AAAA");
                }
            }
        }
    }
}