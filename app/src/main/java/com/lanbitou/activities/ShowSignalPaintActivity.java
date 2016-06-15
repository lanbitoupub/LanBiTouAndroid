package com.lanbitou.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.lanbitou.R;

/**
 * 用于显示一张图片
 * Created by Henvealf on 16-6-15.
 */
public class ShowSignalPaintActivity extends Activity{

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_signal_paint);

        String imagePath = getIntent().getStringExtra("imagePath");
        Log.i("lanbitou","在ShowSignalPaintActivity中,传来的图片路径为: " + imagePath);
        imageView = (ImageView) findViewById(R.id.show_paint_iv);
        BitmapFactory.Options options = new BitmapFactory.Options();          //设置二进制图片工厂
        options.inSampleSize = 2;
        Bitmap bm =
                BitmapFactory.decodeFile(imagePath, options);
        imageView.setImageBitmap(bm);

    }
}
