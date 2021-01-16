package es.ewic.clients.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.List;

import es.ewic.clients.R;

public class DeviceRowAdapter extends BaseAdapter implements ListAdapter {

    private final List<BluetoothDevice> devices;
    private final Fragment fragment;
    private final Resources resources;
    private final String packageName;

    public DeviceRowAdapter(List<BluetoothDevice> devices, Fragment fragment, Resources resources, String packageName) {
        assert devices != null;
        assert fragment != null;
        assert resources != null;
        assert packageName != null;

        this.devices = devices;
        this.fragment = fragment;
        this.resources = resources;
        this.packageName = packageName;
    }

    @Override
    public int getCount() {
        if (devices == null) {
            return 0;
        } else {
            return devices.size();
        }
    }

    @Override
    public BluetoothDevice getItem(int position) {
        if (devices == null) {
            return null;
        } else {
            return devices.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = fragment.getLayoutInflater().inflate(R.layout.devices_row, null);
        }

        TextView device_name = convertView.findViewById(R.id.device_name);
        TextView device_address = convertView.findViewById(R.id.device_address);

        BluetoothDevice device = getItem(position);
        if (device != null) {
            device_name.setText(device.getName());
            device_address.setText(device.getAddress());
        }
        return convertView;
    }

    public void addItem(BluetoothDevice device) {
        if (device != null) {
            devices.add(device);
        }
    }
}
