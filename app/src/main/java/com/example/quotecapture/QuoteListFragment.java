package com.example.quotecapture;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


public class QuoteListFragment extends Fragment {

    //Views
    View view;
    Button btnSortBy;
    View popupView;

    //Database objects
    Database database;
    SQLiteDatabase db;

    public static enum SortingOrder {
        DateDsc(0), BookDsc(1), AuthorDsc(2), AlphabetDsc(3),
        DateAsc(4), BookAsc(5), AuthorAsc(6), AlphabetAsc(7);

        private final int value;
        private SortingOrder(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SortingOrder fromInteger(int x) {
            switch(x) {
                case 0:
                    return DateDsc;
                case 1:
                    return BookDsc;
                case 2:
                    return AuthorDsc;
                case 3:
                    return AlphabetDsc;
                case 4:
                    return DateAsc;
                case 5:
                    return BookAsc;
                case 6:
                    return AuthorAsc;
                case 7:
                    return AlphabetAsc;

            }
            return null;
        }

    };

    SortingOrder sortOrder;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //Database objects
        database = new Database(getContext());
        db = database.getReadableDatabase();

        //Set how the list will be sorted
        sortOrder = SortingOrder.fromInteger(database.getSortOrder(db));

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_quote_list, container, false);
        RefreshList();

        //Sort button functionality
        btnSortBy = view.findViewById(R.id.btnSortBy);
        btnSortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                popupView = inflater.inflate(R.layout.popup_sort_by, null);

                // create the popup window
                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, displayMetrics);
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                Rect location = locateView(btnSortBy);
                popupWindow.showAtLocation(view, Gravity.TOP|Gravity.LEFT, location.right - width  - 5, location.bottom + 10);


                //Create list
                String[] ListText = {
                        getResources().getString(R.string.sortByList1),
                        getResources().getString(R.string.sortByList2),
                        getResources().getString(R.string.sortByList3),
                        getResources().getString(R.string.sortByList4)
                };

                int[] ListImages = {0,0,0,0};

                if (sortOrder.getValue() > 3)
                    ListImages[sortOrder.getValue() - 4] = R.drawable.ic_arrow_upward_black_24dp;
                else
                    ListImages[sortOrder.getValue()] = R.drawable.ic_arrow_downward_black_24dp;


                List<HashMap<String, String>> viewMappings = new ArrayList<HashMap<String, String>>();
                for (int x = 0; x < ListText.length; x++){
                    HashMap<String, String> hm = new HashMap<String, String>();
                    hm.put("ListTitle", ListText[x]);
                    hm.put("ListIcon", Integer.toString(ListImages[x]));
                    viewMappings.add(hm);
                }
                String[] keys = {"ListTitle", "ListIcon"};
                int[] displayViews = {R.id.SortByListText, R.id.SortbyDirection};
                SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), viewMappings, R.layout.list_row_sort, keys, displayViews);
                ListView sortByList = popupView.findViewById(R.id.SortByList);
                sortByList.setAdapter(simpleAdapter);
                sortByList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //Set what field the list will be ordered by
                        if (sortOrder.getValue() == position){
                            sortOrder = SortingOrder.fromInteger(position + 4);
                        }
                        else if (sortOrder.getValue() == position + 4){
                            sortOrder = SortingOrder.fromInteger(position);
                        }
                        else {
                            sortOrder = SortingOrder.fromInteger(position);
                        }

                        //Disappear inimation
                        popupView.animate().setDuration(100).alpha(0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                popupWindow.dismiss();
                            }
                        });

                        database.updateSettings(db, sortOrder.getValue());

                        //Refesh page
                        RefreshList();
                    }
                });

                //Appear animation
                AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(50);
                anim.setRepeatMode(Animation.REVERSE);
                popupView.startAnimation(anim);


                // dismiss the popup window when touched
                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        return true;
                    }
                });
            }
        });
        return view;
    }

    private void RefreshList(){
        QuoteList quoteListQuote = new QuoteList();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.list_fragment_container, quoteListQuote);

        //Fragment transition parameter
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        //Commits the fragment
        ft.commit();
    }

    public static Rect locateView(View v)
    {
        int[] loc_int = new int[2];
        if (v == null) return null;
        try
        {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe)
        {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }


}
