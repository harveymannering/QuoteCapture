package com.businesscompany.quotecapture;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class CameraFragment extends Fragment{



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        FrameLayout fragmentContainer = (FrameLayout) view.findViewById(R.id.camera_fragment);
        if (fragmentContainer != null){
            //Sets up the first fragment
            CameraPreviewFragment quoteListQuote = new CameraPreviewFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.camera_fragment, quoteListQuote);

            //Fragment transition parameter
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

            //Commits the fragment
            ft.commit();
        }
        return view;
    }


}
