package com.lionel.stickynote;

import java.io.Serializable;

// this class is responsible for store notepaper's appearance property
class PaperProperty implements Serializable {

    private final int PAPER_ID;
    private float[] mPosition;
    private String mTitle;
    private String[] mContent;
    private String mBackgroundColor;

    PaperProperty(int paperId, String title, String[] content, String backgroundColor, float[] position) {
        PAPER_ID = paperId;
        mTitle = title;
        mContent = content;
        mBackgroundColor = backgroundColor;
        mPosition = position;
    }

    public int getPaperId() {
        return PAPER_ID;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setContent(String[] content) {
        mContent = content;
    }

    public String[] getContent() {
        return mContent;
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setPosition(float[] position) {
        mPosition = position;
    }

    public float[] getPosition() {
        return mPosition;
    }
}
