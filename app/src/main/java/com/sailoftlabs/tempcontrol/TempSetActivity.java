package com.sailoftlabs.tempcontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class TempSetActivity extends AppCompatActivity {
    EditText tempText;
    String tempSet;
    Double tempRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_set);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TempSetActivity.this);
        setSupportActionBar(toolbar);
        tempText = (EditText)findViewById(R.id.tempText);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempSet = tempText.getText().toString();
                if(tempSet.length() < 0){

                    tempRequest = Double.parseDouble(tempSet);



                    //ParticleCloudSDK.getCloud().publishEvent("SetTemp", tempSet, ParticleEventVisibility.PRIVATE, 60);

                }
                Snackbar.make(view, "Temp set", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(TempSetActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

}
