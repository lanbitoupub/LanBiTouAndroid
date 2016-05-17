package com.lanbitou.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.lanbitou.R;
import com.lanbitou.activities.AddBillActivity;
import com.lanbitou.activities.MainActivity;
import com.lanbitou.adapters.BillListAdapter;
import com.lanbitou.entities.Bill;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 点击右下角的按钮弹出文件夹分类
 * 点击添加弹出添加的Activity.里面有三个选项,即 类型(限制在4个字符内),类型,钱数,备注,时间.
 * Created by Henvealf on 16-5-13.
 */
public class BillFragment extends Fragment{

    private static int ADD_BILL_REQUEST_CODE = 1;

    private Button toAddBillBtn;

    BillListAdapter billListadapter;

    public BillFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bill, container,false);

        toAddBillBtn = (Button) view.findViewById(R.id.go_add_bill_btn);

        //点击跳转到添加Bill的Activity
        toAddBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =
                        new Intent(getActivity(),AddBillActivity.class);
                i.putExtra("uid",1);    //传送必要的用户id给ACT,先模拟一下.
                i.putExtra("folder","日常");
                startActivityForResult(i,ADD_BILL_REQUEST_CODE);
            }
        });

        //测试
        List<Bill> billList = new ArrayList<>();
        billList.add(new Bill(1,1,"吃",-111,"有个无聊的地方,","日常",new Date()));
        billList.add(new Bill(1,1,"工资",-111,"一群人到处游啊游,","日常",new Date()));
        billList.add(new Bill(1,1,"喝",121.2,"醉醺醺的倒了,","日常",new Date()));
        billList.add(new Bill(1,1,"玩",111.22,"有个无聊的地方,","日常",new Date()));
        billList.add(new Bill(1,1,"乐",-111,"","日常",new Date()));
        billList.add(new Bill(1,1,"哈",111,"胡哈哈哈哈","日常",new Date()));
        billList.add(new Bill(1,1,"很多",-111,"中田也不知所措,","日常",new Date()));

        billList.add(new Bill(1,1,"你猜",111,"天上很多鱼,","日常",new Date()));

        billListadapter = new BillListAdapter(this.getActivity(),billList);

        ListView list = (ListView) view.findViewById(R.id.bill_list);
        list.setAdapter(billListadapter);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.i("addBill","从addBill返回喽!");
        if(requestCode == ADD_BILL_REQUEST_CODE){
            if (resultCode == MainActivity.RESULT_OK){
                //Log.i("addBill","返回的值也很对嘛!");
                Bill newBill = (Bill) data.getSerializableExtra("newBill");
                billListadapter.addItem(newBill);
                billListadapter.notifyDataSetChanged();
                Toast.makeText(getActivity(),
                        "用户Id:" + newBill.getUid()
                        + "\n消费类型:" + newBill.getType()
                        + "\n多少钱" + newBill.getMoney()
                        + "\n所在文件夹" + newBill.getFolder()
                        + "\n时间" + newBill.getBillDate()
                        + "\n备注" + newBill.getRemark(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
