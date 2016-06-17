package com.lanbitou.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.lanbitou.R;

import java.io.File;

/**
 * 用于显示一张图片
 * Created by Henvealf on 16-6-15.
 */
public class ShowSignalPaintActivity extends Activity{

    private ImageView imageView;
    private Button deleteBtn;
    private String imagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_signal_paint);
        imagePath = getIntent().getStringExtra("imagePath");
        Log.i("lanbitou","在ShowSignalPaintActivity中,传来的图片路径为: " + imagePath);
        imageView = (ImageView) findViewById(R.id.show_paint_iv);
        deleteBtn = (Button) findViewById(R.id.delete_paint_btn);
        BitmapFactory.Options options = new BitmapFactory.Options();          //设置二进制图片工厂
        options.inSampleSize = 2;
        Bitmap bm =
                BitmapFactory.decodeFile(imagePath, options);
        imageView.setImageBitmap(bm);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ShowSignalPaintActivity.this);  //得到构造器
                builder.setTitle("确认删除?");
                builder.setMessage("此操作不可重复哟!");
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() { //设置确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(imagePath);
                        boolean isSuccess = file.delete();
                        if(isSuccess){
                            Toast.makeText(ShowSignalPaintActivity.this,"删除成功!",Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        }else{
                            Toast.makeText(ShowSignalPaintActivity.this,"删除失败!",Toast.LENGTH_SHORT).show();
                        }

                        dialog.dismiss(); //关闭dialog
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }
}
