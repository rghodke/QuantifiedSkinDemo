package activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.quantifiedskin.demo.quantifiedskindemo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Takes a picture directly from inside the app.
 * Source:http://developer.android.com/training/camera/cameradirect.html
 * Source:http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
 * Source:http://stackoverflow.com/questions/10913181/camera-preview-is-not-restarting
 * Source:http://stackoverflow.com/questions/16765527/android-switch-camera-when-button-clicked
 */
public class CameraActivity extends Activity implements Camera.PictureCallback {

    private Camera mCam; //Camera
    private int mCameraId = 0; //the camera id
    private MirrorView mCamPreview; //Used to see camera data
    private FrameLayout mPreviewLayout; //framelayout to hold mirrorview

    /**
     * Creates a bitmap from byte data
     * @param data  The array of bytes used to generate a bitmap
     * @return      The bitmap generate data
     */
    private static Bitmap toBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * Rotates bitmaps by the specified angle
     * @param in    The bitmap to be rotated
     * @param angle The angle to rotate the bitmap by
     * @return  The new rotated bitmap
     */
    private static Bitmap rotate(Bitmap in, int angle) {
        Matrix mat = new Matrix();
        mat.postRotate(angle);
        return Bitmap.createBitmap(in, 0, 0, in.getWidth(), in.getHeight(), mat, true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraId = findFirstFrontFacingCamera(); //Default to selfie mode

        //Set the layout to the selfie camera
        mPreviewLayout = (FrameLayout) findViewById(R.id.camPreview);
        mPreviewLayout.removeAllViews();
        startCameraInLayout(mPreviewLayout, mCameraId);

        //Take the picture on capture button press
        ImageButton takePic = (ImageButton) findViewById(R.id.capture);
        takePic.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mCam.takePicture(null, null, CameraActivity.this);
            }
        });
    }

    /**
     * Source:http://stackoverflow.com/questions/16765527/android-switch-camera-when-button-clicked
     * Changes the camera to the opposing camera
     * @param view  View for the button to know which view is calling
     */
    public void changeCamera(View view) {
        mCam.stopPreview();
        //NB: if you don't release the current camera before switching, you app will crash
        mCam.release();

        //swap the id of the camera to be used
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        mPreviewLayout = (FrameLayout) findViewById(R.id.camPreview);
        mPreviewLayout.removeAllViews();
        startCameraInLayout(mPreviewLayout, mCameraId);
    }

    /**
     * Method called after picture has been taken by the camera
     * @param data      The byte data from the camera
     * @param camera    The camera that had taken the picture
     */
    public void onPictureTaken(byte[] data, Camera camera) {

        /*
        Create a bitmap from the data and rotate it appropriately to create a portrait selfie
        90 if the back camera
        -90 if the front
         */
        Bitmap bitmap = toBitmap(data);
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            bitmap = rotate(bitmap, 90);
        } else {
            bitmap = rotate(bitmap, -90);
        }

        /*
        This creates the storage directory where the picture is going to be stored
         */
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists() && !storageDir.mkdirs()) {

            Log.d("TAG", "Can't create directory to save image");
            Toast.makeText(this, "Can't make path to save pic.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create a filename based on timeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "JPEG_" + timeStamp + "_";

        //The file is the actual picture file
        File pictureFile = null;
        try {
            pictureFile = File.createTempFile(
                    filename,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        /*
        Put the bitmap from the camera into the file created above.
         */
        try {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Launch into PictureAnalysis and put the selfieURI as an extra
            Intent i = new Intent(this, PictureAnalysis.class);
            i.putExtra("image", Uri.fromFile(pictureFile));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns the camera id of the selfie camera
     * @return  The camera id of the selfie camera
     */
    private int findFirstFrontFacingCamera() {
        int foundId = -1;
        int numCams = Camera.getNumberOfCameras();
        for (int camId = 0; camId < numCams; camId++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(camId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                foundId = camId;
                break;
            }
        }
        return foundId;
    }

    /**
     * Puts the appropriate camera into the layout specified
     * @param layout    The layout to put the camera mirrorView
     * @param cameraId  The camera to be used
     */
    private void startCameraInLayout(FrameLayout layout, int cameraId) {
        mCam = Camera.open(cameraId);
        if (mCam != null) {
            mCamPreview = new MirrorView(this, mCam);
            layout.addView(mCamPreview);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCam == null && mPreviewLayout != null) {
            mPreviewLayout.removeAllViews();
            startCameraInLayout(mPreviewLayout, mCameraId);
        }
    }

    @Override
    protected void onPause() {
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }
        super.onPause();
    }

    /**
     * Displays the selfie camera data to the user
     * Source: https://gist.github.com/CVirus/2645320
     */
    public class MirrorView extends SurfaceView implements
            SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public MirrorView(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            setCameraDisplayOrientationAndSize(); //Set the display orientation and size
            setCameraAutoFocus(); //turns on auto focus
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (Exception error) {
                Log.d("TAG",
                        "Error starting mPreviewLayout: " + error.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {
            if (mHolder.getSurface() == null) {
                return;
            }

            // can't make changes while mPreviewLayout is active
            try {
                mCamera.stopPreview();
            } catch (Exception e) {

            }

            try {

                // start up the mPreviewLayout
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception error) {
                Log.d("TAG",
                        "Error starting mPreviewLayout: " + error.getMessage());
            }
        }

        /**
         * Sets the camera to the appropriate display orientation and size
         */
        private void setCameraDisplayOrientationAndSize() {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int degrees = rotation * 90;

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            } else {
                result = (info.orientation - degrees + 360) % 360;
            }

            mCamera.setDisplayOrientation(result);

            Camera.Size previewSize = mCam.getParameters().getPreviewSize();
            if (result == 90 || result == 270) {
                mHolder.setFixedSize(previewSize.height, previewSize.width);
            } else {
                mHolder.setFixedSize(previewSize.width, previewSize.height);

            }
        }

        /**
         * Turns on autofocus
         */
        private void setCameraAutoFocus() {
            Camera.Parameters params = mCamera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            mCamera.setParameters(params);
        }

    }
}


