package activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;
import com.quantifiedskin.demo.quantifiedskindemo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import helper.JSONParser;
import helper.JSONReader;

/**
 * Performs the analysis of the selfie taken from the CameraActivity
 */
public class PictureAnalysis extends Activity {
    /*
    Source: http://developer.android.com/training/camera/photobasics.html
     */
    private String mCurrentPhotoPath;
    private static String[] sQuotes;
    private Uri mImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_analysis);

        retrieveQuotes(); //Retrieve the loading screen quotes

        mImageUri = getIntent().getParcelableExtra("image"); //get the selfie URI

        /**
         * Create the bitmap based off the uri given
         */
        Bitmap selfieImage = null;
        try {
            selfieImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
        } catch (FileNotFoundException fe) {
            Toast.makeText
                    (PictureAnalysis.this, "The bitmap can't be found", Toast.LENGTH_SHORT).show();
        } catch (IOException ioe) {
            Toast.makeText
                    (PictureAnalysis.this, "The file cannot be loaded", Toast.LENGTH_SHORT).show();
        }

        uploadImage(selfieImage); //Upload the selfie image
        setImage(mImageUri); //Display the image to the user
    }

    /**
     * Sets the imageView to the uri provided
     *
     * @param uri The uri to the selfie image
     */
    private void setImage(Uri uri) {
        ImageView imageView = (ImageView) findViewById(R.id.selfieImage);
        imageView.setImageURI(uri);
    }

    /**
     * Connects to parse to retrieve the quotes
     */
    private void retrieveQuotes() {
        if (sQuotes == null) new GetQuotes().execute();
        else {
            setQuote(sQuotes);
        }
    }

    /**
     * Source: http://stackoverflow.com/questions/16954109/reduce-the-size-of-a-bitmap-to-a-specified-size-in-android
     * Reduces the size of the image
     *
     * @param image     The image to be resized
     * @param maxSize   The max size to resize to
     * @return          The new resized bitmap
     */
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * Uploads the picture to Parse to obtain a url so that I can use to make a
     * REST call to the face++ api
     *
     * @param bitmap The bitmap to be uploaded
     */
    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        /*
        Max file size has to be under limit
        Checked by using generated RGB noise to obtain max file size
         */
        bitmap = getResizedBitmap(bitmap, 640); //2000 kb limit
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        final ParseFile file = new ParseFile("selfieImage", data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                new GetGenderAsync().execute(file.getUrl());
            }
        });
    }

    /**
     * Sets the textview to a random quote from the quote list retrieved online
     *
     * @param quotes A list of quotes or ads that can be placed online
     */
    private void setQuote(String[] quotes) {
        String quote = null;
        //Failsafe
        if (quotes == null) {
            quote = "";
        } else {
            int randomElement = new Random().nextInt(quotes.length);
            quote = quotes[randomElement];
        }
        TextView quoteTextView = (TextView) findViewById(R.id.quoteText);
        quoteTextView.setText(quote);

    }

    /**
     * Sets the image appropriate to the response gender
     *
     * @param gender The string that decides which image to show
     */
    private void setImage(String gender) {
        ImageView toolImage = (ImageView) findViewById(R.id.toolImage);
        toolImage.setVisibility(View.VISIBLE);
        switch (gender) {
            case "NoFace":
                toolImage.setImageResource(R.drawable.noface);
                break;
            case "Male":
                toolImage.setImageResource(R.drawable.razor);
                break;
            case "Female":
                toolImage.setImageResource(R.drawable.mascara);
                break;
        }
    }

    /*
    Reads the answer back after submitting the image to the face++ api
     */
    private class GetGenderAsync extends AsyncTask<String, Integer, JSONObject> {

        private ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);

        protected JSONObject doInBackground(String... urls) {
            JSONReader jsonReader = new JSONReader();
            return jsonReader.readObject(urls[0]);
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(JSONObject response) {
            JSONParser jsonParser = new JSONParser();
            String answer = null;
            try {
                answer = jsonParser.parseJSONfile(response);
                progressBar.setVisibility(View.INVISIBLE);
                setImage(answer); //Set the image depending on the answer
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the quotes from the Parse file online
     */
    private class GetQuotes extends AsyncTask<Void, Integer, JSONObject> {

        protected JSONObject doInBackground(Void... params) {
            JSONReader jsonReader = new JSONReader();
            return jsonReader.readQuotes();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(JSONObject response) {
            JSONParser jsonParser = new JSONParser();
            try {
                sQuotes = jsonParser.parseJSONQuotes(response);
                setQuote(sQuotes);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
    }

}
