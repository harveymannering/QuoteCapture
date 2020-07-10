package com.example.quotecapture;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.os.ParcelFileDescriptor.MODE_WORLD_READABLE;
import static java.util.Calendar.LONG;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraPreviewFragment extends Fragment {

        //Camera variables
        private Camera mCamera;
        private SurfaceView preview;
        private MediaRecorder mediaRecorder;
        FrameLayout cameraPreview;
        private ZoomControls zoomControls;

        //Database variable
        Database database;
        SQLiteDatabase db;

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

            //Database
            //Read quote from database
            database = new Database(getActivity());
            db = database.getWritableDatabase();
            log("start");


            //Set up the take picture button
            FloatingActionButton takePictureButton = (FloatingActionButton) view.findViewById(R.id.button_capture);
            takePictureButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Unsafe to take  picture while another is being saved
                    if (safeToTakePitcutre == true){
                        //Take and save the picture
                        try {
                            safeToTakePitcutre = false;
                            mCamera.takePicture(null, null, mPicture);
                        }
                        catch (Exception e){
                            Toast.makeText(getContext(), "Error accessing camera", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            log("2");


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
            log("3");


            //Menu button
            ImageButton menuBtn = view.findViewById(R.id.menuButton);
            menuBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.viewPager.setCurrentItem(1);
                }
            });
            log("4");

            return view;
        }

    @Override
    public void onStart() {
        super.onStart();
        log("5");
        //Camera setup
        CameraSetup();
        log("6");

    }

    public void log(String sBody) {
        database.log(db, sBody);
    }

    private void CameraSetup(){
        log("7");
        if (checkCameraHardware(getContext())){
            //Gets the camera
            log("8");
            mCamera = getCameraInstance();
            log("9");
            if (mCamera != null){
                log("10");
                // Set Camera parameters
                Camera.Parameters params = mCamera.getParameters();
                log("11");
                //cameraSize = params.getPictureSize();

                //Turn autofocusing on
                List<String> focusModes = params.getSupportedFocusModes();
                log("12");
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    mCamera.setParameters(params);
                }
                log("13");

                //Get the dimensions of the screen
                /*Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);

                //Puts the camera live video feed on the screen

                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(displaySize.x + 200, 1000);
                layoutParams.gravity = Gravity.LEFT;
                preview.setLayoutParams(layoutParams);*/
                preview = new CameraPreviewFragment.CameraPreview(getContext(), mCamera);
                log("14");
                cameraPreview.addView(preview);
                log("15");
            }
        }
    }

    //Get the file selected from the open file button
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        log("16");
        if(requestCode == 123 && resultCode == RESULT_OK) {
            log("17");
            Uri selectedfile = data.getData(); //The uri with the location of the file
            log("18");
            //save a copy of this file
            SaveImageInternally(selectedfile);
            log("19");
            //Start a new activity
            Intent intent  = new Intent(getContext(), PictureTakenActivity.class);
            log("20");
            intent.putExtra("FILENAME", filename);
            log("21");
            intent.putExtra("LOADED_IMAGE", true);
            log("22");
            startActivity(intent);
            log("23");

        }
    }

    private String getRealPathFromURI(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver()
                .query(contentURI, null, null, null, null);
        log("24");
        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            log("25");
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            log("26");
            return cursor.getString(idx);
        }
    }

    public void SaveImageInternally(Uri selectedFile){

        //Read old file into bitmap
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedFile);
            log("26");
        } catch (IOException e) {
            e.printStackTrace();
            log("27");
        }

        //Create new file
        File newFile = getOutputMediaFile();
        log("28");
        if (newFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions");
            log("29");
            return;
        }

        //save image internally
        try {
            FileOutputStream ostream;
            log("30");
            newFile.createNewFile();
            log("31");
            ostream = new FileOutputStream(newFile);
            log("32");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
            log("33");
            ostream.flush();
            log("34");
            ostream.close();
            log("35");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // When picture is taken call this method, it saves the image just taken
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            log("36");
            File pictureFile = getOutputMediaFile();
            log("37");
            if (pictureFile == null){
                log("38");
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                log("39");
                fos.write(data);
                log("40");
                fos.close();
                log("41");
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
            log("42");
            mCamera.stopPreview();
            log("43");
            mCamera.startPreview();
            log("44");


            //Set up new fragment and fragmentmanager
            /*PictureTakenFragment pictureTaken = new PictureTakenFragment(filename);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.camera_fragment, pictureTaken);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(null);
            ft.commit();*/

            //Set up new activity
            Intent intent  = new Intent(getContext(), PictureTakenActivity.class);
            log("45");
            intent.putExtra("FILENAME", filename);
            log("46");
            intent.putExtra("LOADED_IMAGE", false);
            log("47");

            startActivity(intent);
            log("48");


            safeToTakePitcutre = true;
        }
    };


    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            log("49");
            mediaRecorder.reset();   // clear recorder configuration
            log("50");
            mediaRecorder.release(); // release the recorder object
            log("51");
            mediaRecorder = null;
            log("52");
            mCamera.lock();           // lock camera for later use
            log("53");
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            log("54");
            mCamera.release();        // release the camera for other applications
            log("55");
            mCamera = null;
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        log("56");
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            log("57");
            return true;
        } else {
            log("58");
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
        log("59");
        ContextWrapper cw = new ContextWrapper(getContext());
        log("60");
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        log("61");
        if (!directory.exists())
            return null;

        // Create a media file name
        log("62");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        log("63");
        filename = directory.getAbsolutePath() + File.separator +
                "IMG_"+ timeStamp + ".jpg";
        File mediaFile;
        log("64");
        mediaFile = new File(filename);
        log("65");

        return mediaFile;
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;
        float mDist = 0;


        public CameraPreview(Context context, Camera camera) {
            super(context);
            log("66");
            mCamera = camera;
            log("67");

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            log("68");
            mHolder.addCallback(this);
            log("69");
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            log("70");
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                log("71");
                setWillNotDraw(false); //Stops the surface view from cutting anything off
                log("72");
                mCamera.setPreviewDisplay(holder);
                log("73");
                mCamera.startPreview();
                log("74");
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
                log("75");
                return;
            }

            // stop preview before making changes
            try {
                log("76");
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here
            log("77");
            mCamera = setCameraDisplayOrientation(getActivity(), Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);

            // start preview with new settings
            try {
                log("78");
                mCamera.setPreviewDisplay(mHolder);
                log("79");
                mCamera.startPreview();
                log("80");
                safeToTakePitcutre = true;
            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        public Camera setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {

            log("81");
            Camera.CameraInfo info = new Camera.CameraInfo();
            log("82");

            android.hardware.Camera.getCameraInfo(cameraId, info);
            log("83");

            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            log("84");
            int degrees = 0;

            log("85");
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
            log("86");

        /*Code for the front facing camera
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        }*/

            // Camera will always be back-facing
            int result = (info.orientation - degrees + 360) % 360;

            //Perform the roatation
            try {
                log("87");
                camera.setDisplayOrientation(result);
                log("88");
            }
            catch (Exception e){
                e.printStackTrace();
            }

            //Return camera
            return camera;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Get the pointer ID
            log("89");
            Camera.Parameters params = mCamera.getParameters();
            log("90");
            int action = event.getAction();
            log("91");


            if (event.getPointerCount() > 1) {
                // handle multi-touch events
                log("92");
                if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    log("93");
                    mDist = getFingerSpacing(event);
                    log("94");
                } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                    log("95");
                    mCamera.cancelAutoFocus();
                    log("96");
                    handleZoom(event, params);
                    log("97");
                }
            } else {
                // handle single touch events
                if (action == MotionEvent.ACTION_UP) {
                    log("98");
                    handleFocus(event, params);
                    log("99");
                }
            }
            return true;
        }

        private void handleZoom(MotionEvent event, Camera.Parameters params) {
            log("100");
            int maxZoom = params.getMaxZoom();
            log("101");
            int zoom = params.getZoom();
            log("102");
            float newDist = getFingerSpacing(event);
            log("103");
            if (newDist > mDist) {
                //zoom in
                if (zoom < maxZoom)
                    zoom++;
            } else if (newDist < mDist) {
                //zoom out
                if (zoom > 0)
                    zoom--;
            }
            log("104");
            mDist = newDist;
            log("105");
            params.setZoom(zoom);
            log("106");
            mCamera.setParameters(params);
            log("107");
        }

        public void handleFocus(MotionEvent event, Camera.Parameters params) {
            log("108");
            int pointerId = event.getPointerId(0);
            log("109");
            int pointerIndex = event.findPointerIndex(pointerId);
            log("110");
            // Get the pointer's current position
            float x = event.getX(pointerIndex);
            log("111");
            float y = event.getY(pointerIndex);
            log("112");

            List<String> supportedFocusModes = params.getSupportedFocusModes();
            log("113");
            if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                log("114");
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        // currently set to auto-focus on single touch
                    }
                });
                log("115");
            }
        }

        /** Determine the space between the first two fingers */
        private float getFingerSpacing(MotionEvent event) {
            // ...
            log("116");
            float x = event.getX(0) - event.getX(1);
            log("117");
            float y = event.getY(0) - event.getY(1);
            log("118");
            return (float)Math.sqrt(x * x + y * y);
        }

    }
}

