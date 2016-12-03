package com.aware.plugin.sensory_wristband.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aware.plugin.sensory_wristband.R;

import java.util.List;

public class DeviceArrayAdapter extends ArrayAdapter<Device> {

    private Activity context;
    private List<Device> devices;

    public DeviceArrayAdapter(Activity context, int resource, List<Device> devices) {
        super(context, resource, devices);
        this.context = context;
        this.devices = devices;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        View rowView = convertView;
        if (rowView == null){
            LayoutInflater layoutInflater = context.getLayoutInflater();
            rowView = layoutInflater.inflate(R.layout.device_list_item,null,true);
            viewHolder = new ViewHolder();
            viewHolder.deviceName = (TextView) rowView.findViewById(R.id.deviceName);
            viewHolder.deviceAddress = (TextView) rowView.findViewById(R.id.deviceAddress);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }
        viewHolder.deviceName.setText(devices.get(position).getName());
        viewHolder.deviceAddress.setText(devices.get(position).getAddress());
        return rowView;
    }

    /**
     * Static class ViewHolder
     * It is design pattern implemented to store TextViews for optimization purposes
     */
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

}
