package com.example.quotecapture;

import java.util.Date;

public class Quote {

    private int quoteId;
    private String quoteText;
    private Date date;
    private Book book;
    private String imageURI;
    private String highlightedImagedURI;
    private String combinedImageURI;


    Quote(String q){
        quoteText = q;
        date = new Date();
        book= new Book(0);
        imageURI = "";
        highlightedImagedURI = "";
        combinedImageURI = "";
    }


    Quote(int id, String text, Date d, Book b, String URI, String highlightedURI, String combinedURI){
        quoteId = id;
        quoteText = text;
        date = d;
        book = b;
        imageURI = URI;
        highlightedImagedURI = highlightedURI;
        combinedImageURI = combinedURI;
    }

    public String getCombinedImageURI() {
        return  combinedImageURI == null ? "" : combinedImageURI;
    }

    public void setCombinedImageURI(String combinedImageURI) {
        this.combinedImageURI = combinedImageURI;
    }

    public int getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(int quoteId) {
        this.quoteId = quoteId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public String getImageURI() {
        return imageURI == null ? "" : imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public String getHighlightedImagedURI() {
        return highlightedImagedURI  == null ? "" : highlightedImagedURI;
    }

    public void setHighlightedImagedURI(String highlightedImagedURI) {
        this.highlightedImagedURI = highlightedImagedURI;
    }

    public String getQuoteText(){
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }
}
