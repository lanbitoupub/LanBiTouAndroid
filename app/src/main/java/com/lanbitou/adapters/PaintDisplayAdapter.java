package com.lanbitou.adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lanbitou.R;
import com.lanbitou.activities.ShowSignalPaintActivity;
import com.lanbitou.util.FileUtil;

import java.util.List;

/**
 * Created by Henvealf on 16-6-15.
 */
public class PaintDisplayAdapter extends BaseAdapter implements View.OnClickListener{

    private List<String> imagePathList;         //图片文件的路的集合
    private Context context;
    private int uid;
    BitmapFactory.Options options;
    public final static int DELETE_PAINT_REQUEST_CODE = 30;

    public PaintDisplayAdapter(Context context, int uid){
        this.context = context;
        this.uid = uid;
        setImagePathList();
        //Log.i("lanbitou","所有图片的完整路径为 : "+ imagePathList);
        options = new BitmapFactory.Options();          //设置二进制图片工厂
        options.inSampleSize = 2;
    }

    @Override
    public int getCount() {
        if(imagePathList.size() == 0){
            return 0;
        }
        return  imagePathList.size() / 3 + 1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView;

//        if(view == null){
            itemView = inflater.inflate(R.layout.paint_display_list_item, null);
//        } else {
//            itemView = view;
//        }

        ImageView[] imageViews = new ImageView[]{
                (ImageView) itemView.findViewById(R.id.paint_display_item0_iv),
                (ImageView) itemView.findViewById(R.id.paint_display_item1_iv),
                (ImageView) itemView.findViewById(R.id.paint_display_item2_iv),
        };

        int pathListSize = imagePathList.size();
        Log.i("lanbitou",pathListSize+"");
        for(int i = 0; i < imageViews.length; i++) {
            imageViews[i].setOnClickListener(this);
            imageViews[i].setTag(position);
            int index = position * imageViews.length + i;   //每3个图片一个item
            if(pathListSize == 0){
                break;
            }
            if(index > pathListSize - 1){
                break;
            }

            Bitmap bm =
                    BitmapFactory.decodeFile(imagePathList.get(index), options);
            imageViews[i].setImageBitmap(bm);
        }
        return itemView;
    }

    public void setImagePathList(){
        this.imagePathList = new FileUtil("/paint/" + this.uid).getWholePathByExact(".png");
    }

    /**
     * 图片点击后展示
     * @param view
     */
    public void onClick(View view) {
        int id = view.getId();
        int position = (int) view.getTag();
        int index = -1;
        switch (id){
            case R.id.paint_display_item0_iv:
                index = position * 3;
                break;
            case R.id.paint_display_item1_iv:
                index = position * 3 + 1;
                break;
            case R.id.paint_display_item2_iv:
                index = position * 3 + 2;
                break;
        }
        if(index != -1){        //避免没有点击到图片
            String signalWholePath = imagePathList.get(index);
            Intent toShowI = new Intent(context, ShowSignalPaintActivity.class);
            toShowI.putExtra("imagePath",signalWholePath);
            ((Activity)context).startActivityForResult(toShowI,DELETE_PAINT_REQUEST_CODE);
        }
    }

}
