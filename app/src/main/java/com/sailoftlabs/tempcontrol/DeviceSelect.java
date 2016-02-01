package com.sailoftlabs.tempcontrol;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class DeviceSelect extends AppCompatActivity {
    ParticleCloud particleCloud;
    private ArrayList<String> devices = new ArrayList<>();
    private ParticleDevice myDevice;
    private ListView deviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        deviceList = (ListView)findViewById(R.id.deviceList);
        setSupportActionBar(toolbar);
        ParticleCloudSDK.init(DeviceSelect.this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        particleCloud = ParticleCloudSDK.getCloud();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeviceSelect.this, android.R.layout.simple_list_item_1);

        deviceList.setAdapter(adapter);
        Async.executeAsync(particleCloud, new Async.ApiWork<ParticleCloud, Void>() {


            @Override
            public Void callApi(@NonNull final ParticleCloud particleCloud) throws ParticleCloudException, IOException {


                final List<ParticleDevice> allDevices = particleCloud.getDevices();
                for (ParticleDevice device : allDevices){
                    devices.add(device.getID());
                    adapter.add(device.getName());

                }
                if (!devices.isEmpty()){

                    adapter.notifyDataSetChanged();

                    //myDevice = particleCloud.getDevice(devices.get(1));
                    Toaster.l(DeviceSelect.this, "Device found" + " " + myDevice.getName());
                }
                else {
                    Toaster.l(DeviceSelect.this,"No Devices");
                }

                return null;
            }

            @Override
            public void onSuccess(Void aVoid) {
                Toaster.l(DeviceSelect.this, "Success");

            }

            @Override
            public void onFailure(ParticleCloudException e) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceSelect.this);
                builder.setMessage(R.string.device_select_error)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

}
