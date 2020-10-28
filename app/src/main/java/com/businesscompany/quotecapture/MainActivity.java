package com.businesscompany.quotecapture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements QuoteList.Listener {

    static ViewPager viewPager;
    public static int pagerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Request permissions if not already granted
        requestStoragePermission();

        //Hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        //Add all views including the camera display
        BuildUI();

    }

    //Add the pager to the screen which contains the camera and the quotes list
    private void BuildUI(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));

    }

    //This will be called after permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BuildUI();
    }

    @Override
    //Takes user to last page they were on
    protected void onResume() {
        super.onResume();
        viewPager.setCurrentItem(pagerPosition);
    }

    @Override
    //Remember where user was on the camera of quote list page
    protected void onPause() {
        super.onPause();
        pagerPosition = viewPager.getCurrentItem();
    }

    @Override
    public void itemClicked(long id) {
        //Used for the quote list
        Intent viewQuote = new Intent(this, ViewQuote.class);
        viewQuote.putExtra("QUOTE_ID", id);
        startActivity(viewQuote);
    }

    private void requestStoragePermission() {
        //Read image from file
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        //Write images to file
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        //Access to the camera
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            return;


        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA,
                },0);
    }



}
