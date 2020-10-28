package com.businesscompany.quotecapture;

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

    //Contains all book titles and their repsective authors
    private List<Book> bookListFull;

    //constructor
    public AutoCompleteTitleAdapter(@NonNull Context context, @NonNull List<Book> bookList) {
        super(context, 0, bookList);
        bookListFull = new ArrayList<>(bookList);
    }

    public Filter getFilter(){
        return bookFilter;
    }

    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent){
        //Creates the view containing two labels (one for the title one for the author)
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_row_book_title, parent, false
            );
        }
        TextView titleView = convertView.findViewById(R.id.txtListTitle);
        TextView authorView = convertView.findViewById(R.id.txtListAuthor);

        //Sets the value of the labels
        Book book = getItem(position);
        if (book != null){
            titleView.setText(book.getTitle());
            authorView.setText(book.getAuthor());
        }

        //Return view
        return convertView;
    }

    private Filter bookFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<Book> suggestions = new ArrayList<>();

            //Returns all books if nothing is searched
            if (constraint == null || constraint.length() == 0){
                suggestions.addAll(bookListFull);
            }
            else{

                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Book book: bookListFull){
                    //Filters just by title, but title and author are returned
                    if (book.getTitle().toLowerCase().startsWith(filterPattern)){
                        suggestions.add(book);
                    }
                }
            }
            //return results
            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //Refesh results
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
