package com.example.quotecapture;

public class Book {

    private int bookId;
    private String title;
    private String author;

    public Book(int id){
        bookId = id;
    }

    public Book(int id, String title, String author){
        bookId = id;
        this.title = title;
        this.author = author;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author == null ? "" : author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


}
