package com.test.mysede.perfil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.test.mysede.R;

public final class ProfileImageLoader {

    private ProfileImageLoader() {
    }

    public static void loadIntoImageView(@NonNull ImageView imageView, @Nullable String imageUrl) {
        Object model = resolveModel(imageView.getContext(), imageUrl);
        Glide.with(imageView)
                .load(model)
                .transform(new CircleCrop())
                .placeholder(R.drawable.ic_usuario)
                .error(R.drawable.ic_usuario)
                .into(imageView);
    }

    public static void loadIntoMenuItem(@NonNull Context context, @Nullable MenuItem menuItem, @Nullable String imageUrl) {
        if (menuItem == null) {
            return;
        }
        menuItem.setIcon(R.drawable.ic_usuario);
        Object model = resolveModel(context, imageUrl);
        int size = context.getResources().getDimensionPixelSize(R.dimen.avatar_menu_size);
        Glide.with(context)
                .asBitmap()
                .load(model)
                .transform(new CircleCrop())
                .error(R.drawable.ic_usuario)
                .into(new CustomTarget<Bitmap>(size, size) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Drawable drawable = new BitmapDrawable(context.getResources(), resource);
                        menuItem.setIcon(drawable);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        if (placeholder != null) {
                            menuItem.setIcon(placeholder);
                        } else {
                            menuItem.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_usuario));
                        }
                    }
                });
    }

    private static Object resolveModel(@NonNull Context context, @Nullable String imageUrl) {
        if (TextUtils.isEmpty(imageUrl) || "null".equalsIgnoreCase(imageUrl)) {
            return fallbackDrawable();
        }
        if (imageUrl.startsWith("android.resource://")) {
            return Uri.parse(imageUrl);
        }
        if (imageUrl.startsWith("drawable/")) {
            @DrawableRes int resId = context.getResources().getIdentifier(
                    imageUrl.substring("drawable/".length()),
                    "drawable",
                    context.getPackageName());
            if (resId != 0) {
                return resId;
            }
        }
        return imageUrl;
    }

    private static int fallbackDrawable() {
        return R.drawable.ic_usuario;
    }
}