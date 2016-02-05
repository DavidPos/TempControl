package com.sailoftlabs.tempcontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
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
import java.util.ArrayList;
import java.util.Date;

import io.particle.android.sdk.accountsetup.LoginActivity;
import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.cloud.ParticleEvent;
import io.particle.android.sdk.cloud.ParticleEventHandler;
import io.particle.android.sdk.persistance.SensitiveDataStorage;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ParticleDevice myDevice;
    private TextView tempOut;
    private String device;
    private Object tempVar;
    private String temp;
    private  long subscriptionId;
    private ArrayList<String> devices = new ArrayList<>();
    ParticleCloud pCloud;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tempOut = (TextView) findViewById(R.id.tempText);
        setSupportActionBar(toolbar);

        ParticleCloudSDK.init(MainActivity.this);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getTemp();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        pCloud = ParticleCloudSDK.getCloud();
        SensitiveDataStorage sensitiveDataStorage = new SensitiveDataStorage(this);
        Date tokenDate = sensitiveDataStorage.getTokenExpirationDate();
        Date currentDate = new Date();
        if (currentDate.after(tokenDate)) {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            Toaster.l(MainActivity.this, "Token date is: " + tokenDate + " " + " Current Date: " + currentDate);
        } else {
            Toaster.l(MainActivity.this, "Token is not expired " + currentDate + " " + "Token Date is: " + tokenDate);
        }


        Intent intent = getIntent();
        if (intent.getStringExtra("device") == null) {
            Intent intent1 = new Intent(MainActivity.this, DeviceSelect.class);
            startActivity(intent1);


        } else {
            getDevice(intent.getStringExtra("device"));

        }



    }


    @Override
    protected void onPause() {
        super.onPause();

            if (subscriptionId < 0) {
                try {
                   myDevice.unsubscribeFromEvents(subscriptionId);
                }catch(ParticleCloudException e){
                    e.printStackTrace();
            }
        }
    }

    private void getDevice(final String deviceName){

        //final ParticleCloud cloud = ParticleCloudSDK.getCloud();


        Async.executeAsync(pCloud, new Async.ApiWork<ParticleCloud, Void>() {


            @Override
            public Void callApi(@NonNull final ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                    myDevice = particleCloud.getDevice(deviceName);


                return null;
            }

            @Override
            public void onSuccess(Void aVoid) {
                Toaster.l(MainActivity.this, "Success: " + myDevice.getName());
                //subscribeToTemp();
                getTemp();


            }

            @Override
            public void onFailure(ParticleCloudException e) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.device_select_error)
                        .setTitle(R.string.login_error_title)
                        .setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });


    }
    private void getTemp(){
        Async.executeAsync(pCloud, new Async.ApiWork<ParticleCloud, Object>() {
            @Override
            public Object callApi(@NonNull ParticleCloud ParticleCloud) throws ParticleCloudException, IOException {


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

    private void subscribeToTemp() {

        Async.executeAsync(pCloud, new Async.ApiWork<ParticleCloud, Void>() {


            @Override
            public Void callApi(@NonNull final ParticleCloud particleCloud) throws ParticleCloudException, IOException {

                    subscriptionId = particleCloud.subscribeToAllEvents("temperature",new ParticleEventHandler() {
                        @Override
                        public void onEvent(String eventName, ParticleEvent particleEvent) {
                            Log.e("Event: ", "onEvent: " + particleEvent.dataPayload );
                            temp = particleEvent.dataPayload;

                            tempOut.setText(temp);


                        }

                        @Override
                        public void onEventError(Exception e) {
                            Log.e("temperature", "OH NOES, onEventError: ", e);
                        }
                    });


                       /* pCloud.subscribeToMyDevicesEvents(null, new ParticleEventHandler() {
                    @Override
                    public void onEvent(String eventName, ParticleEvent particleEvent) {
                        tempOut.setText(particleEvent.dataPayload);



                    }

                    @Override
                    public void onEventError(Exception e) {
                        Log.e("temperature", "OH NOES, onEventError: ", e);
                    }
                });*/



                return null;
            }



            @Override
            public void onSuccess(Void aVoid) {
                Toaster.l(MainActivity.this,"Success event subscribed too" );


            }

            @Override
            public void onFailure(ParticleCloudException exception) {

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
