package com.lionel.stickynote.customview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lionel.stickynote.MainActivity;
import com.lionel.stickynote.R;
import com.lionel.stickynote.fieldclass.PaperProperty;

// this class is responsible for notepaper's appearance, open the notepaper's content, and delete self
public class Paper extends FrameLayout {

    private Context mContext;
    private CardView mPaperView;
    private String mBackgroundColor;
    private float beforeViewX, beforeViewY, nowViewX, nowViewY;
    private GestureDetector gestureDetector;
    private PaperProperty mPP;

    public interface DeletePaperInterface {
        void deletePaper(Paper paper, PaperProperty pp);
    }

    public interface OpenPaperContent {
        void openContent(PaperProperty pp);
    }


    public Paper(Context context) {
        super(context);
    }

    public Paper(Context context, PaperProperty pp) {
        super(context);
        mContext = context;
        mPP = pp;
        mBackgroundColor = pp.getBackgroundColor();
        nowViewX = pp.getPosition()[0];
        nowViewY = pp.getPosition()[1];

        // must need viewGroup
        inflate(context, R.layout.paper_appearance_layout, this);
        setBackgroundColor(Color.TRANSPARENT);

        // initialize
        setupPaperOrigin();
        setPosition();
        ((TextView) findViewById(R.id.txtPaperAppearanceTitle)).setText(pp.getTitle());
        ((TextView) findViewById(R.id.txtPaperAppearanceItem1)).setText(pp.getContent()[0]);
        ((TextView) findViewById(R.id.txtPaperAppearanceItem2)).setText(pp.getContent()[1]);
        ((TextView) findViewById(R.id.txtPaperAppearanceItem3)).setText(pp.getContent()[2]);
        ((TextView) findViewById(R.id.txtPaperAppearanceItem4)).setText(pp.getContent()[3]);

        gestureDetector = new GestureDetector(context, new MyGestureDetector());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // cause onSingleTapUp() can't called after onScroll() or onLongPress()
        // so use this instead
        if (e.getAction() == MotionEvent.ACTION_UP) {
            // restore view's appearance
            setupPaperOrigin();

            // While finger is up, detect if paper touch the delete region or not.
            if ((getX() < MainActivity.iDeleteRegionX &&
                    (getY() + getHeight()) > MainActivity.iDeleteRegionY)) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete")
                        .setMessage("Are you sure for deleting this paper permanently?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((DeletePaperInterface) mContext).deletePaper(Paper.this, mPP);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setCancelable(true)
                        .show();
            }
        }
        return gestureDetector.onTouchEvent(e);
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            //  bring this view to front
            bringToFront();
            //set shape as pressed
            setupPressDownEffect();

            // view's X as touched
            beforeViewX = getX();
            beforeViewY = getY();

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float deltaX = e2.getRawX() - e1.getRawX();
            float deltaY = e2.getRawY() - e1.getRawY();
            float x = beforeViewX + deltaX;
            float y = beforeViewY + deltaY;

            // move within the boundaries
            moveWithinBoundary(x, y);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            ((OpenPaperContent) mContext).openContent(mPP);
            return true;
        }
    }

    private void moveWithinBoundary(float x, float y) {
        Rect rect = new Rect();
        ((Activity) mContext).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(rect);

        if (x < 0) nowViewX = 0;
        else if (x + getWidth() >= rect.right)
            nowViewX = rect.right - getWidth();
        else nowViewX = x;

        if (y < 0) nowViewY = 0;
        else if (y + getHeight() >= rect.bottom)
            nowViewY = rect.bottom - getHeight();
        else nowViewY = y;

        setPosition();

        // save position property
        mPP.setPosition(new float[]{nowViewX, nowViewY});
    }

    private void setPosition() {
        setX(nowViewX);
        setY(nowViewY);
    }

    private void setupPressDownEffect() {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(mPaperView.getRadius());
        shape.setStroke(10, Color.parseColor("#88ff0000"));
        shape.setColor(Color.parseColor(mBackgroundColor));
        mPaperView.setBackground(shape);
    }

    private void setupPaperOrigin() {
        mPaperView = findViewById(R.id.paperRootView);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(mPaperView.getRadius());
        shape.setStroke(10, Color.TRANSPARENT);
        shape.setColor(Color.parseColor(mBackgroundColor));
        mPaperView.setBackground(shape);
    }
}
