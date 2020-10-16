package com.businesscompany.quotecapture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
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
        //ActivityCompat.RequestPermissionsRequestCodeValidator

        //Hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        BuildUI();

    }

    //Add the pager to the screen which contains the camera and the quotes list
    private void BuildUI(){
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));

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
        viewPager.setCurrentItem(pagerPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pagerPosition = viewPager.getCurrentItem();
    }

    @Override
    public void itemClicked(long id) {
        //Set up new fragment and fragment manager
        /*ViewQuoteFragment viewQuote = new ViewQuoteFragment(false);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.small_fragment_container, viewQuote, "ViewQuoteFragment");



        //Fragment transition parameters
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

        viewQuote.setQuoteID(id);*/
        Intent viewQuote = new Intent(this, ViewQuote.class);
        viewQuote.putExtra("QUOTE_ID", id);
        //viewQuote.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(viewQuote);
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



}
