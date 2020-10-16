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

    private List<String> authorListFull;

    public AutoCompleteAuthorAdapter(@NonNull Context context, @NonNull List<String> authorList) {
        super(context, 0, authorList);
        authorListFull = new ArrayList<>(authorList);
    }

    public Filter getFilter(){
        return authorFilter;
    }

    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent){
        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_row_book_author, parent, false
            );
        }

        TextView authorView = convertView.findViewById(R.id.txtListAuthor2);

        String author = getItem(position);
        if (author != null){
            authorView.setText(author);
        }

        return convertView;
    }

    private Filter authorFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            List<String> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0){
                suggestions.addAll(authorListFull);
            }
            else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (String author : authorListFull){
                    if (author.toLowerCase().startsWith(filterPattern)){
                        suggestions.add(author);
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
            return ((String) resultValue);
        }
    };
}
