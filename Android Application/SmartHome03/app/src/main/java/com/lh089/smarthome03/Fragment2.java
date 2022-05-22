package com.lh089.smarthome03;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fragment2 extends Fragment {

    //블루투스 요청 액티비티 코드
    final static int BLUETOOTH_REQUEST_CODE = 100;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    TextView stateTv;
    Button searchBtn;
    ListView deviceList;
    List<Map<String, String>> deviceData;
    SimpleAdapter deviceDataAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, container, false);
        // ct = container.getContext();

        stateTv = (TextView) view.findViewById(R.id.stateTv);
        searchBtn = (Button) view.findViewById(R.id.searchBtn);
        deviceList = (ListView) view.findViewById(R.id.deviceList);

        deviceData = new ArrayList<>();
        deviceDataAdapter = new SimpleAdapter(getActivity(), deviceData, android.R.layout.simple_list_item_2, new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
        deviceList.setAdapter(deviceDataAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 블루투스 미지원 시 버튼 비활성화
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "블루투스를 지원하지 않는 단말기 입니다.", Toast.LENGTH_SHORT).show();
            searchBtn.setEnabled(false);
        }

        // 블루투스 꺼져있을 경우 활성화 요청
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        }

        return view;
    }
}