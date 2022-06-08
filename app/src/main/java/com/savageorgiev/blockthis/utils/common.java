package com.savageorgiev.blockthis.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class common {
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context){
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
    private static GoogleSignInOptions SignInOptions(){
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("684601164472-s522r2cb7ed6lumm4cvhn289vk6a1hbn.apps.googleusercontent.com")
                .requestEmail()
                .build();
        return googleSignInOptions;
    }
    public static GoogleSignInClient signIntent(Context context){
        return  GoogleSignIn.getClient(context, common.SignInOptions());
    }
}
