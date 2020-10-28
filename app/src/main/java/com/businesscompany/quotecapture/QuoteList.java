package com.businesscompany.quotecapture;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.ListFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.util.ArrayList;
import java.util.List;

public class QuoteList extends ListFragment {

    static interface Listener {
        void itemClicked(long id);
    };

    ArrayList<Quote> quotes;
    private Listener listener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get all quotes currently stored
        Database database = new Database(getContext());
        SQLiteDatabase db = database.getReadableDatabase();
        quotes = database.getAllQuotes(db);

        //Only quote ids are needed
        List<String> names = new ArrayList<String>();
        for (Quote q : quotes)
            names.add(q.getQuoteId() + "");

        setListAdapter(new MyListAdapter(getContext(), quotes));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (Listener) context;
    }

    //Returns quoteID for a selected quote
    public void onListItemClick(ListView listView, View itemView, int position, long id) {
        if (listener != null) {
            listener.itemClicked(quotes.get(position).getQuoteId());
        }
    }

    class MyListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Quote> quotes;

        public MyListAdapter(Context context, ArrayList<Quote> quotes) {
            this.context = context;
            this.quotes = quotes;
        }

        @Override
        public int getCount() {
            return quotes.size();
        }

        @Override
        public Object getItem(int position) {
            return quotes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return quotes.get(position).getQuoteId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //Two line list item, list format already built into android
            TwoLineListItem twoLineListItem;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                twoLineListItem = (TwoLineListItem) inflater.inflate(
                        android.R.layout.simple_list_item_2, null);
            } else {
                twoLineListItem = (TwoLineListItem) convertView;
            }

            //Two label views that will store the Quote + book title
            TextView text1 = twoLineListItem.getText1();
            TextView text2 = twoLineListItem.getText2();

            //... at the end if quote is too long
            text1.setEllipsize(TextUtils.TruncateAt.END);
            text1.setMaxLines(2);
            //Set quotes text
            text1.setText("\"" + quotes.get(position).getQuoteText() + "\"");
            //Build subtext string
            String subtext = "";
            if (quotes.get(position).getBook() != null) {
                //Text will be TITLE by AUTHOR format, unless one of those values doesnt exist
                if (quotes.get(position).getBook().getTitle() != null && !quotes.get(position).getBook().getTitle().equals("")) {
                    subtext += quotes.get(position).getBook().getTitle();
                    if (quotes.get(position).getBook().getAuthor() != null && !quotes.get(position).getBook().getAuthor().equals(""))
                        subtext += " by ";
                }
                if (quotes.get(position).getBook().getAuthor() != null && !quotes.get(position).getBook().getAuthor().equals(""))
                    subtext += quotes.get(position).getBook().getAuthor();
            }
            text2.setText(subtext);

            return twoLineListItem;
        }
    }

}