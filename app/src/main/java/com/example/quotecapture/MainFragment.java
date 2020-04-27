package com.example.quotecapture;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    boolean fromHighlightingPage;
    long quoteID;

    public MainFragment(boolean fromHighlightingPage, long quoteID){
        this.fromHighlightingPage = fromHighlightingPage;
        this.quoteID = quoteID;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        FrameLayout fragmentContainer = (FrameLayout) view.findViewById(R.id.small_fragment_container);
        if (fragmentContainer != null){
            if (fromHighlightingPage == false) {
                //Sets up the first fragment
                QuoteListFragment quoteListQuote = new QuoteListFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.small_fragment_container, quoteListQuote);

                //Fragment transition parameter
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

                //Commits the fragment
                ft.commit();
            }
            else{
                ViewQuoteFragment viewQuoteFrag = new ViewQuoteFragment(true);
                viewQuoteFrag.setQuoteID(quoteID);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.small_fragment_container, viewQuoteFrag);

                //Fragment transition parameter
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

                //Commits the fragment
                ft.commit();
            }
        }


        return view;
    }

}
