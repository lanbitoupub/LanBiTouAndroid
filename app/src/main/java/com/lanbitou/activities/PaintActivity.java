package com.lanbitou.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lanbitou.R;
import com.lanbitou.views.DrawView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PaintActivity extends AppCompatActivity {

    DrawView drawView = null;
    private TextView tv_finish = null;//完成
    private ImageView iv_brush = null;//画笔
    private ImageView iv_eraser = null;//橡皮
    private ImageView iv_cancel = null;//撤销
    private ImageView iv_recover = null;//恢复
    private ImageView iv_dustbin = null;//清除
    private View popView = null;
    private PopupWindow popWin = null;
    private boolean popdisplay = false;
    private int uid;
    private String imageSaveName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);
        getSupportActionBar().hide();

        SharedPreferences preferences = getSharedPreferences("lanbitou", Context.MODE_PRIVATE);
        uid = preferences.getInt("uid", -1);


        //图片的名字为矩阵的坐标
        imageSaveName = new SimpleDateFormat("yyyyMMddhhmmss")
                .format(new Date(System.currentTimeMillis()));   // 产生时间戳为文件名
        //Log.i("lanbitou","图片保存的名字为:" + imageSaveName);
        final DrawView drawView_C = (DrawView) findViewById(R.id.re_container);
        drawView = drawView_C;

        tv_finish = (TextView) findViewById(R.id.finish);
        iv_brush = (ImageView) findViewById(R.id.iv_brush);
        iv_eraser = (ImageView) findViewById(R.id.iv_eraser);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        iv_recover = (ImageView) findViewById(R.id.iv_recover);
        iv_dustbin = (ImageView) findViewById(R.id.iv_dustbin);

        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popdisplay==true)
                {
                    popdisplay=false;
                    popWin.dismiss();
                }
                try {
                    drawView.saveBitmapAsPNG(imageSaveName);
                    drawView.saved = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        iv_brush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popdisplay==false) {
                    popdisplay = true;
                    if(drawView.preColor!=Integer.MAX_VALUE)
                    {
                        drawView.paint.setColor(drawView.preColor);
                    }

                    showPopupWindow();
                } else {
                    popdisplay = false;
                    popWin.dismiss();
                }
            }
        });
        iv_eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawView.preColor=drawView.paint.getColor();
                drawView.paint.setColor(Color.WHITE);
                Toast.makeText(PaintActivity.this,
                        "已选择橡皮",
                        Toast.LENGTH_SHORT).show();
                if(popdisplay==true)
                {
                    popdisplay=false;
                    popWin.dismiss();
                }
            }
        });
        iv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popdisplay==true)
                {
                    popdisplay=false;
                    popWin.dismiss();
                }
                drawView.undo();
            }
        });
        iv_recover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popdisplay==true)
                {
                    popdisplay=false;
                    popWin.dismiss();
                }
                drawView.redo();
            }
        });
        iv_dustbin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(popdisplay==true)
                {
                    popdisplay=false;
                    popWin.dismiss();
                }
               if(drawView.saved==false)
               {
                   AlertDialog.Builder builder = new AlertDialog.Builder(PaintActivity.this);  //得到构造器
                   builder.setTitle("提醒");
                   builder.setMessage("  将清除所有画迹?");
                   builder.setIcon(R.mipmap.ic_launcher);
                   builder.setPositiveButton("         确定", new DialogInterface.OnClickListener() { //设置确定按钮
                       @Override
                       public void onClick(DialogInterface dialog, int which) {

                           dialog.dismiss(); //关闭dialog
                           drawView.removeAllPaint();
                           drawView.saved=true;
                       }
                   });
                   builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                       }
                   });
                   builder.create().show();
               }else {
                   drawView.removeAllPaint();
               }
            }
        });
    }

    private void showPopupWindow() {

        // 一个自定义的布局，作为显示的内容
        LayoutInflater inflater = LayoutInflater.from(PaintActivity.this);
        LinearLayout lay_Bottom=(LinearLayout) findViewById(R.id.paint_bottom);
        popView = inflater.inflate(R.layout.activity_pens, null);
        popWin = new PopupWindow(popView,lay_Bottom.getWidth(),lay_Bottom.getHeight()*3, false);
        popWin.showAtLocation(PaintActivity.this.iv_brush, Gravity.BOTTOM, 10,lay_Bottom.getHeight()+1);
        popView.setBackgroundColor(Color.parseColor("#d3cfc4"));



        SeekBar paint_thick=(SeekBar) popView.findViewById(R.id.paint_thick);
        final TextView t_seekbar=(TextView) popView.findViewById(R.id.t_seekbar);
        paint_thick.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                t_seekbar.setText(""+i+"px");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drawView.paint.setStrokeWidth(seekBar.getProgress());
            }
        });
        t_seekbar.setText(""+Math.round(drawView.paint.getStrokeWidth())+"px");//内部类无法传递出参数，可间接取得值，并转为整数


        // 设置颜色按钮的点击事件
        Button button_red = (Button) popView.findViewById(R.id.color_red);
        button_red.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.RED;
                drawView.paint.setColor(Color.RED);
                popdisplay = false;
                iv_brush.setBackgroundColor(Color.RED);
                popWin.dismiss();
            }
        });
        Button button_blue = (Button) popView.findViewById(R.id.color_blue);
        button_blue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.BLUE;
                drawView.paint.setColor(Color.BLUE);
                popdisplay = false;
                iv_brush.setBackgroundColor(Color.BLUE);
                popWin.dismiss();
            }
        });
        Button button_green = (Button) popView.findViewById(R.id.color_green);
        button_green.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.GREEN;
                drawView.paint.setColor(Color.GREEN);
                iv_brush.setBackgroundColor(Color.GREEN);
                popdisplay = false;
                popWin.dismiss();
            }
        });
        Button button_black = (Button) popView.findViewById(R.id.color_black);
        button_black.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.BLACK;
                drawView.paint.setColor(Color.BLACK);
                iv_brush.setBackgroundColor(Color.BLACK);
                popdisplay = false;
                popWin.dismiss();
            }
        });
        Button button_yellow = (Button) popView.findViewById(R.id.color_yellow);
        button_yellow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.YELLOW;
                drawView.paint.setColor(Color.YELLOW);
                iv_brush.setBackgroundColor(Color.YELLOW);
                popdisplay = false;
                popWin.dismiss();
            }
        });
        Button button_brown = (Button) popView.findViewById(R.id.color_brown);
        button_brown.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.parseColor("#a52a2a");
                drawView.paint.setColor(Color.parseColor("#a52a2a"));
                iv_brush.setBackgroundColor(Color.parseColor("#a52a2a"));
                popdisplay = false;
                popWin.dismiss();
            }
        });
        Button button_pink = (Button) popView.findViewById(R.id.color_pink);
        button_pink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.parseColor("#ff1493");
                drawView.paint.setColor(Color.parseColor("#ff1493"));
                iv_brush.setBackgroundColor(Color.parseColor("#ff1493"));
                popdisplay = false;
                popWin.dismiss();
            }
        });
        Button button_fuchsia = (Button) popView.findViewById(R.id.color_fuchsia);
        button_fuchsia.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawView.preColor=Color.parseColor("#ff00ff");
                drawView.paint.setColor(Color.parseColor("#ff00ff"));
                iv_brush.setBackgroundColor(Color.parseColor("#ff00ff"));

                popdisplay = false;
                popWin.dismiss();
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaintActivity.this);  //先得到构造器
        builder.setTitle("画作还未保存"); //设置标题
        builder.setMessage("确认退出?"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可

        builder.setPositiveButton("保存后退出", new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    drawView.saveBitmapAsPNG(imageSaveName);
                    dialog.dismiss(); //关闭dialog
                    setResult(MainActivity.ADD_PAINT_REQUEST_CODE);
                    PaintActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNeutralButton("不保存",new DialogInterface.OnClickListener() { //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                setResult(MainActivity.ADD_PAINT_REQUEST_CODE);
                PaintActivity.this.finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        if (!drawView.saved) {
            builder.create().show();
        } else {
            setResult(MainActivity.ADD_PAINT_REQUEST_CODE);
            PaintActivity.this.finish();
        }
        if(popdisplay==true)
        {
            popdisplay=false;
            popWin.dismiss();
        }
        return true;
    }


}
