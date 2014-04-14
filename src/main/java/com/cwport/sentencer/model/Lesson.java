package com.cwport.sentencer.model;

import java.util.ArrayList;

/**
 * Created by isayev on 02.02.14.
 */
public class Lesson {
    String filename;
    String title;
    ArrayList<Card> cards = new ArrayList<Card>();
    String faceLocale;
    String backLocale;
    String description;
    int cardCount;

    public Lesson() {}

    public Lesson(String title) {
        this.title = title;
    }

    public Lesson(String filename, String title, String faceLocale, String backLocale) {
        this.filename = filename;
        this.title = title;
        this.faceLocale = faceLocale;
        this.backLocale = backLocale;
    }

    public Lesson(String filename, String title, String faceLocale, String backLocale,
                  String description, int cardCount) {
        this.filename = filename;
        this.title = title;
        this.faceLocale = faceLocale;
        this.backLocale = backLocale;
        this.description = description;
        this.cardCount = cardCount;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFaceLocale() {
        return faceLocale;
    }

    public void setFaceLocale(String faceLocale) {
        this.faceLocale = faceLocale;
    }

    public String getBackLocale() {
        return backLocale;
    }

    public void setBackLocale(String backLocale) {
        this.backLocale = backLocale;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String t) {
        this.description = t;
    }

    public String getDescription() {
        return this.description;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }
}
