package com.diyandroid.eazycampus.service;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.diyandroid.eazycampus.R;

import ss.com.bannerslider.ImageLoadingService;

public class PicassoImageLoadingService implements ImageLoadingService {
    public Context context;

    public PicassoImageLoadingService(Context context) {
        this.context = context;
    }

    @Override
    public void loadImage(String url, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(R.drawable.horizontal_stack))
                .transition(DrawableTransitionOptions.withCrossFade(600))
                .into(imageView);
    }

    @Override
    public void loadImage(int resource, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(resource)
                .apply(new RequestOptions().placeholder(R.drawable.horizontal_stack))
                .transition(DrawableTransitionOptions.withCrossFade(600))
                .into(imageView);
    }

    @Override
    public void loadImage(String url, int placeHolder, int errorDrawable, ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions().placeholder(R.drawable.horizontal_stack))
                .transition(DrawableTransitionOptions.withCrossFade(600))
                .into(imageView);
    }
}