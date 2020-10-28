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

public class AutoCompleteAuthorAdapter extends ArrayAdapter<String> {

    //All authors in database
    private List<String> authorListFull;

    //Constructor
    public AutoCompleteAuthorAdapter(@NonNull Context context, @NonNull List<String> authorList) {
        super(context, 0, authorList);
        authorListFull = new ArrayList<>(authorList);
    }

    public Filter getFilter(){
        return authorFilter;
    }

    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent){
        //Create main view if it doesnt already exist
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_row_book_author, parent, false
            );
        }

        //View that will contain authors name
        TextView authorView = convertView.findViewById(R.id.txtListAuthor2);
        //Set TextView to the authors name
        String author = getItem(position);
        if (author != null){
            authorView.setText(author);
        }

        //Return view for single author
        return convertView;
    }

    private Filter authorFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<String> suggestions = new ArrayList<>();

            //Returns all results if nothing has been searched
            if (constraint == null || constraint.length() == 0){
                suggestions.addAll(authorListFull);
            }
            else{
                //Case insensitive
                String filterPattern = constraint.toString().toLowerCase().trim();

                //Get all authors begining with the searched string
                for (String author : authorListFull){
                    if (author.toLowerCase().startsWith(filterPattern)){
                        suggestions.add(author);
                    }
                }
            }
            //return filtered results
            filterResults.values = suggestions;
            filterResults.count = suggestions.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //Refeshes results
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((String) resultValue);
        }
    };
}
