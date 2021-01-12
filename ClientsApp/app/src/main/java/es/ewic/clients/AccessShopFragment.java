package es.ewic.clients;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.ewic.clients.adapters.DeviceRowAdapter;
import es.ewic.clients.model.Client;
import es.ewic.clients.utils.FormUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccessShopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccessShopFragment extends Fragment {

    private static final String ARG_CLIENT = "client";
    private static final int BLUETOOTH_REQUEST_CODE = 01;

    private ConstraintLayout parent;
    private ProgressDialog pd;
    private Client client;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private ListView bonded_devices_list;
    private ListView new_devices_list;
    private List<BluetoothDevice> bonded_devices;
    private Set<BluetoothDevice> new_devices;
    private DeviceRowAdapter mDeviceRowAdapter;
    private TextView bluetooth_shop_name;

    private BlueeToothConectionTask mBlueeToothConectionTask;

    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Handler handler;

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }

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
        parent = (ConstraintLayout) inflater.inflate(R.layout.fragment_access_shop, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.access_shop);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        requestActivateBluetooth();

        bonded_devices_list = parent.findViewById(R.id.bonded_devices_list);
        bonded_devices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.bluetooth_connecting_device, R.string.please_wait);
                BluetoothDevice clickedDevice = bonded_devices.get(position);
                if (clickedDevice != null) {
                    connectToBluetoothServer(clickedDevice);
                }
            }
        });

        new_devices_list = parent.findViewById(R.id.new_devices_list);
        new_devices_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pd = FormUtils.showProgressDialog(getContext(), getResources(), R.string.bluetooth_connecting_device, R.string.please_wait);
                BluetoothDevice clickedDevice = mDeviceRowAdapter.getItem(position);
                if (clickedDevice != null) {
                    connectToBluetoothServer(clickedDevice);
                }
            }
        });

        bluetooth_shop_name = parent.findViewById(R.id.bluetooth_shop_name);

        Button bluetooth_exit_shop = parent.findViewById(R.id.bluetooth_exit_shop);
        bluetooth_exit_shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("BLUETOOTH", "Try to cancel");
                boolean cancelled = mBlueeToothConectionTask.cancel(true);
                Log.e("BLUETOOTH", "Cancelled: " + cancelled);
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
        if (mBroadcastReceiver != null) {
            requireActivity().unregisterReceiver(mBroadcastReceiver);
        }
    }


    private void requestActivateBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, BLUETOOTH_REQUEST_CODE);
    }

    private void addNewDevice(BluetoothDevice device) {
        mDeviceRowAdapter.addItem(device);
        new_devices_list.setAdapter(mDeviceRowAdapter);
    }

    private void searchBluetoothDevices() {
        bonded_devices = new ArrayList<>(mBluetoothAdapter.getBondedDevices());

        DeviceRowAdapter bondedAdapter = new DeviceRowAdapter(new ArrayList<>(bonded_devices), AccessShopFragment.this, getResources(), getActivity().getPackageName());
        bonded_devices_list.setAdapter(bondedAdapter);

        mDeviceRowAdapter = new DeviceRowAdapter(new ArrayList<>(), AccessShopFragment.this, getResources(), getActivity().getPackageName());
        new_devices_list.setAdapter(mDeviceRowAdapter);

        mBluetoothAdapter.startDiscovery();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                //Finding devices
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!bonded_devices.contains(device)) {
                        addNewDevice(device);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    private void connectToBluetoothServer(BluetoothDevice device) {
        mBlueeToothConectionTask = new BlueeToothConectionTask();
        mBlueeToothConectionTask.execute(device);
    }

    private void toogleVisibilityAccessShop(boolean showWelcome) {

        // Bluetooth elements
        TextView bluetooth_list_explanation = parent.findViewById(R.id.bluetooth_list_explanation);
        TextView bluetooth_searching_devices = parent.findViewById(R.id.bluetooth_searching_devices);
        ProgressBar bluetooth_progressBar = parent.findViewById(R.id.bluetooth_progressBar);
        TextView bluetooth_bonded_devices = parent.findViewById(R.id.bluetooth_bonded_devices);
        // Bonded device listView bonded_devices_list
        TextView bluetooth_new_devices = parent.findViewById(R.id.bluetooth_new_devices);
        // New devices listView new_devices_list

        //Access elements
        TextView welcome_text = parent.findViewById(R.id.welcome_text);
        // Shop name textView bluetooth_shop_name
        TextView enjoy_visit_text = parent.findViewById(R.id.enjoy_visit_text);
        Button bluetooth_exit_shop = parent.findViewById(R.id.bluetooth_exit_shop);

        if (showWelcome) {
            bluetooth_list_explanation.setVisibility(View.GONE);
            bluetooth_searching_devices.setVisibility(View.GONE);
            bluetooth_progressBar.setVisibility(View.GONE);
            bluetooth_bonded_devices.setVisibility(View.GONE);
            bonded_devices_list.setVisibility(View.GONE);
            bluetooth_new_devices.setVisibility(View.GONE);
            new_devices_list.setVisibility(View.GONE);

            welcome_text.setVisibility(View.VISIBLE);
            bluetooth_shop_name.setVisibility(View.VISIBLE);
            enjoy_visit_text.setVisibility(View.VISIBLE);
            bluetooth_exit_shop.setVisibility(View.VISIBLE);
        } else {
            bluetooth_list_explanation.setVisibility(View.VISIBLE);
            bluetooth_searching_devices.setVisibility(View.VISIBLE);
            bluetooth_progressBar.setVisibility(View.VISIBLE);
            bluetooth_bonded_devices.setVisibility(View.VISIBLE);
            bonded_devices_list.setVisibility(View.VISIBLE);
            bluetooth_new_devices.setVisibility(View.VISIBLE);
            new_devices_list.setVisibility(View.VISIBLE);

            welcome_text.setVisibility(View.GONE);
            bluetooth_shop_name.setVisibility(View.GONE);
            enjoy_visit_text.setVisibility(View.GONE);
            bluetooth_exit_shop.setVisibility(View.GONE);
        }
    }

    private class BlueeToothConectionTask extends AsyncTask<BluetoothDevice, String, BluetoothSocket> {
        private BluetoothSocket mBluetoothSocket;
        private BluetoothDevice mDevice;

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... bluetoothDevices) {
            BluetoothSocket tmp = null;
            if (bluetoothDevices.length != 0) {
                mDevice = bluetoothDevices[0];
            }
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e("BLUETOOTH", "Socket's create() method failed", e);
            }
            mBluetoothSocket = tmp;

            try {
                mBluetoothSocket.connect();
                //Send idGoogleLogin for the entry
                OutputStream outputStream = mBluetoothSocket.getOutputStream();
                outputStream.write(client.getIdGoogleLogin().getBytes());

                final int BUFFER_SIZE = 1024;
                byte[] buffer = new byte[1024];
                int bytes = 0;
                InputStream inputStream = mBluetoothSocket.getInputStream();

                //First read

                try {
                    bytes = inputStream.read(buffer);

                    String shopString = new String(buffer);
                    Log.e("BLUETOOTH", "Nuevo:" + shopString.trim());
                    publishProgress(shopString);
                } catch (IOException e) {
                    Log.e("BLUETOOTH", "Sin conexión");
                    return null;
                }

                while (true) {
                    //Check if async task isCancelled
                    if (isCancelled()) {
                        Log.e("BLUETOOTH", "Cancelada");
                        break;
                    }

                    //Check if socket is connected
                    try {
                        //TODO revisar otro método
                        outputStream.write(client.getIdGoogleLogin().getBytes());
                    } catch (IOException e) {
                        Log.e("BLUETOOTH", "Conexión perdida");
                        break;
                    }


                }
            } catch (IOException e) {
                // Unable to connect; close the socket and return.
                Log.e("BLUETOOTH", "Could not connect client socket", e);
                mBluetoothAdapter.startDiscovery();
                try {
                    mBluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e("BLUETOOTH", "Could not close the client socket", closeException);
                    mBluetoothAdapter.startDiscovery();
                }
                return null;
            }

            return mBluetoothSocket;
        }

        @Override
        protected void onProgressUpdate(String... shopNames) {
            toogleVisibilityAccessShop(true);
            if (shopNames.length != 0) {
                String shopName = shopNames[0];
                try {
                    JSONObject shopData = new JSONObject(shopName.trim());
                    bluetooth_shop_name.setText(shopData.getString("name"));
                } catch (JSONException e) {
                    bluetooth_shop_name.setText(shopName);
                }
            }
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            pd.dismiss();

        }

        @Override
        protected void onCancelled(BluetoothSocket bluetoothSocket) {
            Log.e("BLUETOOTH", "On Cancelled");
            toogleVisibilityAccessShop(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            pd.dismiss();
            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e) {
                    Log.e("BLUETOOTH", "On cancelled: Could not close the connect socket", e);
                }
            }
        }

        @Override
        protected void onPostExecute(BluetoothSocket bluetoothSocket) {
            Log.e("BLUETOOTH", "On postExecute");
            toogleVisibilityAccessShop(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            pd.dismiss();
            if (mBluetoothSocket != null) {
                try {
                    mBluetoothSocket.close();
                } catch (IOException e) {
                    Log.e("BLUETOOTH", "On postExecute: Could not close the connect socket", e);
                }
            }
        }
    }
}