package com.libria.domain;
import com.libria.exception.pdfBookMissingException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @Column(name = "isbn", nullable = false, length = 20)
    private String isbn;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;
    @Column(name = "book_year",nullable = false)
    private int year;
    @Column(nullable = false)
    private String genre;
    @Column(nullable = false)
    private boolean available;
    @Column(nullable = false)
    private String coverPath; //url de l'image de couverture du livre
    @Column(nullable = false)
    private String pdfPath; //url pour le fichier pdf du livre


    //il nous faut un constructeur vide, car dans le libriaService, on recup des books dans les requette HTTP, et
    //il faut pouvoir créer un book vide pour le passer en param
    public Book() {
    }

    public Book(String isbn, String title, String author, int year, String genre, boolean available, String coverPath, String pdfPath) {
        if (isbn == null || isbn.isBlank())
            throw new IllegalArgumentException("ISBN est obligatoire.");
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Le titre est obligatoire.");
        if (author == null || author.isBlank())
            throw new IllegalArgumentException("L'auteur est obligatoire.");
        if (year <= 0)
            throw new IllegalArgumentException("L'année doit être positive.");
        if (pdfPath == null || pdfPath.isBlank())
            throw new pdfBookMissingException("Le fichier PDF est obligatoire.");

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
        this.available = available;
        this.coverPath = coverPath;
        this.pdfPath = pdfPath;
    }


    public void setCoverImage(String url){
        this.coverPath = url;
    }
    public void setPdf(String url){
        this.pdfPath = url;
    }

    public String getCoverImage(){
        return this.coverPath;
    }
    public String getPdf(){
        return this.pdfPath;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void setUnavailable(boolean available) {
        this.available = available;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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