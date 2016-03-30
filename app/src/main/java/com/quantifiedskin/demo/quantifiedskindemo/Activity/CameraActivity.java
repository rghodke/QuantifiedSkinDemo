package com.quantifiedskin.demo.quantifiedskindemo.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.quantifiedskin.demo.quantifiedskindemo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Helper.JSONParser;
import Helper.JSONReader;


public class CameraActivity extends AppCompatActivity {
    /*
    Source: http://developer.android.com/training/camera/photobasics.html
     */
    private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        dispatchTakePictureIntent();
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Bitmap retVal;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        retVal = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        return retVal;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {


            Bitmap myBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);

            try{
                ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                switch(orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        myBitmap = rotateImage(myBitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        myBitmap = rotateImage(myBitmap, 180);
                        break;
                    // etc.
                }
            }
            catch (IOException ioe){}

            ImageView myImage = (ImageView) findViewById(R.id.selfieImage);
            myImage.setImageBitmap(myBitmap);
            uploadImage(myBitmap);
        }
    }

    /**
     * Source: http://stackoverflow.com/questions/16954109/reduce-the-size-of-a-bitmap-to-a-specified-size-in-android
     * Reduces the size of the image
     * @param image
     * @param maxSize
     * @return
     */
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /*
    Uploads the picture to Parse to obtain a url so that I can use to make a
    REST call to the face++ api
     */
    private void uploadImage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap = getResizedBitmap(bitmap, 640); //2000 kb limit
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        final ParseFile file = new ParseFile("selfieImage", data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                System.out.println("TAG!!!!");
                new GetGenderAsync().execute(file.getUrl());
            }
        });
    }

    private class GetGenderAsync extends AsyncTask<String, Integer, JSONObject> {

        protected JSONObject doInBackground(String... urls) {
            JSONReader jsonReader = new JSONReader();
            return jsonReader.readObject(urls[0]);
        }

        protected void onProgressUpdate(Integer...integers) {
        }

        protected void onPostExecute(JSONObject response) {
            JSONParser jsonParser = new JSONParser();
            String answer = null;
            try{
                answer = jsonParser.parseJSONfile(response);
                System.out.println(answer);}
            catch (JSONException je){}
        }
    }
}
