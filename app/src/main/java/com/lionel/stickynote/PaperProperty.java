package com.lionel.stickynote;

import java.io.Serializable;

// this class is responsible for store notepaper's appearance property
class PaperProperty implements Serializable {

    private float[] mPosition;
    private  String mTitle;
    private  String[] mContent;
    private  String mBackgroundColor;

    PaperProperty( String title, String[] content, String backgroundColor, float[] position) {
        mTitle = title;
        mContent = content;
        mBackgroundColor = backgroundColor;
        mPosition = position;

    }

    public String getTitle() {
        return mTitle;
    }

    public String[] getContent() {
        return mContent;
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setPosition(float[] position) {
        mPosition = position;
    };
    public float[] getPosition() {
        return mPosition;
    }
}
