package com.example.quotecapture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static android.content.ContentValues.TAG;

public class PictureTakenActivity extends AppCompatActivity {



    //File location of the picture
    Uri uri;
    String uriString;
    //Quote id that may or may not passed to to the activity
    long quoteID;
    boolean loaded_image = false;

    //Drawing objects
    Queue<DrawingView> drawingViews;
    DrawingView previous_dv;
    //DrawingView dv2;
    Paint currentPaint;

    //Buttons at the bottom of the screen
    ImageButton sizeButton;
    ImageButton colorButton;
    ImageButton eraserButton;
    ImageButton rotateButton;

    //Screen/canavas dimensions
    int canvasWidth = 0;
    int canvasHeight = 0;
    Point displaySize;

    //Settings
    enum COLOUR {YELLOW(0), PINK(1), GREEN(2), BLUE(3), ERASER(4);
        private final int value;
        private COLOUR(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    };
    int SelectedColour = COLOUR.YELLOW.getValue();
    int brushSize = 50;
    boolean eraserToggle = false;

    //popup views
    ConstraintLayout layout;
    View PopupColour;
    BrushSizeView circleView;
    View PopupBrushSize;
    ProgressDialog dialog;

    //Flags used for removal animations
    boolean PopupSizeRemoved = true;
    boolean PopupColourRemoved = true;

    float toolbarHeight = 0;
    ConstraintSet set;

    //Rotate button variables
    ImageView image;
    ViewSwitcher highlightingSwitcher;
    LinearLayout splashScreen;
    boolean viewsHaveBeenSwitched;
    int rotations = 0;
    int imageWidth = 0;
    int imageHeight = 0;

    //Rotation animations
    AnimationSet animationSetImage;
    AnimationSet animationSetCanvas;
    AlphaAnimation anim1; //fades in new canvas
    AlphaAnimation anim2; //fades out old canvas


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //Get filename of the image to be displayed
        uriString = getIntent().getStringExtra("FILENAME");
        uri = Uri.parse(uriString);
        //Get the QuoteID (if it exists)
        quoteID = 0;
        if (getIntent() != null && getIntent().getExtras() != null) {
            quoteID = getIntent().getExtras().getInt("QUOTE_ID");
        }
        //Check if the image was loaded from storage
        loaded_image = getIntent().getExtras().getBoolean("LOADED_IMAGE");

        CreateUI();
    }

