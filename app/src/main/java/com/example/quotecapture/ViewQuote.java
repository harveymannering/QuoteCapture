package com.example.quotecapture;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ViewQuote extends AppCompatActivity {


    Database database;
    SQLiteDatabase db;
    Quote quote;
    boolean fromHighlighterPage;

    //Book/Author popup dialog variables
    ArrayList<Book> books;
    AutoCompleteTextView titleTxt;
    AutoCompleteTextView authorTxt;
    //
    TextView quoteTextView;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Hide the status bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_view_quote);

        context = this;

        //Read quote from database
        database = new Database(this);
        db = database.getWritableDatabase();



    }

    @Override
    protected void onResume() {
        super.onResume();
        int quoteID = (int) getIntent().getExtras().getLong("QUOTE_ID");
        quote = database.getQuote(db, quoteID);
        //Textbox
        quoteTextView = (TextView) findViewById(R.id.quoteText);
        quoteTextView.setText(quote.getQuoteText());

        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quoteTextView.requestFocus();
            }
        });

        File imgFile = new File(quote.getCombinedImageURI());

        if(imgFile.exists()){

            //Get screen dimensions
            Display display = getWindowManager().getDefaultDisplay();
            Point displaySize = new Point();
            display.getSize(displaySize);

            //Load images
            Bitmap bitmapImage;
            try {
                bitmapImage = BitmapFactory.decodeStream(new FileInputStream(imgFile)).copy(Bitmap.Config.ARGB_8888, true);
            }
            catch(Exception e){
                bitmapImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            }

            ImageView imView = (ImageView) findViewById(R.id.image1);
            imView.setImageBitmap(bitmapImage);
        }


        //Button onClick listeners
        //Copy quote button
        ImageButton copyBtn = findViewById(R.id.quotesCopyButton);
        copyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                TextView textView = (TextView) findViewById(R.id.quoteText);
                if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(textView.getText());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Quote",textView.getText());
                    clipboard.setPrimaryClip(clip);
                }
                Toast.makeText(context, getResources().getString(R.string.copy_toast_text), Toast.LENGTH_SHORT).show();
            }
        });

        //Delete button
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.quotesDeleteButton);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                database.removeQuote(db, quote.getQuoteId(), context);
                                backToQuoteList();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(getResources().getString(R.string.deleteWarning))
                        .setPositiveButton(getResources().getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getResources().getString(R.string.no), dialogClickListener).show();
            }


        });


        //Go back button
        ImageButton backBtn = (ImageButton) findViewById(R.id.quotesBackButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToQuoteList();
            }
        });

        //Edit the highlighting button
        ImageButton editBtn = findViewById(R.id.quotesEditButton);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set up new activity
                Intent intent  = new Intent(context, PictureTakenActivity.class);
                intent.putExtra("FILENAME", quote.getImageURI());
                intent.putExtra("QUOTE_ID", quote.getQuoteId());
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //Get list all books from the database
        books = database.getAllBooks(db);
        //Add books and authors to lists
        Book[] bookTitles = new Book[books.size()];
        List<String> authors = new ArrayList<String>();
        for (int i = 0; i < books.size(); i++){
            //add titles of books to the list
            bookTitles[i] = new Book(books.get(i).getBookId(), books.get(i).getTitle(), books.get(i).getAuthor());
            //Add author provided it isnt already in the list
            boolean alreadyExist = false;
            for (int j = 0; j < authors.size(); j++) {
                if (authors.get(j).equals(books.get(i).getAuthor()))
                    alreadyExist = true;
            }
            if (alreadyExist == false)
                authors.add(books.get(i).getAuthor());
        }

        //Populate Autocomplete edittext (book titles)
        titleTxt = (AutoCompleteTextView) findViewById(R.id.titleTxt);
        ArrayAdapter<Book> titlesAdapter = new AutoCompleteTitleAdapter(this, books);
        titleTxt.setThreshold(1);
        titleTxt.setAdapter(titlesAdapter);
        titleTxt.setText(quote.getBook().getTitle());

        //Populate Autocomplete edittext (authors text box)
        authorTxt = (AutoCompleteTextView) findViewById(R.id.authorTxt);
        ArrayAdapter<String> authorsAdapter = new AutoCompleteAuthorAdapter(this, authors);
        authorTxt.setThreshold(1);
        authorTxt.setAdapter(authorsAdapter);
        authorTxt.setText(quote.getBook().getAuthor());

        //Set up auto complete.  By selecting a book title, the corresponding author field should be filled in
        titleTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                authorTxt.setText(books.get(position).getAuthor());
                int i = parent.getSelectedItemPosition();

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        //Get user input
        String title = titleTxt.getText().toString();
        String author = authorTxt.getText().toString();
        String quoteText = quoteTextView.getText().toString();

        //Update quote text
        database.updateQuoteText(db, quote.getQuoteId(), quoteText);

        //Update local quote data
        //quote.getBook().setTitle(title);
        //quote.getBook().setAuthor(author);

        //Remove book id from quote id database if fields are empty
        if (title.equals("") && author.equals("")) {
            database.updateQuotesBookId(db, quote.getQuoteId(), 0);
            return;
        }

        //Find book if already exists
        int preexistingId = 0;
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getTitle().equals(title) && books.get(i).getAuthor().equals(author)) {
                preexistingId = books.get(i).getBookId();
                break;
            }
        }

        //Add book to database
        if (preexistingId > 0)
            database.updateQuotesBookId(db, quote.getQuoteId(), preexistingId);
        else
            database.addBook(db, quote.getQuoteId(), title, author);

    }

    /*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("quoteID", quoteID);
    }*/

    private void backToQuoteList(){

        //Clear the backstack
                    /*
                    if (fromHighlighterPage == false){
                        FragmentManager fragmentManager = getFragmentManager();
                        // this will clear the back stack and displays no animation on the screen
                        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }*/
        //if (fromHighlighterPage == false){
        //getFragmentManager().popBackStack();
        //}
        //else{
        Intent intent = new Intent(this, MainActivity.class);
        MainActivity.pagerPosition = 1;
        MainActivity.viewPager.setCurrentItem(1);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        //}

        //getActivity().finishAffinity();
    }
}
