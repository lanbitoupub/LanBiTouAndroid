package com.lanbitou.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.util.FileUtil;

import java.util.List;

/**
 * Created by Henvealf on 16-5-19.
 */
public class BillFolderAdapter extends BaseAdapter{

    private List<String> folderNameList ;
    private Context context;


    public BillFolderAdapter(Context context){
        this.context = context;
        setFolderNameList();
    }

    public void addItem(String newFolder){
        folderNameList.add(newFolder);

    }

    @Override
    public int getCount() {
        return folderNameList.size();
    }

    @Override
    public Object getItem(int i) {
        return folderNameList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View listItemView;
        if(view == null ){
            listItemView = inflater.inflate(R.layout.bill_folder_list_item,null);
        }else{
            listItemView = view;
        }

        TextView tv = (TextView) listItemView.findViewById(R.id.bill_folder_list_item_tv);
        tv.setText(folderNameList.get(i));
        return listItemView;
    }

    /**
     * 设置文件List
     */
    private void setFolderNameList(){

        //获取uid
        int uid = 1;
        FileUtil fileUtil = new FileUtil("/bill/" + uid, null);

        folderNameList = fileUtil.getfileCount();

        Log.i("lanbitou", "/bill下的文件数为" + folderNameList.size());
    }
}
