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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.activities.AddBillActivity;
import com.lanbitou.adapters.BillListAdapter;
import com.lanbitou.entities.Bill;
import com.lanbitou.net.IsNet;
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

    private final static String ROOT_URL = "http://192.168.1.100:8082/lanbitou";

    private final static String ADD_ONE_BILL_URL =
            ROOT_URL + "/bill/addOne";
    private final static String ADD_SOME_BILLS_URL =
            ROOT_URL + "/bill/addSome";          //添加一些

    private final static String DELETE_ONE_BILL_URL =
            ROOT_URL + "/bill/deleteOne";
    private final static String DELETE_SOME_BILLS_URL =
            ROOT_URL + "/bill/deleteSome";

    private final static String UPDATE_ONE_BILL_URL =
            ROOT_URL + "/bill/updateOne";
    private final static String UPDATE_SOME_BILLS_URL =
            ROOT_URL + "/bill/updateSome";

    private final static String GET_ONE_BY_ID =
            ROOT_URL + "/bill/getOne";
    private final static String GET_SOME_BILLS_URL =
            ROOT_URL + "/bill/getSomeByFolder";

    private final static int ADD_BILL_REQUEST_CODE = 1;
    private final static String TALLY_FOLTER = "/.tallyLastOperate";            //无网时记录更改的文件,修改这里时别忘了FileUtil里
    private final static String TALLY_FOLTER_ADD = "add";
    private final static String TALLY_FOLTER_UPDATE = "update";
    private final static String TALLY_FOLTER_DELETE = "delete";

    private Button toAddBillBtn;

    private ImageButton folderIb;

    private ProgressBar inoutRatioPb;          //收支比例进度条
    ListView billList;
    private double totalMoney;
    private double inTotalMoney;

    private BillListAdapter billListadapter;

    private int uid;                         //登陆用户Id
    private String folder = "日常";          //所在文件夹,默认为 日常

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
        if(IsNet.isConnect(getActivity())){
            //检查本地情况,是否有记录账单操作的标记文件
            FileUtil recordFile = new FileUtil("/bill/" + uid + TALLY_FOLTER,null);
            String addJsonStr = recordFile.readByFileName(TALLY_FOLTER_ADD);
            Log.i("labbitou","还未同步的添加操作的数据为\n" + addJsonStr);
            String deleteJsonStr = recordFile.readByFileName(TALLY_FOLTER_DELETE);
            String updateJsonStr = recordFile.readByFileName(TALLY_FOLTER_UPDATE);
            if(!jsonIsEmpty(addJsonStr) || !jsonIsEmpty(deleteJsonStr) || !jsonIsEmpty(updateJsonStr)){
                toastMeesage("开始同步账单");
            }
            //无网时有添加
            if(addJsonStr != null){
                List<Bill> addBillList = gson.fromJson(addJsonStr,new TypeToken<List<Bill>>(){}.getType());
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillHandler,
                        ADD_SOME_BILLS_URL,addJsonStr));
            }
/*            //无网时有删除
            if(deleteJsonStr != null){
                List<Bill> addBillList = gson.fromJson(deleteJsonStr,new TypeToken<List<Bill>>(){}.getType());
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillHandler,
                        ADD_SOME_BILLS_URL,addJsonStr));
            }
            //无网时有修改
            if(updateJsonStr != null){
                List<Bill> addBillList = gson.fromJson(updateJsonStr,new TypeToken<List<Bill>>(){}.getType());
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillHandler,
                        ADD_SOME_BILLS_URL,addJsonStr));
            }*/

        }

        //没有网络接入就直接加载本地数据
        setBillListAdapterFromSD();

        //在ListView中显示
        billList = (ListView) view.findViewById(R.id.bill_list);
        billList.setAdapter(billListadapter);
        billList.setOnItemClickListener(new MyOnItemClickListener());

        inoutRatioPb = (ProgressBar) view.findViewById(R.id.inout_ratio_pb);
        //更新进度条
        updateInoutRatioPb();

        folderFragment = BillFolderFragment.getInstance();

        folderIb = (ImageButton) view.findViewById(R.id.show_folder_ib);
        getActivity().setTitle(folder + "的" + "账单");        //修改标题

        folderIb.setOnClickListener(new View.OnClickListener() {
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
        getActivity().setTitle(folder + "的" + "账单");        //修改标题
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADD_BILL_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                Bill newBill = (Bill) data.getSerializableExtra("newBill");
                newBill.setUid(uid);

                String newBillJsonStr =  gson.toJson(newBill);
                if(IsNet.isConnect(getActivity())){                           //有网就添加
                    ThreadPoolUtils.execute(new HttpPostThread(postOneBillHandler,
                            ADD_ONE_BILL_URL,newBillJsonStr));
                }else{
                    //Log.i("lanbitou","没联网");
                    FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER,TALLY_FOLTER_ADD);
                    fileUtil.appendToJsonListTop(newBillJsonStr);
                }

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

                //有没有网写入到SD卡中
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

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int potion, long l) {

        }
    }

    private  Handler postOneBillHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    toastMeesage("数据同步成功");
                    break;
                default:
                    break;
            }
        }
    };

    private Handler postSomeBillHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    if(msg.obj.toString().equals("200")){
                        //清空记录数据
                        FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER,TALLY_FOLTER_ADD);
                        fileUtil.emptyFileContent();
                        toastMeesage("同步账单成功");
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private void toastMeesage(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean jsonIsEmpty(String jsonStr){
        return jsonStr == null || jsonStr.equals("") || jsonStr.isEmpty();
    }
}
