package com.jkingone.customviewlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.jkingone.commonlib.Utils.ImageUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class PicassoBackground extends LinearLayout implements Target {

    private Handler mHandler = new Handler();
    private Runnable mLastCallback =null;

    public PicassoBackground(Context context) {
        super(context);
    }

    public PicassoBackground(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PicassoBackground(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//        Vibrant （有活力的）
//        Muted （柔和的）

//        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//            @Override
//            public void onGenerated(Palette palette) {
//                int rgb = palette.getDarkMutedColor(Color.GRAY);
//                setBackgroundColor(rgb);
//            }
//        });
        if (mLastCallback != null) {
            mHandler.removeCallbacks(mLastCallback);
        }
        Runnable curCallback = new Runnable() {
            @Override
            public void run() {
                setBackground(new BitmapDrawable(getResources(), bitmap));
            }
        };
        mHandler.postDelayed(curCallback, 1000);
        mLastCallback = curCallback;
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }
}