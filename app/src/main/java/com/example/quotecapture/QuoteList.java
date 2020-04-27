package com.example.quotecapture;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import java.lang.reflect.Array;
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

        Database database = new Database(getContext());
        SQLiteDatabase db = database.getReadableDatabase();
        quotes = database.getAllQuotes(db);
        List<String> names = new ArrayList<String>();
        for (Quote q : quotes)
            names.add(q.getQuoteId() + "");

            /*ArrayAdapter<Quote> adapter = new ArrayAdapter<Quote>(
                    inflater.getContext(),
                    android.R.layout.,
                    quotes
            ); */

        setListAdapter(new MyAdapter(getContext(), quotes));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.listener = (Listener) context;
    }

    public void onListItemClick(ListView listView, View itemView, int position, long id) {
        if (listener != null) {
            listener.itemClicked(quotes.get(position).getQuoteId());
        }
    }

    class MyAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<Quote> quotes;

        public MyAdapter(Context context, ArrayList<Quote> quotes) {
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

            TwoLineListItem twoLineListItem;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                twoLineListItem = (TwoLineListItem) inflater.inflate(
                        android.R.layout.simple_list_item_2, null);
            } else {
                twoLineListItem = (TwoLineListItem) convertView;
            }

            TextView text1 = twoLineListItem.getText1();
            TextView text2 = twoLineListItem.getText2();

            text1.setEllipsize(TextUtils.TruncateAt.END);
            text1.setMaxLines(2);
            text1.setText("\"" + quotes.get(position).getQuoteText() + "\"");
            String subtext = "";
            if (quotes.get(position).getBook() != null) {
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