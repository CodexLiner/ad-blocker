package com.savageorgiev.blockthis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.savageorgiev.blockthis.databinding.ActivitySignInBinding;
import com.savageorgiev.blockthis.network.AsyncResponse;
import com.savageorgiev.blockthis.network.BackendCall;
import com.savageorgiev.blockthis.utils.common;

import org.json.JSONObject;

import java.net.URLEncoder;

public class SignInActivity extends AppCompatActivity implements AsyncResponse {
    private FirebaseAuth mFirebaseAuth;
    private ActivitySignInBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferences = getSharedPreferences("user" , MODE_PRIVATE);
        editor = preferences.edit();
        if (preferences.contains("loggedIn")){
            if (preferences.getBoolean("loggedIn" , false)){
                startActivity(new Intent(getApplicationContext() , MainActivity.class));
                finishAffinity();
                overridePendingTransition(0,0);
            }
        }
        mFirebaseAuth = FirebaseAuth.getInstance();
        findViewById(R.id.signIn).setOnClickListener(v->{
            signIn();
        });
        binding.validate.setOnClickListener(v->{
            String url =" https://netlux.in/_subpages/adb/api.php?paraURL=101" +
                    "&pkey="
                    +binding.licenseKey.getText().toString()
                    +"&emailid="+binding.userEmail.getText().toString()+
                    "&hwid="+binding.userDeviceId.getText().toString();
            BackendCall call = new BackendCall(url, this);
            call.asyncResponse = this;
            call.execute();
        });
    }
    private void signIn() {
        Intent intent =  common.signIntent(this).getSignInIntent();
        startActivityForResult(intent , 100);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                nextStep(account);
            } catch (ApiException ignored) {}
        }
    }
    private void nextStep(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnSuccessListener(this, authResult -> {
            if (mFirebaseAuth.getCurrentUser()!=null){
              binding.nextLayout.setVisibility(View.VISIBLE);
              binding.signIn.setVisibility(View.INVISIBLE);
              fillViews(mFirebaseAuth.getCurrentUser());
            }
        }).addOnFailureListener(this, e -> Toast.makeText(SignInActivity.this, "Sign In failed.", Toast.LENGTH_SHORT).show());
    }

    private void fillViews(FirebaseUser currentUser) {
        binding.userEmail.setText(currentUser.getEmail());
        binding.userName.setText(currentUser.getDisplayName());
        binding.userDeviceId.setText(common.getDeviceId(getApplicationContext()));
    }

    @Override
    public void Result(String response) {
        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
        if (response!=null){
            if (response.equalsIgnoreCase("INVALID")){
                Toast.makeText(this, "Invalid Product Key", Toast.LENGTH_SHORT).show();
            }
            else if (response.equalsIgnoreCase("VALID")){
                String url = "https://netlux.in/_subpages/adb/api.php?paraURL=201&" +
                        "pkey=" +binding.licenseKey.getText().toString()+
                        "&emailid="+binding.userEmail.getText().toString()+
                        "&hwid="+binding.userDeviceId.getText().toString()+"&cname="+ URLEncoder.encode(binding.userName.getText().toString()) +
                        "&cmob=554561745";
                BackendCall call = new BackendCall(url , SignInActivity.this);
                call.asyncResponse = this;
                call.execute();
            }else if (response.equalsIgnoreCase("TRUE")){
                editor.putBoolean("loggedIn" , true);
                editor.putString("pkey" , binding.licenseKey.getText().toString());
                editor.commit();
                editor.apply();
                startActivity(new Intent(getApplicationContext() , MainActivity.class));
                finishAffinity();
            }
            else if (response.equalsIgnoreCase("FALSE")){
                Toast.makeText(this, "Failed For Some Reasons", Toast.LENGTH_SHORT).show();
            }
        }
    }
}