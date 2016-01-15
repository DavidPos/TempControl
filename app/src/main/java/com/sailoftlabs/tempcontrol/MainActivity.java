package com.sailoftlabs.tempcontrol;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ParticleDevice myDevice;
    private TextView tempOut;
    private String device;
    private Object tempVar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tempOut = (TextView) findViewById(R.id.tempText);
        setSupportActionBar(toolbar);
        //ParticleDeviceSetupLibrary.init(this.getApplicationContext(), MainActivity.class);
        //ParticleCloudSDK.init(MainActivity.this);
        

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Temp refreshed", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                getTemp();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Intent intent = getIntent();
        device = intent.getStringExtra("device");
        ParticleCloud pCloud = ParticleCloudSDK.getCloud();
        Toaster.l(MainActivity.this, "Access" + pCloud.getAccessToken());


        Async.executeAsync(pCloud, new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud particleCloud) throws ParticleCloudException, IOException {
                ParticleDevice myDevice = particleCloud.getDevice(device);

                long subscriptionId;  // save this for later, for unsubscribing
                try {
                    subscriptionId = myDevice.subscribeToEvents(
                            "temperature",  //event name
                            new ParticleEventHandler() {
                                public void onEvent(String eventName, ParticleEvent event) {
                                    tempVar = event.dataPayload;
                                    tempOut.setText("Temp: " + tempVar + " \u2103");
                                    Log.i("Photon Event: ", "Received event with payload: " + event.dataPayload);
                                }

                                public void onEventError(Exception e) {
                                    Log.e("some tag", "Event error: ", e);
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return tempVar;
            }

            @Override
            public void onSuccess(Object o) {
               // tempOut.setText("Temp: " + o + " \u2103");

            }

            @Override
            public void onFailure(ParticleCloudException exception) {

            }
        });

        //getTemp();


    }

    private void getTemp() {
        ParticleCloud pCloud = ParticleCloudSDK.getCloud();
        Async.executeAsync(pCloud, new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {

                ParticleDevice myDevice = ParticleCloud.getDevice(device);
                Object variable;
                try {
                    variable = myDevice.getVariable("temperature");
                } catch (ParticleDevice.VariableDoesNotExistException e) {
                    Toaster.l(MainActivity.this, e.getMessage());
                    variable = -1;
                }



                return variable;
            }

            @Override
            public void onSuccess(Object i) { // this goes on the main thread
                final double tempCon = Double.parseDouble(i.toString());

                String result = String.format("%.2f", tempCon);

                tempOut.setText("Temp: " + result + " \u2103");

            }

            @Override
            public void onFailure(ParticleCloudException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //TODO:Add Login id for going to login activity
        //TODO:Add Setting id

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
