package com.example.quotecapture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteTitleAdapter extends ArrayAdapter<Book> {

    private List<Book> bookListFull;

    public AutoCompleteTitleAdapter(@NonNull Context context, @NonNull List<Book> bookList) {
        super(context, 0, bookList);
        bookListFull = new ArrayList<>(bookList);
    }

    public Filter getFilter(){
        return bookFilter;
    }

    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_row_book_title, parent, false
            );
        }

        TextView titleView = convertView.findViewById(R.id.txtListTitle);
        TextView authorView = convertView.findViewById(R.id.txtListAuthor);

        Book book = getItem(position);
        if (book != null){
            titleView.setText(book.getTitle());
            authorView.setText(book.getAuthor());
        }

        return convertView;
    }

    private Filter bookFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<Book> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                suggestions.addAll(bookListFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Book book: bookListFull){
                    if (book.getTitle().toLowerCase().startsWith(filterPattern)){
                        suggestions.add(book);
                    }
                }
            }
            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Book) resultValue).getTitle();
        }
    };
}
