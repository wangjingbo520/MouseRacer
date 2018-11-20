package com.example.mouseracer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * class
 *
 * @author bobo
 * @date 2018/10/22
 */
public class MyLinearLayout extends LinearLayout {
    private float downX;
    private float downY;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (listener != null) {
                    listener.onStart(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() > getWidth() + 20 || event.getX() < -20 || event.getY() > getHeight() + 20 || event.getY() < -20) {
                    if (listener != null) {
                        listener.onStop(this);
                    }
                } else {
                    if (listener != null) {
                        listener.onStart(this);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (listener != null) {
                    listener.onStop(this);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (listener != null) {
                    listener.onStop(this);
                }
                break;
            default:
                break;
        }
        return true;
    }



    private LinearChangeListener listener;

    public void setOnLinearChangeListener(LinearChangeListener l) {
        this.listener = l;
    }


    public interface LinearChangeListener {

        void onStart(MyLinearLayout linearLayout);

        void onStop(MyLinearLayout linearLayout);
    }


}
