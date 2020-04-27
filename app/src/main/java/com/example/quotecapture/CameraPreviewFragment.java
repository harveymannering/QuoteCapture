package com.example.quotecapture;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.CursorLoader;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraPreviewFragment extends Fragment {

        //Camera variables
        private Camera mCamera;
        private SurfaceView preview;
        private MediaRecorder mediaRecorder;
        FrameLayout cameraPreview;
        //android.hardware.Camera.Size cameraSize;

        //Filename of the last picture taken
        String filename;

        //A second picture can only be taken after saving of the first has finished
        private boolean safeToTakePitcutre = false;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_camera_preview, container, false);
            cameraPreview = (FrameLayout) view.findViewById(R.id.camera_preview);

            //Set up the take picture button
            FloatingActionButton takePictureButton = (FloatingActionButton) view.findViewById(R.id.button_capture);
            takePictureButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Unsafe to take  picture while another is being saved
                    if (safeToTakePitcutre == true){
                        //Take and save the picture
                        mCamera.takePicture(null, null, mPicture);
                        safeToTakePitcutre = false;
                    }
                }
            });

            //Load image from file button
            ImageButton openFileBtn = view.findViewById(R.id.fileButton);
            openFileBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent()
                            .setType("image/*")
                            .setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
                }
            });

            //Menu button
            ImageButton menuBtn = view.findViewById(R.id.menuButton);
            menuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.viewPager.setCurrentItem(1);
                }
            });

            return view;
        }

    @Override
    public void onStart() {
        super.onStart();
        //Camera setup
        CameraSetup();
    }

    private void CameraSetup(){
        if (checkCameraHardware(getContext())){
            //Gets the camera
            mCamera = getCameraInstance();
            if (mCamera != null){
                // Set Camera parameters
                Camera.Parameters params = mCamera.getParameters();
                //cameraSize = params.getPictureSize();

                //Turn autofocusing on
                List<String> focusModes = params.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(params);
                }

                //Get the dimensions of the screen
                /*Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);

                //Puts the camera live video feed on the screen

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(displaySize.x + 200, 1000);
                layoutParams.gravity = Gravity.LEFT;
                preview.setLayoutParams(layoutParams);*/
                preview = new CameraPreviewFragment.CameraPreview(getContext(), mCamera);
                cameraPreview.addView(preview);
            }
        }
    }

    //Get the file selected from the open file button
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file

            //save a copy of this file
            SaveImageInternally(selectedfile);

            //Start a new activity
            Intent intent  = new Intent(getContext(), PictureTakenActivity.class);
            intent.putExtra("FILENAME", filename);
            intent.putExtra("LOADED_IMAGE", true);
            startActivity(intent);

        }
    }

    private String getRealPathFromURI(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver()
                .query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    public void SaveImageInternally(Uri selectedFile){

        //Read old file into bitmap
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Create new file
        File newFile = getOutputMediaFile();
        if (newFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }

        //save image internally
        try {
            FileOutputStream ostream;
            newFile.createNewFile();
            ostream = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            ostream.flush();
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // When picture is taken call this method, it saves the image just taken
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile();
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            /*Bitmap b  = BitmapFactory.decodeByteArray(data, 0, data.length);

            //Get the dimensions of the screen
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);
            //Rotate and scale the display
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            float scaleX = ((float) displaySize.x / (float) b.getHeight());
            float scaleY = ((float) (displaySize.y * 0.9) / (float) b.getWidth());
            if (scaleX <= scaleY)
                matrix.postScale(scaleX, scaleX);
            else
                matrix.postScale(scaleY, scaleY);
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);


            //Save image
            //Create new file
            File newFile = getOutputMediaFile();
            if (newFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            //save image
            try {
                FileOutputStream ostream;
                newFile.createNewFile();
                ostream = new FileOutputStream(newFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                ostream.flush();
                ostream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            //restart the preview or else screen freezes
            mCamera.stopPreview();
            mCamera.startPreview();

            //Set up new fragment and fragmentmanager
            /*PictureTakenFragment pictureTaken = new PictureTakenFragment(filename);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.camera_fragment, pictureTaken);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();*/

            //Set up new activity
            Intent intent  = new Intent(getContext(), PictureTakenActivity.class);
            intent.putExtra("FILENAME", filename);
            intent.putExtra("LOADED_IMAGE", false);

            startActivity(intent);


            safeToTakePitcutre = true;
        }
    };


    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.getStackTraceString(e);
        }
        return c; // returns null if camera is unavailable
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        ContextWrapper cw = new ContextWrapper(getContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        if (!directory.exists())
            return null;

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        filename = directory.getAbsolutePath() + File.separator +
                "IMG_"+ timeStamp + ".jpg";
        File mediaFile;
        mediaFile = new File(filename);

        return mediaFile;
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                setWillNotDraw(false); //Stops the surface view from cutting anything off
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                safeToTakePitcutre = true;
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
            //releaseMediaRecorder();
            //releaseCamera();
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }


            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here
            mCamera = setCameraDisplayOrientation(getActivity(), Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                safeToTakePitcutre = true;
            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        public Camera setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {

            Camera.CameraInfo info = new Camera.CameraInfo();

            android.hardware.Camera.getCameraInfo(cameraId, info);

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;

            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

        /*Code for the front facing camera
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }*/

            // Camera will always be back-facing
            int result = (info.orientation - degrees + 360) % 360;

            //Perform the roatation
            try {
                camera.setDisplayOrientation(result);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            //Return camera
            return camera;
        }
    }
}

