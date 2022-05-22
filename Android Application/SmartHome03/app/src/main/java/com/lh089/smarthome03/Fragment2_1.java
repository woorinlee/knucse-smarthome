package com.lh089.smarthome03;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Fragment2_1 extends Fragment {
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
        View view = inflater.inflate(R.layout.fragment_2_1, container, false);

        stateTv = (TextView) view.findViewById(R.id.stateTv);
        searchBtn = (Button) view.findViewById(R.id.searchBtn);
        deviceList = (ListView) view.findViewById(R.id.deviceList);

        deviceData = new ArrayList<>();
        deviceDataAdapter = new SimpleAdapter(getActivity(), deviceData, android.R.layout.simple_list_item_2, new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
        deviceList.setAdapter(deviceDataAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            stateTv.setText("블루투스를 지원하지 않는 단말기입니다.");
            searchBtn.setEnabled(false);
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        }
        return view;
    }
}
