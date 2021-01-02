package es.ewic.clients;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Set;

import es.ewic.clients.adapters.DeviceRowAdapter;
import es.ewic.clients.model.Client;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccessShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccessShopFragment extends Fragment {

    private static final String ARG_CLIENT = "client";
    private static final int BLUETOOTH_REQUEST_CODE = 01;

    private Client client;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private ListView devices_list;
    private Set<BluetoothDevice> devices;
    private DeviceRowAdapter mDeviceRowAdapter;

    public AccessShopFragment() {
        // Required empty public constructor
    }

    public static AccessShopFragment newInstance(Client clientData) {
        AccessShopFragment fragment = new AccessShopFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CLIENT, clientData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            client = (Client) getArguments().getSerializable(ARG_CLIENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ConstraintLayout parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_access_shop, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.access_shop);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        requestActivateBluetooth();

        devices_list = parent.findViewById(R.id.devices_list);
        devices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice clickedDevice = mDeviceRowAdapter.getItem(position);
                if (clickedDevice != null) {

                }
            }
        });


        return parent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //Find elements
                searchBluetoothDevices();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Snackbar snackbar = Snackbar.make(getView(), getString(R.string.bluetooth_needed_message), Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(R.string.activate, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        requestActivateBluetooth();
                    }
                });
                snackbar.show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private void requestActivateBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
    }

    private void addNewDevice(BluetoothDevice device) {
        mDeviceRowAdapter.addItem(device);
        devices_list.setAdapter(mDeviceRowAdapter);
    }

    private void searchBluetoothDevices() {
        devices = mBluetoothAdapter.getBondedDevices();
        Log.e("BLUETOOTH", "Bonded devices: " + devices.size());
        mDeviceRowAdapter = new DeviceRowAdapter(new ArrayList<>(devices), AccessShopFragment.this, getResources(), getActivity().getPackageName());
        devices_list.setAdapter(mDeviceRowAdapter);

        mBluetoothAdapter.startDiscovery();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.e("BLUETOOTH", "Encontrado: " + device.getName());
                    addNewDevice(device);
                }
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }
}