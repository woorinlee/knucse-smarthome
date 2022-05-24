package com.lh089.smarthome03;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Fragment2_1 extends Fragment {
    final static int BLUETOOTH_REQUEST_CODE = 1;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    TextView stateTv;
    Button searchBtn;
    ListView deviceList;
    /*List<Map<String, String>> deviceData;
    SimpleAdapter deviceDataAdapter;*/
    Set<BluetoothDevice> pairedDevice;
    ArrayAdapter<String> btArrayAdapter;
    ArrayList<String> deviceAddressArray;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2_1, container, false);

        stateTv = (TextView) view.findViewById(R.id.stateTv);
        searchBtn = (Button) view.findViewById(R.id.searchBtn);
        deviceList = (ListView) view.findViewById(R.id.deviceList);

        /*deviceData = new ArrayList<>();
        deviceDataAdapter = new SimpleAdapter(getActivity(), deviceData, android.R.layout.simple_list_item_2, new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
        deviceList.setAdapter(deviceDataAdapter);*/

        btArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_2, R.id.stateTv);
        btArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceAddressArray = new ArrayList<>();
        deviceList.setAdapter(btArrayAdapter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            stateTv.setText("블루투스를 지원하지 않는 단말기입니다.");
            searchBtn.setEnabled(false);
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        }

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                } else {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.startDiscovery();
                        btArrayAdapter.clear();
                        if (deviceAddressArray != null && !deviceAddressArray.isEmpty()) {
                            deviceAddressArray.clear();
                        }
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        Toast.makeText(getActivity(), "5", Toast.LENGTH_SHORT).show();
                        requireActivity().registerReceiver(receiver, filter);
                        Toast.makeText(getActivity(), "6", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "블루투스가 켜지지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return view;
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                btArrayAdapter.add(deviceName);
                deviceAddressArray.add(deviceHardwareAddress);
                btArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        //requireActivity().unregisterReceiver(receiver);
    }
}
