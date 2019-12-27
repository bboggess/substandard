package com.example.substandard.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * An {@link android.widget.ImageView} to be used when you need your image to be
 * cropped as a perfect square.
 * Definitely found this idea on StackOverflow.
 **/
public class CoverArtImageView extends AppCompatImageView {
    public CoverArtImageView(Context context) {
        super(context);
    }

    public CoverArtImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CoverArtImageView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
