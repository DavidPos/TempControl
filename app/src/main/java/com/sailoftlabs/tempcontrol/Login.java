package com.sailoftlabs.tempcontrol;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;
import io.particle.android.sdk.utils.Async;
import io.particle.android.sdk.utils.Toaster;

public class Login extends AppCompatActivity {
    protected TextView mSignUpTextView;
    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    private ArrayList<String> devices = new ArrayList<>();
    private ParticleDevice mDevice;

    private String accessToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        ParticleCloudSDK.init(Login.this);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mUsername = (EditText)findViewById(R.id.usernameField);
        mPassword = (EditText)findViewById(R.id.passwordField);


        mLoginButton = (Button)findViewById(R.id.loginButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginButton.setEnabled(false);
                final String username = mUsername.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();





                if (username.isEmpty() || password.isEmpty() ){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    //Login here
                    setProgressBarVisibility(true);
                    final ParticleCloud cloud = ParticleCloudSDK.getCloud();


                    Async.executeAsync(cloud, new Async.ApiWork<ParticleCloud, Void>() {


                        @Override
                        public Void callApi(@NonNull final ParticleCloud particleCloud) throws ParticleCloudException, IOException {


                            particleCloud.logIn(username, password);
                            //TODO: Add list of devices to select from once logged in


                            return null;
                        }

                        @Override
                        public void onSuccess(Void aVoid) {
                            Toaster.l(Login.this, "Logged in");
                            // start new activity...
                            Intent intent = new Intent(Login.this, MainActivity.class);

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //intent.putExtra("device", mDevice.getID());
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(ParticleCloudException e) {
                            Log.e("SOME_TAG", e +"");
                            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                            builder.setMessage(R.string.login_error_message)
                                    .setTitle(R.string.login_error_title)
                                    .setPositiveButton(android.R.string.ok, null);
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            mLoginButton.setEnabled(true);
                        }
                    });





                }
            }

        });
    }

}
