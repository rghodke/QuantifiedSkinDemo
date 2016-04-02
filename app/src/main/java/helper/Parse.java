package helper;

/**
 * Created by Admin on 3/31/2016.
 */

/*
Sets up the Parse auth for this app to allow the pictures to be uploaded to the cloud for
analysis
 */
public class Parse extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Init parse only once in the entire app's life
        com.parse.Parse.enableLocalDatastore(this);
        com.parse.Parse.initialize(this);
    }
}