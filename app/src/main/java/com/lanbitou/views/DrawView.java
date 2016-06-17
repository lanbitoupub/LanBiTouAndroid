package com.lanbitou.views;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.lanbitou.util.FileUtil;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by ShanRongjie on 2016/5/20.
 */
public class DrawView extends View {

    public boolean saved=true;//表示是否已保存
    private Bitmap cacheBitmap;
    private Canvas cacheCanvas;
    private Path path;
    public Paint paint;
    public Paint BitmapPaint;
    private float preX, preY;
    public Integer preColor=Integer.MAX_VALUE;//临时画笔颜色存储
    public int view_width, view_height;
    private ArrayList<DrawPath> savePath;
    private ArrayList<DrawPath> deletePath;
    private ArrayList<Integer> saveColor;
    private ArrayList<Integer> deleteColor;
    private ArrayList<Integer> savepen;
    private ArrayList<Integer> deletepen;
    private DrawPath dp;
    private int uid;


    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SharedPreferences preferences = context.getSharedPreferences("lanbitou", Context.MODE_PRIVATE);
        uid = preferences.getInt("uid", -1);


        cacheCanvas = new Canvas();
        //获取高度与宽度
        view_width = context.getResources().getDisplayMetrics().widthPixels;
        view_height = context.getResources().getDisplayMetrics().heightPixels;
        final float scale = context.getResources().getDisplayMetrics().density;
        view_height = (int) (view_height-(126)*scale + 0.5f);

        cacheCanvas.setBitmap(cacheBitmap);

        initCanvas();
        savePath = new ArrayList<>();
        deletePath = new ArrayList<>();
        saveColor = new ArrayList<>();
        deleteColor = new ArrayList<>();
        savepen = new ArrayList<>();
        deletepen = new ArrayList<>();

    }
    public void initCanvas(){//初始画板设置

        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);//消除锯齿
        paint.setDither(true);//防抖动
        paint.setColor(Color.BLACK);// 设置画笔的默认颜色
        paint.setStyle(Paint.Style.STROKE);// 设置画笔的填充方式为无填充
        paint.setStrokeJoin(Paint.Join.ROUND);//设置结合处的样子为圆弧
        paint.setStrokeCap(Paint.Cap.ROUND);//圆角笔刷
        paint.setStrokeWidth(10);//线宽

        BitmapPaint = new Paint(Paint.DITHER_FLAG);

        //画布大小
        cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);// 建立图像缓冲区用来保存图像
        cacheCanvas = new Canvas(cacheBitmap);  //所有Canvas画的东西都被保存在了cacheBitmap中

        cacheCanvas.drawColor(Color.WHITE);//设置画板背景

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(cacheBitmap, 0, 0, BitmapPaint);
        if(path!=null)
        {
            canvas.drawPath(path,paint);
        }
    }
    //路径对象
    class DrawPath{
        Path path;
        Paint paint;
    }
    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径和颜色最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo(){

        if(savePath != null && savePath.size() > 0){
            //调用初始化画布函数以清空画布
            initCanvas();

            //将路径与颜色保存列表中的最后一个元素删除 ,并将其保存在路径删除列表中
            DrawPath drawPath = savePath.get(savePath.size() - 1);
            Integer color=saveColor.get(saveColor.size()-1);
            Integer pen=savepen.get(savepen.size()-1);
            deletePath.add(drawPath);
            deleteColor.add(color);
            deletepen.add(pen);
            savePath.remove(savePath.size() - 1);
            saveColor.remove(saveColor.size()-1);
            savepen.remove(savepen.size()-1);

            //将路径保存列表中的路径重绘在画布上
            Iterator<DrawPath> iter1 = savePath.iterator();
            Iterator<Integer> iter2 = saveColor.iterator();
            Iterator<Integer> iter3 = savepen.iterator();
            while (iter1.hasNext()) {
                DrawPath dp = iter1.next();
                dp.paint.setColor(iter2.next());
                dp.paint.setStrokeWidth(iter3.next());
                cacheCanvas.drawPath(dp.path, dp.paint);

            }
            invalidate();// 刷新
        }
    }
    /**
     * 恢复的核心思想就是将撤销的路径保存到另外一个ArrayList
     * 然后从redo里面取出最顶端对象，
     * 画在画布上面
     */
    public void redo(){
        if(deletePath.size() > 0&&deleteColor.size() > 0){
            //将删除的路径列表中的最后一个，也就是最顶端路径取出（栈）,并加入路径保存列表中
            DrawPath dp = deletePath.get(deletePath.size() - 1);
            Integer color= deleteColor.get(deleteColor.size()-1);
            Integer pen= deletepen.get(deletepen.size()-1);
            savePath.add(dp);
            saveColor.add(color);
            savepen.add(pen);
            //将取出的路径重绘在画布上
            dp.paint.setColor(color);
            dp.paint.setStrokeWidth(pen);
            cacheCanvas.drawPath(dp.path, dp.paint);
            //将该路径从删除的路径列表中去除
            deletePath.remove(deletePath.size() - 1);
            deleteColor.remove(deleteColor.size()-1);
            deletepen.remove(deletepen.size()-1);
            invalidate();
        }
    }
    /*
         * 清空的主要思想就是初始化画布
         * 将保存路径的两个List清空
         * */
    public void removeAllPaint(){

        //调用初始化画布函数以清空画布
        initCanvas();
        invalidate();//刷新
        savePath.clear();
        deletePath.clear();
        saveColor.clear();
        deleteColor.clear();
        savepen.clear();
        deletepen.clear();

    }


    private void touch_start(float x, float y) {
        path.reset();//清空path
        path.moveTo(x, y);
        preX = x;
        preY = y;
        saved=false;
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - preX);
        float dy = Math.abs(y - preY);
        if (dx >= 5 || dy >= 5) {//防止误触
            path.quadTo(preX, preY, (x + preX)/2, (y + preY)/2);//绘制曲线
            preX = x;
            preY = y;
        }
    }

    private void touch_up() {
        path.lineTo(preX, preY);
        cacheCanvas.drawPath(path, paint);
        savePath.add(dp);
        saveColor.add(paint.getColor());
        savepen.add(Math.round(paint.getStrokeWidth()));
        path = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                path = new Path();
                dp = new DrawPath();
                dp.path = path;
                dp.paint = paint;

                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }



    public void saveBitmapAsPNG(String fileName) throws Exception {

        FileUtil fileUtil = new FileUtil("/paint/" + uid,fileName + ".png");
        fileUtil.saveBitmapAsImageFile(cacheBitmap);

        Toast.makeText(getContext(),
                "图像保存成功!",
                Toast.LENGTH_SHORT).show();
    }
}
