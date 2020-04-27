package com.example.quotecapture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements QuoteList.Listener {

    static long QuoteID;
    static ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Request permissions if not already granted
        requestStoragePermission();
        //ActivityCompat.RequestPermissionsRequestCodeValidator

        //Hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        QuoteID = 0;
        if (getIntent() != null && getIntent().getExtras() != null)
            QuoteID = getIntent().getExtras().getLong("QUOTE_ID");
    }

    //Add the pager to the screen which contains the camera and the quotes list
    private void BuildUI(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));

        if (QuoteID > 0) {
            viewPager.setCurrentItem(1);
        }
    }

    //This will be called after permission is granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BuildUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BuildUI();
    }

    @Override
    public void itemClicked(long id) {
        //Set up new fragment and fragment manager
        ViewQuoteFragment viewQuote = new ViewQuoteFragment(false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.small_fragment_container, viewQuote, "ViewQuoteFragment");



        //Fragment transition parameters
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

        viewQuote.setQuoteID(id);
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            return;


        ActivityCompat.requestPermissions(this, new String[]
                {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA,
                },0);
    }


    public static class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position < 1){
                return new CameraFragment();
            }
            //this else if is for when a quote has just been read via OCR
            else if (QuoteID > 0) {
                return new MainFragment(true, QuoteID);
            }
            else{
                return new MainFragment(false, 0);
            }

        }
    }
}
