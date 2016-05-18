package com.lanbitou.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.entities.Bill;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * 按照创建时间从近到往向下排列
 * Created by Henvealf on 16-5-14.
 */
public class BillListAdapter extends BaseAdapter{

    private List<Bill> billList;

    private  Context context;

    public BillListAdapter(Context context){
        this.context =context;
        billList = new ArrayList<>();
    }

    public BillListAdapter(Context context, List<Bill> billList){
        this.context =context;
        this.billList = billList;
    }

    public void addItem(Bill bill){
        billList.add(bill);
    }

    @Override
    public int getCount() {
        return billList.size();
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View listItemView = inflater.inflate(R.layout.bill_list_item,null);
        TextView typeTv = (TextView) listItemView.findViewById(R.id.bill_list_item_type);
        TextView moneyTv;
        TextView remarkTv;

        Bill bill = billList.get(position);

        if(bill.getMoney() > 0){      //>0 为收 左边
            moneyTv = (TextView) listItemView.findViewById(R.id.bill_list_item_left_money);
            remarkTv = (TextView) listItemView.findViewById(R.id.bill_list_item_left_remark);
            typeTv.setBackgroundColor(Color.RED);
        }else {
            moneyTv = (TextView) listItemView.findViewById(R.id.bill_list_item_right_money);
            remarkTv = (TextView) listItemView.findViewById(R.id.bill_list_item_right_remark);
            typeTv.setBackgroundColor(Color.BLUE);
        }
        typeTv.setText(splitWithN(bill.getType()));
        moneyTv.setText(String.valueOf(bill.getMoney()));
        remarkTv.setText(bill.getRemark());

        return listItemView;
    }

    /**
     * 使用换行符分割每个字符
     * @return
     */
    private String splitWithN(String str){
       //  = "我和很多人一同,漫游在无人知的原野上";
        StringBuilder sb = new StringBuilder(str);
        for(int i = 1; i < str.length(); i++){
            sb.insert(i + i - 1,"\n");
        }
        Log.i("增加换行符",sb.toString());
        return sb.toString();
    }
}
