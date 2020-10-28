package com.businesscompany.quotecapture;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "QuoteCaptureDB";
    private static final int DB_VERSION = 5;

    Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        updateMyDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateMyDatabase(db, oldVersion, newVersion);
    }

    public void updateMyDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1){
            //QUOTE TABLE
            //ID  TEXT  DATE  BOOK  IMAGE  HIGHLIGHTING  COMBINED_IMAGE
            db.execSQL("CREATE TABLE QUOTE " +
                    "(_id INTEGER PRIMARY KEY, " +
                    "QUOTE_TEXT TEXT, " +
                    "DATE DATETIME, " +
                    "BOOK_ID INT, " +
                    "IMAGE_URI TEXT, " +
                    "HIGHLIGHTED_URI TEXT," +
                    "COMBINED_IMAGE_URI TEXT)");


        }

        if (oldVersion < 2) {
            // SETTINGS TABLE
            //  SORT_BY: 0 = Date, 1 = Book, 2 = Author, 3 = Alphabetical, 4 = Date Ascending,
            //           5 = Book Ascending, 6 = Author Ascending, 7 = Alphabetical Ascending
            db.execSQL("CREATE TABLE SETTINGS " +
                    "(SORT_BY INT)");

            //Add single row to settings table
            ContentValues settingsDefaults = new ContentValues();
            settingsDefaults.put("SORT_BY", 0);
            long book_id = db.insert("SETTINGS", null, settingsDefaults);
        }

        if (oldVersion < 3 ){
            //BOOK TABLE
            //ID  TITLE  AUTHOR
            db.execSQL("CREATE TABLE BOOK " +
                    "(_id INTEGER PRIMARY KEY, " +
                    "TITLE TEXT, " +
                    "AUTHOR TEXT)");
        }

        if (oldVersion < 4){
            //LOG TABLE
            //DATE  MESSAGE
            db.execSQL("CREATE TABLE DEVELOPER_LOG " +
                    "(_id INTEGER PRIMARY KEY, " +
                    "DATE DATETIME," +
                    "MESSAGE TEXT)");
            db.execSQL("ALTER TABLE SETTINGS ADD COLUMN DEVELOPER_MODE INT DEFAULT 0");
        }

        if (oldVersion < 5){
            //DELETE THE LOG TABLE
            db.execSQL("DROP TABLE DEVELOPER_LOG");
        }
    }

    public void updateSettings(SQLiteDatabase db, int sortBy){
        //Define input parameters
        ContentValues s = new ContentValues();
        s.put("SORT_BY", sortBy);

        //Update database
        db.update("SETTINGS", s, null, null);
    }

    public void setDeveloperMode(SQLiteDatabase db, boolean developerMode){
        //Define input parameters
        ContentValues s = new ContentValues();
        s.put("DEVELOPER_MODE", developerMode ? 1 : 0);

        //Update database
        db.update("SETTINGS", s, null, null);
    }

    public boolean isDeveloperModeOn(SQLiteDatabase db){
        //Set what the settings are
        int developer_mode = 0;
        try{
            //Define cursor for accessing data
            Cursor cursor = db.query("SETTINGS",
                    new String[] {
                            "DEVELOPER_MODE INT",
                    },
                    null, null, null, null, null);

            //Move cursor to first record
            if (cursor.moveToFirst())
                developer_mode = cursor.getInt(0);

            //Close database resources
            cursor.close();

        } catch (Exception e) {

        }
        return developer_mode == 0 ? false : true;
    }

    //Adds a quote to the table
    public long addQuote(SQLiteDatabase db, String quoteText, Date date, int bookID, String imageURI, String highlightedURI, String combinedURI){
        //Useful objects
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        ContentValues quoteValues = new ContentValues();

        //Add parameters
        quoteValues.put("QUOTE_TEXT", quoteText);
        quoteValues.put("DATE", dateFormat.format(date));
        quoteValues.put("BOOK_ID", bookID);
        quoteValues.put("IMAGE_URI", imageURI);
        quoteValues.put("HIGHLIGHTED_URI", highlightedURI);
        quoteValues.put("COMBINED_IMAGE_URI", combinedURI);

        //Run the SQL (and returns the id)
        return db.insert("QUOTE", null, quoteValues);
    }

    public void removeQuote(SQLiteDatabase db, long quoteID, Context context){
        //Delete quote from database
        Quote q = getQuote(db, quoteID);
        db.delete("QUOTE", "_id = " + quoteID, null);

        //Delete images from internal storage
        DeleteInternalImage(q.getImageURI());
        DeleteInternalImage(q.getHighlightedImagedURI());
        DeleteInternalImage(q.getCombinedImageURI());
    }

    //Gets all data retaining to specific quote
    public Quote getQuote(SQLiteDatabase db, long id){
        //Default return value is a error
        Quote returnQuote = new Quote(0, "Error",  new Date(), new Book(0), "","", "");
        //Used to read dates from the database
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try{
            //Define cursor for accessing data
            Cursor quoteCursor = db.query("QUOTE",
                    new String[] {
                            "_id",
                            "QUOTE_TEXT",
                            "DATE",
                            "BOOK_ID",
                            "IMAGE_URI",
                            "HIGHLIGHTED_URI",
                            "COMBINED_IMAGE_URI",
                    },
                    "_id = " + id, null, null, null, null);

            //Move cursor to first record
            if (quoteCursor.moveToFirst()){
                returnQuote = new Quote(
                        quoteCursor.getInt(0),
                        quoteCursor.getString(1),
                        dateFormat.parse(quoteCursor.getString(2)),
                        new Book(quoteCursor.getInt(3)),
                        quoteCursor.getString(4),
                        quoteCursor.getString(5),
                        quoteCursor.getString(6)
                );
                //Get book info
                int bookId = quoteCursor.getInt(3);
                Cursor bookCursor = db.query("BOOK",
                        new String[] {
                                "TITLE",
                                "AUTHOR",
                        },
                        "_id = " + bookId, null, null, null, null);
                if (bookCursor.moveToFirst()) {
                    Book b = new Book(bookId, bookCursor.getString(0), bookCursor.getString(1));
                    returnQuote.setBook(b);
                    bookCursor.close();
                }
            }

            //Close database resources
            quoteCursor.close();

        } catch (Exception e) {

        }

        return returnQuote;
    }

    public ArrayList<Quote> getAllQuotes(SQLiteDatabase db){
        ArrayList<Quote> returnQuotes = new ArrayList<Quote>();
        //Used to read dates from the database
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try{
            //Define cursors for accessing data
            Cursor quoteCursor = db.query("QUOTE",
                    new String[] {
                            "_id",
                            "QUOTE_TEXT",
                            "DATE DATETIME",
                            "BOOK_ID INT",
                            "IMAGE_URI TEXT",
                            "HIGHLIGHTED_URI TEXT",
                            "COMBINED_IMAGE_URI",
                    },
                    null, null, null, null, null);


            int bookId = 0;

            //Move cursor to first record
            if (quoteCursor.moveToFirst()){
                //Get Quote info
                returnQuotes.add(new Quote(
                        quoteCursor.getInt(0),
                        quoteCursor.getString(1),
                        dateFormat.parse(quoteCursor.getString(2)),
                        new Book(quoteCursor.getInt(3)),
                        quoteCursor.getString(4),
                        quoteCursor.getString(5),
                        quoteCursor.getString(6)
                ));
                //Get book info
                bookId = quoteCursor.getInt(3);
                Cursor bookCursor = db.query("BOOK",
                        new String[] {
                                "TITLE",
                                "AUTHOR",
                        },
                        "_id = " + bookId, null, null, null, null);
                if (bookCursor.moveToFirst()) {
                    Book b = new Book(bookId, bookCursor.getString(0), bookCursor.getString(1));
                    returnQuotes.get(returnQuotes.size() - 1).setBook(b);
                    bookCursor.close();
                }
            }

            //Loop until all records have been processed
            while (quoteCursor.moveToNext()) {
                returnQuotes.add(new Quote(
                        quoteCursor.getInt(0),
                        quoteCursor.getString(1),
                        dateFormat.parse(quoteCursor.getString(2)),
                        new Book(quoteCursor.getInt(3)),
                        quoteCursor.getString(4),
                        quoteCursor.getString(5),
                        quoteCursor.getString(6)
                ));
                //Get book info
                bookId = quoteCursor.getInt(3);
                Cursor bookCursor = db.query("BOOK",
                        new String[] {
                                "TITLE",
                                "AUTHOR",
                        },
                        "_id = " + bookId, null, null, null, null);
                if (bookCursor.moveToFirst()) {
                    Book b = new Book(bookId, bookCursor.getString(0), bookCursor.getString(1));
                    returnQuotes.get(returnQuotes.size() - 1).setBook(b);
                    bookCursor.close();
                }
            }

            //Close database resources
            quoteCursor.close();

        } catch (Exception e) {

        }

        return SortQuotes(db, returnQuotes);
    }

    public void updateQuoteText(SQLiteDatabase db, long id, String quoteText){
        //Define input parameters
        ContentValues q = new ContentValues();
        q.put("QUOTE_TEXT", quoteText);

        //Update database
        db.update("QUOTE", q, "_id = " + id, null);
    }

    public void updateQuote(SQLiteDatabase db, long id, String quoteText, String highlightedURL, String combinedURI){
        //delete old image from storage
        Quote oldQuote = getQuote(db, id);
        DeleteInternalImage(oldQuote.getHighlightedImagedURI());
        DeleteInternalImage(oldQuote.getCombinedImageURI());

        //Define input parameters
        ContentValues q = new ContentValues();
        q.put("QUOTE_TEXT", quoteText);
        q.put("HIGHLIGHTED_URI", highlightedURL);
        q.put("COMBINED_IMAGE_URI", combinedURI);

        //Update database
        db.update("QUOTE", q, "_id = " + id, null);
    }

    public void addBook(SQLiteDatabase db, long quoteId, String title, String author){
        ContentValues bookValues = new ContentValues();

        //Add parameters
        bookValues.put("TITLE", title);
        bookValues.put("AUTHOR", author);

        //Run the SQL (and returns the id)
        long book_id = db.insert("BOOK", null, bookValues);
        updateQuotesBookId(db, quoteId, book_id);
    }

    public void updateQuotesBookId(SQLiteDatabase db, long quoteId, long bookId){
        //Update quote table
        ContentValues quote = new ContentValues();
        quote.put("BOOK_ID", bookId);

        //Update database
        db.update("QUOTE", quote, "_id = " + quoteId, null);

        removeUnusedBooks(db);
    }

    public void removeUnusedBooks(SQLiteDatabase db){
        //All quotes and all books
        ArrayList<Quote> quotes = getAllQuotes(db);
        ArrayList<Book> books = getAllBooks(db);

        //Unused book ids
        ArrayList<Integer> unusedBooks = new ArrayList<Integer>();
        for (int b = 0; b < books.size(); b++){
            boolean bookFound = false;
            for (int q = 0; q < quotes.size(); q++){
                if (quotes.get(q).getBook().getBookId() == books.get(b).getBookId()){
                    bookFound = true;
                    break;
                }
            }
            if (bookFound == false)
                removeBook(db, books.get(b).getBookId());
        }
    }


    //Delete book from the database
    public void removeBook(SQLiteDatabase db, int bookID){
        db.delete("BOOK", "_id = " + bookID, null);
    }

    //Gets all book currently in the database
    public ArrayList<Book> getAllBooks(SQLiteDatabase db){
        ArrayList<Book> returnBooks = new ArrayList<Book>();

        Cursor cursortemp = db.rawQuery("SELECT * FROM BOOK", null);

        if (cursortemp.moveToFirst()) {
            while ( !cursortemp.isAfterLast() ) {

                cursortemp.moveToNext();
            }
        }

        //Used to read dates from the database
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try{
            //Define cursor for accessing data
            Cursor cursor = db.query("BOOK",
                    new String[] {
                            "_id INT",
                            "TITLE TEXT",
                            "AUTHOR TEXT",
                    },
                    null, null, null, null, null);

            //Move cursor to first record
            if (cursor.moveToFirst()){
                returnBooks.add(new Book(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            }

            //Loop until all records have been processed
            while (cursor.moveToNext()) {
                returnBooks.add(new Book(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            }

            //Close database resources
            cursor.close();

        } catch (Exception e) {

        }
        return returnBooks;
    }

    public int getSortOrder(SQLiteDatabase db){
        //Set what the settings are
        int sortOrder = 0;
        try{
            //Define cursor for accessing data
            Cursor cursor = db.query("SETTINGS",
                    new String[] {
                            "SORT_BY INT",
                    },
                    null, null, null, null, null);

            //Move cursor to first record
            if (cursor.moveToFirst())
                sortOrder = cursor.getInt(0);

            //Close database resources
            cursor.close();

        } catch (Exception e) {

        }
        return sortOrder;
    }

    private ArrayList<Quote> SortQuotes(SQLiteDatabase db, ArrayList<Quote> returnQuotes){

        int sortOrder = getSortOrder(db);

        // Date Descending
        if (sortOrder == 0)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    return q1.getDate().compareTo(q2.getDate());
                }
            });
            // Book Descending
        else if (sortOrder == 1)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    String book1 = q1.getBook() != null ?
                            q1.getBook().getTitle().trim() + " " + q1.getBook().getAuthor().trim() : "";
                    String book2 = q2.getBook() != null ?
                            q2.getBook().getTitle().trim() + " " + q2.getBook().getAuthor().trim() : "";
                    return book1.compareTo(book2);
                }
            });
            // Author Descending
        else if (sortOrder == 2)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    String book1 = q1.getBook().getAuthor().trim() + " " + q1.getBook().getTitle().trim();
                    String book2 = q2.getBook().getAuthor().trim() + " " + q2.getBook().getTitle().trim();
                    return book1.compareTo(book2);
                }
            });
            // Alphabet Descending
        else if (sortOrder == 3)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    return q1.getQuoteText().compareTo(q2.getQuoteText());
                }
            });

            // Date Ascending
        else if (sortOrder == 4)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    return q2.getDate().compareTo(q1.getDate());
                }
            });
            // Book Ascending
        else if (sortOrder == 5)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    String book2 = q1.getBook().getTitle().trim() + " " + q1.getBook().getAuthor().trim();
                    String book1 = q2.getBook().getTitle().trim() + " " + q2.getBook().getAuthor().trim();
                    return book1.compareTo(book2);
                }
            });
            // Author Ascending
        else if (sortOrder == 6)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    String book2 = q1.getBook().getAuthor().trim() + " " + q1.getBook().getTitle().trim();
                    String book1 = q2.getBook().getAuthor().trim() + " " + q2.getBook().getTitle().trim();
                    return book1.compareTo(book2);
                }
            });
            // Alphabet Ascending
        else if (sortOrder == 7)
            Collections.sort(returnQuotes, new Comparator<Quote>() {
                @Override
                public int compare(Quote q1, Quote q2) {
                    return q2.getQuoteText().compareTo(q1.getQuoteText());
                }
            });


        return returnQuotes;
    }

    //Removes picture user has taken once the quote referencing it has gone (this is like garbage collection)
    public static boolean DeleteInternalImage(String uri){

        File file = new File(uri);
        try{
            file.delete();
            return true;
        }
        catch(Exception e){
            return false;
        }

    }
}
