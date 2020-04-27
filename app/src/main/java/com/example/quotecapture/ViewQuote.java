package com.example.quotecapture;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

public class ViewQuote extends AppCompatActivity {

    public static final String QUOTE_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_quote);
        ViewQuoteFragment frag  = (ViewQuoteFragment) getSupportFragmentManager().findFragmentById(R.id.quote_fragment);


        int quote_id = (int) getIntent().getExtras().get(QUOTE_ID);
        frag.setQuoteID(quote_id);
    }
}
