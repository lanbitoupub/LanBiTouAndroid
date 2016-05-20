package com.lanbitou.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.activities.AddBillActivity;
import com.lanbitou.adapters.BillListAdapter;
import com.lanbitou.entities.Bill;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 点击右下角的按钮弹出文件夹分类
 * 点击添加弹出添加的Activity.里面有三个选项,即 类型(限制在4个字符内),类型,钱数,备注,时间.
 * Created by Henvealf on 16-5-13.
 */
public class BillFragment extends Fragment implements BillFolderFragment.OnFragmentReturnListener {

    private final static String ADD_ONE_BILL_URL =
            "http://10.0.2.2:8082/lanbitou/bill/addOne";


    private static int ADD_BILL_REQUEST_CODE = 1;

    private Button toAddBillBtn;

    private TextView folderTv;

    private ProgressBar inoutRatioPb;   //收支比例进度条
    private double totalMoney;
    private double inTotalMoney;

    private BillListAdapter billListadapter;

    private int uid;                     //登陆用户Id
    private String folder = "日常";      //所在文件夹,默认为 日常

    private FragmentTransaction fragmentTransaction;
    private BillFolderFragment folderFragment;

   // private FileUtil fileUtil;
    private List<Bill> nowBillList;         //内存中的BillList
    private Gson gson = new Gson();

    public BillFragment(){


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bill, container,false);

        //获取uid
        uid = 1;

        toAddBillBtn = (Button) view.findViewById(R.id.go_add_bill_btn);

        //点击跳转到添加Bill的Activity
        toAddBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i =
                        new Intent(getActivity(),AddBillActivity.class);
                i.putExtra("folder",folder);  //传过去文件夹,以备查看与修改
                startActivityForResult(i,ADD_BILL_REQUEST_CODE);
            }
        });

        billListadapter = new BillListAdapter(this.getActivity());

        //此处检查用户的网络状况,有网络连接就先获取云端数据,然后与本地比较后保存在本地.
        //if(IsNet.isConnect(getActivity())){

       // }

        //没有网络接入就直接加载本地数据
        setBillListAdapterFromSD();


        //在ListView中显示
        ListView list = (ListView) view.findViewById(R.id.bill_list);
        list.setAdapter(billListadapter);

        inoutRatioPb = (ProgressBar) view.findViewById(R.id.inout_ratio_pb);
        //更新进度条
        updateInoutRatioPb();

        folderFragment = BillFolderFragment.getInstance();

        folderTv = (TextView) view.findViewById(R.id.show_folder_tv);
        folderTv.setText(folder);

        folderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentTransaction = getFragmentManager().beginTransaction();
                //加个动画
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                //显示碎片
                if(!folderFragment.isAdded()){
                    fragmentTransaction.add(R.id.bill_folder_fra,folderFragment);
                }
                fragmentTransaction.show(folderFragment);
                fragmentTransaction.commit();
            }
        });
        folderFragment.setOnFragmentReturnListener(this);
        return view;
    }


    private  Handler postOneBillHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    Toast.makeText(getActivity(), "上传新的Bill成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 当文件夹碎片返回时获取文件夹名字,执行其他其他一些操作
     * @param folderName 得到的文件夹的名字
     */
    @Override
    public void onFragmentReturn(String folderName) {
        folder = folderName;            //获取到新的文件夹的名字
        setBillListAdapterFromSD();
        fragmentTransaction = getFragmentManager().beginTransaction();
        //fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.hide(folderFragment);
        fragmentTransaction.commit();
        //更新进度条
        updateInoutRatioPb();
        folderTv.setText(folderName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADD_BILL_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                Bill newBill = (Bill) data.getSerializableExtra("newBill");
                newBill.setUid(uid);

                String newBillJsonStr =  gson.toJson(newBill);

                ThreadPoolUtils.execute(new HttpPostThread(postOneBillHandler,
                        ADD_ONE_BILL_URL,newBillJsonStr));

                //更新ListView
                billListadapter.addItemToTop(newBill);  //你变nowBillList也变
                billListadapter.notifyDataSetChanged();

                Toast.makeText(getActivity(),
                        "\n时间" + newBill.getBillDate(),
                        Toast.LENGTH_LONG).show();

                //这里上传以及保存到本地,当然还得视当前网络状况决定

                //更新进度条
                updateInoutRatioPb();

                //得到新的BillList的Json串
                String newBillListJsonStr = gson.toJson(nowBillList);
                Log.i("json",newBillListJsonStr);

                //写入到SD卡中
                FileUtil fileUtil = new FileUtil("/bill/" + uid, folder);
                fileUtil.write(newBillListJsonStr);
                //读出看一看,没毛病!
                String testRead = fileUtil.read();
                Log.i("lanbitou","从更新后的bill文件中读出的结果为:" + testRead);

                //if(isNet.IsConnect()){

                //}
            }
        }
    }

    /**
     * 获取sd卡BillList数据,并设置nowBillList;
     */
    private void setBillListAdapterFromSD(){
        FileUtil fileUtil = new FileUtil("/bill/" + uid, folder);
        String localJson = fileUtil.read();
        //如果读出的串不为空,就将其转换为List
        if(localJson != null && !localJson.equals("")){
            //将Json转为List
            nowBillList = gson.fromJson(localJson,new TypeToken<List<Bill>>(){}.getType());
            //为空,就new一个;
        }else{
            nowBillList = new ArrayList<>();
        }

        billListadapter.setBillList(nowBillList);
        billListadapter.notifyDataSetChanged();
    }

    /**
     * 更新收支比例进度条
     */
    public void updateInoutRatioPb(){
        totalMoney = 0;
        inTotalMoney = 0;
        for(int i = 0 ; i < nowBillList.size(); i ++){

            Bill b = nowBillList.get(i);
            //获取新的进度条数据
            totalMoney += Math.abs(b.getMoney());

            if(b.getMoney() > 0){
                inTotalMoney += b.getMoney();
            }

        }

        //数据为空
        if(totalMoney == 0 && inTotalMoney == 0){
            totalMoney = 2;
            inTotalMoney = 1;
        }
        //更新进度条
        inoutRatioPb.setMax((int)totalMoney);
        inoutRatioPb.setProgress((int)inTotalMoney);
    }

}
