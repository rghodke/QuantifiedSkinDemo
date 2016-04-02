package activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseUser;
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
        (if the user has already signed in, go to the next activity)
         */
    }

    /*
    Creates a new ParseAccount for the session. For the sake of this demo, I will create
    a new account each time but account persistence can be easily maintained with authentication
     */
    private boolean createAccount() {
        ParseUser.enableAutomaticUser();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {
            return true;
        } else {
            Toast.makeText(SplashScreen.this,
                    "Unable to start the demo. Please contact rghodke@ucsd.edu",
                    Toast.LENGTH_SHORT).show();
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
    private boolean isOnline() {
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
    private boolean hasCamera() {
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the phone can properly run the demo
     *
     * @return Boolean true if the phone can run the app
     */
    private boolean allChecks() {
        //Check for internet
        if (!isOnline()) {
            Toast.makeText(SplashScreen.this, getString(R.string.app_internet_requirement),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        //Check for camera
        if (!hasCamera()) {
            Toast.makeText(SplashScreen.this, getString(R.string.app_camera_requirement),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        //Check for making an account
        if (!createAccount()) {
            Toast.makeText(SplashScreen.this, getString(R.string.parse_fail),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    /*
    Begins the demo checking for network connectivity and a camera else a new toast is made
    @param view The view for the onClick to know which view is reference
     */
    public void transitionToClassScreen(View view) {
        /*
        Potential authentication code goes here
         */
        if (allChecks()) {
            Intent i = new Intent(this, CameraActivity.class);
            startActivity(i);
        }
    }

}


