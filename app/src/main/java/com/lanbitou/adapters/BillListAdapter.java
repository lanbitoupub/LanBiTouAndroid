package com.lanbitou.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.comparators.BillComparatorByDate;
import com.lanbitou.entities.Bill;

import java.util.Collections;
import java.util.List;

/**
 * 按照创建时间从近到往向下排列
 * Created by Henvealf on 16-5-14.
 */
public class BillListAdapter extends BaseAdapter{

    private List<Bill> billList;
    private boolean[] tallyDateShowList;

    private  Context context;

    public BillListAdapter(Context context){
        this.context =context;
        nowDateStr = null;

    }

    public BillListAdapter(Context context, List<Bill> billList){
        this(context);
        this.billList = billList;
        setDateShow();
    }

    public void setBillList(List<Bill> billList){
        this.billList = billList;
        setDateShow();
    }

    public void addItemToTop(Bill bill){
        billList.add(0,bill);
        setDateShow();
    }

    public void removeItem(int position){
        if(position < 0 || position > getCount()){
            return;
        }
        billList.remove(position);
        setDateShow();
    }

    public synchronized void updateItem(int position,Bill bill){
        billList.remove(position);
        billList.add(position,bill);
        setDateShow();
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

    private String nowDateStr = null;

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
//        Log.i("lanbitou","getView里: ");
        LayoutInflater inflater = LayoutInflater.from(context);
        View listItemView = null;
        listItemView = inflater.inflate(R.layout.bill_list_item,null);
        TextView typeTv = (TextView) listItemView.findViewById(R.id.bill_list_item_type);
        TextView moneyTv;
        TextView remarkTv;
        TextView dateTv;
        Bill bill = billList.get(position);

        if(tallyDateShowList[position]){
            dateTv = (TextView) listItemView.findViewById(R.id.bill_list_item_date_tv);
            String billDateYMD = bill.getBillDate().substring(0,10);
            dateTv.setText(billDateYMD);
            dateTv.setVisibility(View.VISIBLE);
//            Log.i("lanbitou","第 " + position + " 个item的Date显示:" + nowDateStr);
        }
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
        //Log.i("增加换行符",sb.toString());
        return sb.toString();
    }

    /**
     * 标明哪一个Item用来显示时间
     */
    private void setDateShow() {
        nowDateStr = null;
        tallyDateShowList = new boolean[billList.size()];
        Collections.sort(this.billList,new BillComparatorByDate());
        for (int i = 0; i < tallyDateShowList.length; i++) {
            tallyDateShowList[i] = false;
            Bill bill = billList.get(i);
            String billDateYMD = bill.getBillDate().substring(0, 10);
            if (nowDateStr == null || !nowDateStr.equals(billDateYMD)) {
                nowDateStr = billDateYMD;
//                Log.i("lanbitou","第 " +i+" 项的时间被设置 nowDateStr: " + nowDateStr);
                tallyDateShowList[i] = true;
            }
        }
    }
}