    private void CreateUI() {
        super.onResume();
        //Get dimensions of the toolbar
        Drawable drawable = getResources().getDrawable(R.drawable.ic_adjust_black_24dp);
        toolbarHeight = convertDpToPixel((float) drawable.getMinimumHeight(), getApplicationContext());

        //Get the dimensions of the screen
        Display display = getWindowManager().getDefaultDisplay();
        displaySize = new Point();
        display.getSize(displaySize);

        //Main layout
        layout = new ConstraintLayout(this);
        //layout.setLayoutTransition(new LayoutTransition());
        set = new ConstraintSet();

        //Image behind the canvas
        image = new ImageView(this);
        File imgFile = new  File(uriString);
        if(imgFile.exists())
        {
            //Load the image into a bitmap
            Bitmap myBitmap = ImageProcessor.LoadImageIntoBitmap(uriString, displaySize);
            imageHeight = myBitmap.getHeight();
            imageWidth = myBitmap.getWidth();

            //configure the layout
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(displaySize.x, (int) (displaySize.y * 0.9));
            layoutParams.gravity = Gravity.CENTER;
            //Add border/margins to image
            if (myBitmap.getWidth() < displaySize.x){
                double xDifference = displaySize.x - myBitmap.getWidth();
                layoutParams.setMargins((int) Math.ceil(xDifference / 2), 0, (int) Math.floor(xDifference / 2), 0);
            }
            else if (myBitmap.getHeight() < displaySize.y){
                double yDifference = displaySize.y - myBitmap.getHeight();
                layoutParams.setMargins(0, (int) Math.ceil(yDifference / 2), 0, (int) Math.floor(yDifference / 2));
            }
            image.setLayoutParams(layoutParams);

            //add the image to the imageview
            image.setImageBitmap(myBitmap);
        }
        image.setId(View.generateViewId());

        //Drawing canvas
        drawingViews = new LinkedList<DrawingView>();
        DrawingView dv = new DrawingView(this);
        dv.setId(View.generateViewId());
        dv.loadImage(0,0);
        dv.setLayoutParams(new ViewGroup.LayoutParams(canvasWidth, canvasHeight));
        dv.setAlpha(0.3f);
        drawingViews.add(dv);
        highlightingSwitcher = new ViewSwitcher(this);
        highlightingSwitcher.setId(View.generateViewId());
        highlightingSwitcher.addView(dv);

        //Create default paint object (which contain colour and stroke width)
        currentPaint = CreatePaintObject(COLOUR.YELLOW);

        SelectedColour = 0; //Default yellow
        setHighlighterWidth();
        //Back button
        ImageButton backButton = new ImageButton(this);
        backButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        backButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        backButton.setBackground(null);
        backButton.setId(View.generateViewId());
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        //Tick button
        ImageButton okButton = new ImageButton(this);
        okButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        okButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_white_24dp));
        okButton.setBackground(null);
        okButton.setId(View.generateViewId());
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //Display loading screen
                dialog = ProgressDialog.show(PictureTakenActivity.this, "",
                        getResources().getString(R.string.loading), true);

                //String highlightedImageURI = dv.SaveCanvas();
                new Thread(new Runnable() {
                    public void run() {
                        //if the image has been rotated the image will need to be resaved
                        Bitmap bitmapImage;
                        if (rotations != 0) {
                            File file = new File(uriString);
                            bitmapImage = ImageProcessor.LoadImageIntoBitmapAndRotate(uriString, displaySize, rotations);
                            Database.DeleteInternalImage(uriString);

                            File replacementFile = new File(uriString);
                            try {
                                FileOutputStream ostream;
                                replacementFile.createNewFile();
                                ostream = new FileOutputStream(replacementFile);
                                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                                ostream.flush();
                                ostream.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //correct image rotation
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            float scaleX = ((float) displaySize.x / (float) bitmapImage.getHeight());
                            float scaleY = ((float) (displaySize.y * 0.9) / (float) bitmapImage.getWidth());

                            if (scaleX <= scaleY)
                                matrix.postScale(scaleX, scaleX);
                            else
                                matrix.postScale(scaleY, scaleY);

                            bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, bitmapImage.getWidth(), bitmapImage.getHeight(), matrix, true);

                        }
                        else {
                            bitmapImage = ImageProcessor.LoadImageIntoBitmap(uriString, displaySize);
                        }

                        View content = drawingViews.peek();
                        content.setDrawingCacheEnabled(true);
                        content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

                        //Saves the highlighting as an image
                        Bitmap bitmapHighlight = content.getDrawingCache();
                        File fileHighlights = drawingViews.peek().getOutputMediaFile("_h");
                        saveBitmap(fileHighlights, bitmapHighlight);

                        //Load images
                        Bitmap bitmapHighlighted = BitmapFactory.decodeFile(fileHighlights.getAbsolutePath());
                        Bitmap resultImage = ImageProcessor.applyHighlighting(bitmapImage, bitmapHighlighted);

                        //Read text in the image
                        String text = ImageProcessor.OpticalCharacterRecognition(resultImage, getApplicationContext());

                        //Saves the results image
                        File fileResults = drawingViews.peek().getOutputMediaFile("_r");
                        saveBitmap(fileResults, resultImage);

                        //Save the quote to database
                        Database database = new Database(getApplicationContext());
                        SQLiteDatabase db = database.getReadableDatabase();
                        if (quoteID > 0)
                            database.updateQuote(db, quoteID, text, fileHighlights.getAbsolutePath(), fileResults.getAbsolutePath());
                        else
                            quoteID = database.addQuote(db, text, Calendar.getInstance().getTime(), 0, uriString, fileHighlights.getAbsolutePath(), fileResults.getAbsolutePath());
                        db.close();
                        //Set up new activity
                        Intent intent = new Intent(getApplicationContext(), ViewQuote.class);
                        intent.putExtra("QUOTE_ID", quoteID);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);


                    }

                    public void saveBitmap(File file, Bitmap bitmap){
                        try {
                            FileOutputStream ostream;
                            file.createNewFile();
                            ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        //Brush Size button
        sizeButton = new ImageButton(this);
        sizeButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        sizeButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_adjust_black_24dp));
        sizeButton.setBackground(null);
        sizeButton.setId(View.generateViewId());
        sizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                //Remove the colour popup if its currently being displayed
                if (PopupColour != null){
                    RemoveColourPopup();
                }

                //Check that the popup up is not already there before displaying it
                if (PopupBrushSize == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    PopupBrushSize = inflater.inflate(R.layout.popup_brush_size, null);
                    layout.addView(PopupBrushSize);
                    PopupBrushSize.setId(View.generateViewId());
                    PopupBrushSize.setMinimumWidth(displaySize.x);
                    PopupBrushSize.setMinimumHeight(displaySize.y / 10);

                    //Canvas where circle will be drawn
                    circleView = new BrushSizeView(getApplicationContext());
                    circleView.setId(View.generateViewId());
                    circleView.setLayoutParams(new ViewGroup.LayoutParams(canvasWidth, (int) (canvasHeight * 0.8)));
                    layout.addView(circleView);

                    //Set up seekbar
                    SeekBar seekBar = (SeekBar) PopupBrushSize.findViewById((R.id.seekBar));
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) (displaySize.x * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.setMargins(0, 10, 0, 10);
                    seekBar.setProgress(brushSize);
                    seekBar.setLayoutParams(layoutParams);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            brushSize = progress;
                            setHighlighterWidth();
                            circleView.drawCircle();
                            circleView.invalidate();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    //This clock disables clicks when clicked on the popup window (outside the seek bar)
                    PopupBrushSize.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    //Format the layout
                    set.clone(layout);
                    set.connect(PopupBrushSize.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
                    set.applyTo(layout);


                    //Appear animation
                    AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(200);
                    anim.setRepeatMode(Animation.REVERSE);
                    PopupBrushSize.startAnimation(anim);
                    AlphaAnimation anim2 = new AlphaAnimation(0.0f, 1.0f);
                    anim2.setDuration(200);
                    anim2.setRepeatMode(Animation.REVERSE);
                    circleView.startAnimation(anim2);
                    //Flag used for fade out animationd
                    PopupSizeRemoved = false;
                }
                else
                {
                    RemoveBrushSizePopup();
                }
            }

        });

        //Color button
        colorButton = new ImageButton(this);
        colorButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        colorButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_palette_white_24dp));
        colorButton.setBackground(null);
        colorButton.setId(View.generateViewId());
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                //Remove the brush size popup if its currently being displayed
                if (PopupBrushSize != null)
                    RemoveBrushSizePopup();

                //Check that the popup up is not already there before displaying it
                if (PopupColour == null) {
                    //Set up the popup's layout
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    PopupColour = inflater.inflate(R.layout.popup_colour, null);
                    layout.addView(PopupColour);
                    PopupColour.setId(View.generateViewId());
                    PopupColour.setMinimumWidth(displaySize.x);

                    //Get references to the layouts containing colour buttons
                    LinearLayout yellowLayout = PopupColour.findViewById(R.id.layoutYellow);
                    LinearLayout pinkLayout = PopupColour.findViewById(R.id.layoutPink);
                    LinearLayout greenLayout = PopupColour.findViewById(R.id.layoutGreen);
                    LinearLayout blueLayout = PopupColour.findViewById(R.id.layoutBlue);
                    //Formate the image buttons (colour buttons)
                    yellowLayout.setMinimumWidth(displaySize.x / 4);
                    pinkLayout.setMinimumWidth(displaySize.x / 4);
                    greenLayout.setMinimumWidth(displaySize.x / 4);
                    blueLayout.setMinimumWidth(displaySize.x / 4);
                    yellowLayout.setMinimumHeight(displaySize.y / 10);
                    pinkLayout.setMinimumHeight(displaySize.y / 10);
                    greenLayout.setMinimumHeight(displaySize.y / 10);
                    blueLayout.setMinimumHeight(displaySize.y / 10);

                    //Format view on the canvas
                    set.clone(layout);
                    set.connect(PopupColour.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
                    set.applyTo(layout);

                    setSelectedColour(PopupColour);

                    //Add functionality to buttons
                    ImageButton btnYellow = PopupColour.findViewById(R.id.buttonYellow);
                    btnYellow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SelectedColour = COLOUR.YELLOW.getValue();
                            if (eraserToggle == false) {
                                currentPaint = CreatePaintObject(COLOUR.YELLOW);
                                setHighlighterWidth();
                            } else {
                                ToggleEraser();
                            }
                            setSelectedColour(PopupColour);
                        }
                    });

                    ImageButton btnPink = PopupColour.findViewById(R.id.buttonPink);
                    btnPink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SelectedColour = COLOUR.PINK.getValue();
                            if (eraserToggle == false) {
                                currentPaint = CreatePaintObject(COLOUR.PINK);
                                setHighlighterWidth();
                            } else {
                                ToggleEraser();
                            }
                            setSelectedColour(PopupColour);
                        }
                    });
                    ImageButton btnGreen = PopupColour.findViewById(R.id.buttonGreen);
                    btnGreen.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SelectedColour = COLOUR.GREEN.getValue();
                            if (eraserToggle == false) {
                                currentPaint = CreatePaintObject(COLOUR.GREEN);
                                setHighlighterWidth();
                            } else {
                                ToggleEraser();
                            }
                            setSelectedColour(PopupColour);
                        }
                    });

                    ImageButton btnBlue = PopupColour.findViewById(R.id.buttonBlue);
                    btnBlue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SelectedColour = COLOUR.BLUE.getValue();
                            if (eraserToggle == false) {
                                currentPaint = CreatePaintObject(COLOUR.BLUE);
                                setHighlighterWidth();
                            } else {
                                ToggleEraser();
                            }
                            setSelectedColour(PopupColour);
                        }
                    });

                    //This clock disables clicks when clicked on the popup window (outside the circles)
                    PopupColour.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });


                    //Appear animation
                    AlphaAnimation anim1 = new AlphaAnimation(0.0f, 1.0f);
                    anim1.setDuration(200);
                    anim1.setRepeatMode(Animation.REVERSE);
                    PopupColour.startAnimation(anim1);
                    //Flag used for fade out animation
                    PopupColourRemoved = false;

                }
                else {
                    RemoveColourPopup();
                }


            }

            public void setSelectedColour(View PopupColour){
                //Define the colour buttons
                ImageButton btnYellow = PopupColour.findViewById(R.id.buttonYellow);
                ImageButton btnPink = PopupColour.findViewById(R.id.buttonPink);
                ImageButton btnGreen = PopupColour.findViewById(R.id.buttonGreen);
                ImageButton btnBlue = PopupColour.findViewById(R.id.buttonBlue);

                //set the default image for all buttons
                btnYellow.setBackground(getResources().getDrawable(R.drawable.ic_colour_yellow));
                btnPink.setBackground(getResources().getDrawable(R.drawable.ic_colour_pink));
                btnGreen.setBackground(getResources().getDrawable(R.drawable.ic_colour_green));
                btnBlue.setBackground(getResources().getDrawable(R.drawable.ic_colour_blue));

                //Change the image of the selected colour
                if (SelectedColour == COLOUR.YELLOW.getValue())
                    btnYellow.setBackground(getResources().getDrawable(R.drawable.ic_colour_yellow_border));
                else if (SelectedColour == COLOUR.PINK.getValue())
                    btnPink.setBackground(getResources().getDrawable(R.drawable.ic_colour_pink_border));
                else if (SelectedColour == COLOUR.GREEN.getValue())
                    btnGreen.setBackground(getResources().getDrawable(R.drawable.ic_colour_green_border));
                else if (SelectedColour == COLOUR.BLUE.getValue())
                    btnBlue.setBackground(getResources().getDrawable(R.drawable.ic_colour_blue_border));
            }
        });

        //Brush Size button
        eraserButton = new ImageButton(this);
        eraserButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        eraserButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_eraser_black_24dp));
        eraserButton.setBackground(null);
        eraserButton.setId(View.generateViewId());
        eraserButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){
                ToggleEraser();
                //Remove any popups
                if (PopupBrushSize != null)
                    RemoveBrushSizePopup();
                if (PopupColour != null)
                    RemoveColourPopup();
            }
        });

        //Rotate button
        rotateButton = new ImageButton(this);
        rotateButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        rotateButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_rotate_90_degrees_ccw_black_24dp));
        rotateButton.setBackground(null);
        rotateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){

                //Remove any popups
                if (PopupBrushSize != null)
                    RemoveBrushSizePopup();
                if (PopupColour != null)
                    RemoveColourPopup();

                if ((animationSetImage == null || animationSetImage.hasEnded()) &&
                        (animationSetCanvas == null || animationSetCanvas.hasEnded()) &&
                        (anim1 == null || anim1.hasEnded()) &&
                        (anim2 == null || anim2.hasEnded())) {
                    //disables drawing
                    //drawingViews.peek().setEnabled(false);
                    //Add invisible splash screen to disable touch event during animation
                    splashScreen = new LinearLayout(getBaseContext());
                    splashScreen.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    Button but = new Button(getBaseContext());
                    but.setText("SCREEN DISABLED WHILE ROTATING");
                    but.setAlpha(0f);
                    but.setLayoutParams(new LinearLayout.LayoutParams(displaySize.x, (int) (displaySize.y * 0.9)));
                    splashScreen.addView(but);
                    layout.addView(splashScreen);

                    //Rotate Animation
                    final RotateAnimation rotateAnimImage = new RotateAnimation((-90) * rotations, (-90) * (rotations + 1),
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimImage.setDuration(200);
                    rotateAnimImage.setFillAfter(true);

                    final RotateAnimation rotateAnimCanvas = new RotateAnimation(0, -90,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                            RotateAnimation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimCanvas.setDuration(200);
                    rotateAnimCanvas.setFillAfter(true);
                    //image.startAnimation(rotateAnim);

                    rotations += 1;
                    rotations %= 4;

                    //Scale Animation
                    float scaleX = ((float) displaySize.x / (float) imageHeight);
                    float scaleY = ((float) (displaySize.y * 0.9) / (float) imageWidth);
                    float scale = scaleY;
                    if (scaleX <= scaleY)
                        scale = scaleX;

                    Animation scaleAnimImage;
                    Animation scaleAnimCanvas;
                    if (rotations % 2 == 1) {
                        scaleAnimImage = new ScaleAnimation(
                                1f, scale, // Start and end values for the X axis scaling
                                1f, scale, // Start and end values for the Y axis scaling
                                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                        scaleAnimCanvas = new ScaleAnimation(1f, scale, 1f, scale,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    } else {
                        scaleAnimImage = new ScaleAnimation(
                                scale, 1f, // Start and end values for the X axis scaling
                                scale, 1f, // Start and end values for the Y axis scaling
                                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
                        scaleAnimCanvas = new ScaleAnimation(1f, 1 / scale, 1f, 1 / scale,
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    }
                    scaleAnimImage.setFillAfter(true); // Needed to keep the result of the animation
                    scaleAnimImage.setDuration(200);
                    scaleAnimCanvas.setFillAfter(true); // Needed to keep the result of the animation
                    scaleAnimCanvas.setDuration(200);
                    //image.startAnimation(scaleAnim);

                    //Animations collections
                    animationSetImage = new AnimationSet(true);
                    animationSetCanvas = new AnimationSet(true);

                    //Setup animations for the image
                    animationSetImage.addAnimation(scaleAnimImage);
                    animationSetImage.addAnimation(rotateAnimImage);
                    animationSetImage.setFillAfter(true);

                    //Set up animations for the canvas that is drawn on
                    animationSetCanvas.addAnimation(scaleAnimCanvas);
                    animationSetCanvas.addAnimation(rotateAnimCanvas);
                    animationSetCanvas.setFillAfter(true);
                    animationSetCanvas.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            RefreshDrawingView();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    drawingViews.peek().startAnimation(animationSetCanvas);
                    image.startAnimation(animationSetImage);
                }
            }
        });

        //Add all component to the layout
        layout.addView(image);
        layout.addView(highlightingSwitcher);
        layout.addView(backButton);
        layout.addView(okButton);
        set.clone(layout);
        set.connect(okButton.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
        set.applyTo(layout);

        /////////////////////////////
        //Set up the bottom toolbar//
        /////////////////////////////
        TableLayout toolbarLayout = new TableLayout(this);
        //What these layouts are used for are specified in the comments
        LinearLayout linLayout1 = new LinearLayout(this); // Colour picker button
        LinearLayout linLayout2 = new LinearLayout(this); // Resizing button
        LinearLayout linLayout3 = new LinearLayout(this); // Eraser/Highlighter toggle button
        LinearLayout linLayout4 = new LinearLayout(this); // Rotate button
        //Add the views to the hierarcy of layouts
        TableRow row = new TableRow(this);
        linLayout1.addView(colorButton);
        linLayout2.addView(sizeButton);
        linLayout3.addView(eraserButton);
        linLayout4.addView(rotateButton);
        row.addView(linLayout1);
        row.addView(linLayout2);
        row.addView(linLayout3);
        row.addView(linLayout4);
        toolbarLayout.addView(row);
        //Format the table cells, set the width and center the buttons
        linLayout1.setGravity(Gravity.CENTER);
        linLayout1.setMinimumWidth(displaySize.x / 4);
        linLayout1.setMinimumHeight(displaySize.y / 10);
        linLayout2.setGravity(Gravity.CENTER);
        linLayout2.setMinimumWidth(displaySize.x / 4);
        linLayout2.setMinimumHeight(displaySize.y / 10);
        linLayout3.setGravity(Gravity.CENTER);
        linLayout3.setMinimumWidth(displaySize.x / 4);
        linLayout3.setMinimumHeight(displaySize.y / 10);
        linLayout4.setGravity(Gravity.CENTER);
        linLayout4.setMinimumWidth(displaySize.x / 4);
        linLayout4.setMinimumHeight(displaySize.y / 10);

        //Layout containing all other layouts
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.BLACK);

        mainLayout.addView(layout);
        mainLayout.addView(toolbarLayout);
        setContentView(mainLayout);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dialog != null)
            //Removes loading dialog
            dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        //if the image is one we've saved and the previous screen was the camera...
        if (quoteID == 0){
            //...delete the image
            Database.DeleteInternalImage(uriString);
        }

        super.onDestroy();
    }

    private void RefreshDrawingView(){

        //Get the image from the drawing view
        View content = drawingViews.peek();
        content.setDrawingCacheEnabled(true);
        content.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmapHighlighting = content.getDrawingCache();

        //Calculate scaling
        float scaleX = ((float) displaySize.x / (float) imageHeight);
        float scaleY = ((float) (displaySize.y * 0.9) / (float) imageWidth);
        float scale = scaleY;
        if (scaleX <= scaleY)
            scale = scaleX;
        if (rotations % 2 == 0)
            scale = 1 / scale;

        //Calculate dimensions of underliying image
        int iH = image.getDrawable().getIntrinsicHeight();//original height of underlying image
        int iW = image.getDrawable().getIntrinsicWidth();//original
        if (rotations % 2 == 0){
            int tmp = iH;
            iH = (int) Math.floor(iW * (1/scale));
            iW = (int) Math.floor(tmp * (1/scale));
        }

        //Crop bitmap
        if (iW + 1 < displaySize.x){
            double xDifference = displaySize.x - iW;
            bitmapHighlighting = Bitmap.createBitmap(bitmapHighlighting, (int) Math.floor(xDifference / 2), 0, bitmapHighlighting.getWidth() - (int) xDifference , iH);
        }
        else if (iH + 1 < displaySize.y * 0.9){
            double yDifference = (displaySize.y * 0.9) - iH;
            bitmapHighlighting = Bitmap.createBitmap(bitmapHighlighting, 0, (int) Math.floor(yDifference / 2), iW, bitmapHighlighting.getHeight() - (int) yDifference);
        }

        //Rotate and scale image
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        //Apply scaling of image
        matrix.postScale(scale, scale);

        //rotate and scale bitmap
        bitmapHighlighting = Bitmap.createBitmap(bitmapHighlighting, 0, 0, bitmapHighlighting.getWidth(), bitmapHighlighting.getHeight(), matrix, true);

        //add padding to bitmap
        int padding_y = ((int) (displaySize.y * 0.9) - bitmapHighlighting.getHeight()) / 2;
        int padding_x = (displaySize.x - bitmapHighlighting.getWidth()) / 2;
        Bitmap outputimage = Bitmap.createBitmap(bitmapHighlighting.getWidth() + padding_x,bitmapHighlighting.getHeight() + padding_y, Bitmap.Config.ARGB_8888);
        Canvas can = new Canvas(outputimage);
        //can.drawARGB(FF,FF,FF,FF); //This represents White color
        can.drawBitmap(bitmapHighlighting, padding_x, padding_y, null);


        //Build and add replacement drawing view to layout
        DrawingView dv = new DrawingView(this);
        dv.setId(View.generateViewId());
        dv.setLayoutParams(new ViewGroup.LayoutParams(canvasWidth, canvasHeight));
        dv.setAlpha(0.3f);
        dv.setRotatedBitmap(outputimage);


        //Fade in animation
        anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(100);
        anim1.setRepeatMode(Animation.REVERSE);
        anim1.setFillAfter(true);

        //Fade out animation
        anim2 = new AlphaAnimation(1.0f, 0.0f);
        anim2.setDuration(100);
        anim2.setRepeatMode(Animation.REVERSE);
        anim2.setFillAfter(true);
        anim2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                previous_dv = drawingViews.peek();
                drawingViews.remove();
                layout.removeView(splashScreen);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        highlightingSwitcher.setInAnimation(anim1);
        highlightingSwitcher.setOutAnimation(anim2);
        if (viewsHaveBeenSwitched == true){
            highlightingSwitcher.removeView(previous_dv);
        }
        highlightingSwitcher.addView(dv);
        drawingViews.add(dv);
        highlightingSwitcher.showNext();
        viewsHaveBeenSwitched = true;
        //dv.setRotation(90);
        //dv.setScaleX(1/scale);
        //dv.setScaleY(1/scale);

    }

    //private void

    private void ToggleEraser(){
        if (eraserToggle == true)
        {
            eraserButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_eraser_black_24dp));
            eraserToggle = false;
            currentPaint = CreatePaintObject(COLOUR.values()[SelectedColour]);
            setHighlighterWidth();
        }
        else
        {
            eraserButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_white_24dp));
            eraserToggle = true;
            currentPaint = CreatePaintObject(COLOUR.ERASER);
            setHighlighterWidth();
        }
    }

    //Creates the four different paint brushes
    private Paint CreatePaintObject(COLOUR c){
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.BUTT);
        if (c == COLOUR.YELLOW)
            p.setColor(Color.YELLOW);
        else if (c == COLOUR.PINK)
            p.setColor(Color.MAGENTA);
        else if (c == COLOUR.GREEN)
            p.setColor(Color.GREEN);
        else if (c == COLOUR.BLUE)
            p.setColor(Color.CYAN);
        else if (c == COLOUR.ERASER) {
            p.setColor(getResources().getColor(android.R.color.transparent));
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        return p;

    }

    void RemoveColourPopup(){
        if (PopupColourRemoved == false) {
            PopupColourRemoved = true;
            PopupColour.animate().setDuration(100).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layout.removeView(PopupColour);
                    PopupColour = null;
                }
            });

        }
    }

    void RemoveBrushSizePopup(){
        if (PopupSizeRemoved == false) {
            PopupSizeRemoved = true;
            PopupBrushSize.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layout.removeView(PopupBrushSize);
                    PopupBrushSize = null;
                }
            });
            circleView.animate().setDuration(200).alpha(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layout.removeView(circleView);
                    circleView = null;
                }
            });

        }
    }



    //Sets the brush size for all Paint objects
    void setHighlighterWidth(){
        currentPaint.setStrokeWidth((brushSize + convertDpToPixel(15, this))*2);
    }

    float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        Context context;
        //Object for saving the lines drawn by the user
        ArrayList<Pair<Path, Paint>> linesDrawn;
        Path currentPath;
        boolean isRotation;

        //private Paint circlePaint;
        //private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context = c;
            //Initalize the paths array
            linesDrawn = new ArrayList<Pair<Path, Paint>>();

            //mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            /*circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.BLUE);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);*/
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h , oldw, oldh);

            loadImage(w, h);
            mCanvas = new Canvas(mBitmap);
            draw(mCanvas);
        }

        public void setRotatedBitmap(Bitmap bm){
            mBitmap = bm;
            isRotation = true;
        }

        public void loadImage(int w, int h){
            if (isRotation == false ) {
                if (quoteID > 0) {
                    Database database = new Database(getApplicationContext());
                    SQLiteDatabase db = database.getReadableDatabase();
                    Quote q = database.getQuote(db, quoteID);
                    mBitmap = ImageProcessor.LoadHighlightedImageIntoBitmap(q.getHighlightedImagedURI(), displaySize);
                } else
                    mBitmap = ImageProcessor.LoadBlankImageIntoBitmap(uriString, displaySize);
            }
            canvasWidth = displaySize.x;
            canvasHeight = (int) Math.round(displaySize.y * 0.9);
        }

        public void loadImage(Bitmap bm){
            mBitmap = bm;
            canvasWidth = displaySize.x;
            canvasHeight = (int) Math.round(displaySize.y * 0.9);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap( mBitmap, 0, 0, null);
            //Draws previous lines
            for (int i = 0; i < linesDrawn.size(); i++)
                canvas.drawPath(linesDrawn.get(i).first, linesDrawn.get(i).second);
            //Draws current line being drawn
            if (currentPaint != null && currentPath != null)
                canvas.drawPath(currentPath, currentPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            //mPath.reset();
            currentPath = new Path();
            currentPath.moveTo(x, y);
            mX = x;
            mY = y;
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (drawingViews.peek().equals(this)) {
                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                    currentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                    mX = x;
                    mY = y;
                    //circlePath.reset();
                    //circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
                }
            }
        }

        private void touch_up() {
            currentPath.lineTo(mX, mY);
            //circlePath.reset();
            // commit the path to our offscreen
            loadImage(canvasWidth, canvasHeight);
            //Make a copy of the current paint object
            Paint paintCopy =
                    eraserToggle == false ? CreatePaintObject(COLOUR.values()[SelectedColour]) : CreatePaintObject(COLOUR.ERASER);
            paintCopy.setStrokeWidth((brushSize +  + convertDpToPixel(15, getContext()))*2);
            //Save the current path
            linesDrawn.add(new Pair<Path, Paint>(currentPath, paintCopy));
            currentPath = null;
            //mCanvas.drawPath(currentPath,  currentPaint);
            // kill this so we don't double draw
            //mPath.reset();
            if (!drawingViews.peek().equals(this))
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // remove popup views if present
            if (PopupColour != null) {
                RemoveColourPopup();
            }
            else if (PopupBrushSize != null){
                RemoveBrushSizePopup();
            }

            //Get location of touch event
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }

        /** Create a File for saving an image or video */
        private File getOutputMediaFile(String filenameSuffix){
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            if (!directory.exists())
                return null;

            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = directory.getAbsolutePath() + File.separator + "IMG_"+ timeStamp + filenameSuffix + ".jpg";
            File mediaFile;
            mediaFile = new File(filename);

            return mediaFile;
        }
    }

    public class BrushSizeView extends View {

        Canvas sizeCanvas;

        public BrushSizeView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);
            sizeCanvas  = canvas;
            drawCircle();
        }

        public boolean onTouchEvent(MotionEvent event) {
            RemoveBrushSizePopup();
            return true;
        }

        public void drawCircle() {
            int x = getWidth();
            int y = getHeight();
            sizeCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            Paint cPaint = new Paint();
            if (SelectedColour == COLOUR.YELLOW.getValue())
                cPaint.setARGB(255, 255, 255, 0);
            else if (SelectedColour == COLOUR.PINK.getValue())
                cPaint.setARGB(255, 245, 17, 233);
            else if (SelectedColour == COLOUR.GREEN.getValue())
                cPaint.setARGB(255, 0, 255, 0);
            else if (SelectedColour == COLOUR.BLUE.getValue())
                cPaint.setARGB(255, 0, 255, 255);
            cPaint.setStyle(Paint.Style.STROKE);
            cPaint.setStrokeWidth(3);
            cPaint.setPathEffect(new DashPathEffect(new float[]{15, 20}, 0)); // Make it a dotted line
            cPaint.setAntiAlias(true);
            cPaint.setDither(true);
            sizeCanvas.drawCircle(x / 2, y / 2,  + convertDpToPixel(15, getContext()) + brushSize, cPaint);
        }
    }
}
