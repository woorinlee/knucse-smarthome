package com.lh089.smarthome04;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    String TAG = "MainActivity";
    UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    CancellationSignal cancellationSignal;
    private Context context;
    ConnectedThread connectedThread;


    public FingerprintHandler(Context context) {
        this.context = context;
    }

    //메소드들 정의
    public void startAutho(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        this.update("인증 에러 발생" + errString, false);
    }

    @Override
    public void onAuthenticationFailed() {
        this.update("인증 실패", false);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        this.update("Error: " + helpString, false);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        this.update("앱 접근이 허용되었습니다.", true);
    }

    public void stopFingerAuth() {
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
    }

    public void Call(ConnectedThread a) {

    }

    private void update(String s, boolean b) {
        final TextView tv_message = (TextView) ((Activity) context).findViewById(R.id.tempFingerPrintTv);

        //안내 메세지 출력
        tv_message.setText(s);

        if (b == false) {
            tv_message.setText("지문인증에 실패하였습니다.");

        } else {//지문인증 성공

            tv_message.setText("인증에 성공하였습니다.");
            connectedThread = ((MainActivity) MainActivity.context_main).connectedThread;
            if (connectedThread != null) {
                connectedThread.write("AAAA");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

}