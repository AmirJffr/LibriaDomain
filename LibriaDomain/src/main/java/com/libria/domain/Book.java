package com.libria.domain;
import java.util.Objects;


public class Book {
    private String isbn;
    private String title;
    private String author;
    private int year;
    private String genre;
    private boolean available;



    public Book(String isbn, String title, String author, int year, String genre, boolean available) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.available = available;
    }


    public boolean isAvailable() {
        return available;
    }

    void markAvailable() {
        this.available = true;
    }

    void markUnavailable() {
        this.available = false;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }


    public String getIsbn() {
        return isbn;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setAuthor(String author) {
        this.author = author;
    }

    void setYear(int year) {
        this.year = year;
    }

    void setGenre(String genre) {
        this.genre = genre;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }


    @Override
    public String toString() {
        return "Book{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", year=" + year +
                ", genre='" + genre + '\'' +
                ", available=" + available +
                '}';
    }
}