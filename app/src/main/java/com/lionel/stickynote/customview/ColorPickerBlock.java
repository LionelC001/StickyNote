package com.lionel.stickynote.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerBlock extends View {

    private int id;
    private Handler mHandler;
    private String mColorF;
    private Paint mPaint;
    private String[] mColorList = {"#AF626262", "#AFefb2d0", "#D6B1D434", "#E08B468B", "#AFff7500", "#DC0450FB"};

    public ColorPickerBlock(Context context) {
        super(context);
    }

    public ColorPickerBlock(Context c, Handler handler, String colorB, String colorF) {
        this(c);
        for (int i = 0; i < mColorList.length; i++) {
            if (colorB.equals(mColorList[i])) id = i;
        }
        mHandler = handler;
        mColorF = colorF;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor(colorB));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(getWidth() / 2 - 150, getHeight() / 2 - 100, getWidth() / 2 + 150, getHeight() / 2 + 100, 50, 50, mPaint);
        mPaint.setColor(Color.parseColor(mColorF));
        canvas.drawRoundRect(getWidth() / 2 - 100, getHeight() / 2 - 50, getWidth() / 2 + 100, getHeight() / 2 + 50, 50, 50, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Message msg = new Message();
            msg.arg1 = id;
            mHandler.sendMessage(msg);
        }
        return super.onTouchEvent(event);
    }
}
