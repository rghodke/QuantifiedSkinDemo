package com.quantifiedskin.demo.quantifiedskindemo.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.quantifiedskin.demo.quantifiedskindemo.R;

/*
Displays the splash screen that is shown to the user when the app is first booted.
This is where we could potentially have the login and other auth done.
 */
public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        animateLogo();
        fadeInDemoButton();
        /*
        Potential authentication logic here
        (if the user has already signed in, go to the next screen)
         */
    }

    /*
    Creates a new ParseAccount for the session. For the sake of this demo, I will create
    a new account each time but account persistence can be easily maintained with authentication
     */
    private boolean createAccount(){
        Parse.enableLocalDatastore(this);
        Parse.initialize(this);
        ParseUser.enableAutomaticUser();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {return true;}
        else {
            return false;
        }
    }

    /*
    Performs the logo animation of moving from the center of the layout to the top
     */
    private void animateLogo() {
        ImageView logo = (ImageView) findViewById(R.id.logo);
        int height = logo.getDrawable().getIntrinsicHeight();
        height *= -1;
        TranslateAnimation toTop = new TranslateAnimation(0, 0, 0, height);
        toTop.setDuration(2000);
        toTop.setFillAfter(true);
        logo.startAnimation(toTop);
    }

    /*
    Fades in the button that prompts the user to sign in
     */
    private void fadeInDemoButton() {
        Button signInButton = (Button) findViewById(R.id.signInButton);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new AccelerateInterpolator());
        fadeIn.setDuration(2500);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        signInButton.startAnimation(animation);
    }

    /**
     * Checks if the phone is connected to the internet
     *
     * @return Boolean true if the phone has network connectivity
     */
    public boolean isOnline() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Checks if the phone has a camera
     *
     * @return Boolean true if the phone has camera
     */
    public boolean hasCamera() {
        PackageManager pm = getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }

        return false;
    }


    /*
    Begins the demo assuming the app has network connectivity and a camera else a new toast is made
    @param view The view for the onClick to know which view is reference
     */
    public void transitionToClassScreen(View view) {
        /*
        Potential authentication code goes here
         */
        if (isOnline()) {
            if(hasCamera()){
                if(createAccount()) {
                    Intent i = new Intent(this, CameraActivity.class);
                    startActivity(i);
                    finish();
                }
                else{
                    Toast.makeText(SplashScreen.this, "Parse Account Creation went wrong", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(SplashScreen.this, "Need Camera", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(SplashScreen.this, getString(R.string.app_internet_requirement),
                    Toast.LENGTH_SHORT).show();
        }
    }

}
