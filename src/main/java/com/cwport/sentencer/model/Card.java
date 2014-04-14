package com.cwport.sentencer.model;

/**
 * Created by isayev on 02.02.14.
 */
public class Card {
    boolean marked;
    String faceText;
    String backText;
    String id;
    String faceLocale;
    String backLocale;


    public Card() {}

    public Card(String id, String q, String a, boolean mark) {
        this.id = id;
        this.faceText = q;
        this.backText = a;
        this.marked = mark;
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

    public boolean isMarked() {
        return marked;
    }

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setBackText(String backText) {
        this.backText = backText;
    }

    public String getBackText() {
        return this.backText;
    }

    public void setFaceText(String faceText) {
        this.faceText = faceText;
    }

    public String getFaceText() {
        return this.faceText;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean getMarked() {
        return this.marked;
    }

    @Override
    public String toString() {
        return this.id + ";" + this.faceText + ";" + this.backText;
    }
}
