package com.lanbitou.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.activities.AddBillActivity;
import com.lanbitou.adapters.BillListAdapter;
import com.lanbitou.entities.Bill;
import com.lanbitou.entities.BillFolder;
import com.lanbitou.net.BillUrl;
import com.lanbitou.net.IsNet;
import com.lanbitou.thread.HttpGetThread;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.FileUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 点击右下角的按钮弹出文件夹分类
 * 点击添加弹出添加的Activity.里面有三个选项,即 类型(限制在4个字符内),类型,钱数,备注,时间.
 * Created by Henvealf on 16-5-13.
 */
public class BillFragment extends Fragment implements BillFolderFragment.OnFragmentReturnListener {


    private final static int ADD_BILL_REQUEST_CODE = 1;
    private final static int UPDATE_BILL_REQUEST_CODE = 2;

    public final static String TALLY_FOLTER = "/.tallyLastOperate";            //无网时记录更改的文件,修改这里时别忘了FileUtil里
    private final static String TALLY_FOLTER_ADD = "add";
    private final static String TALLY_FOLTER_UPDATE = "update";
    private final static String TALLY_FOLTER_DELETE = "delete";
    public final static String TALLY_FOLTER_ADD_FOLDER = "folderAdd";
    public final static String TALLY_FOLTER_DELETE_FOLDER = "folderDelete";
    public final static String TALLY_FOLTER_UPDATE_FOLDER = "folderUpdate";

    private Button toAddBillBtn;
    private ImageButton folderIb;
    private ProgressBar inoutRatioPb;          //收支比例进度条
    private TextView inMoneyTv, outMoneyTv;
    ListView billListView;
    private double totalMoney;
    private double inTotalMoney;

    private BillListAdapter billListadapter;


    private int uid = -1;                         //登陆用户Id
    private String folder;          //所在文件夹,默认为 日常

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

        SharedPreferences preferences = getActivity().getSharedPreferences("lanbitou", Context.MODE_PRIVATE);

        uid = preferences.getInt("uid", -1);

        List<String> names = new FileUtil("/bill/" + uid).getInterFileName();
        if(names.isEmpty()){
            folder = "日常";
            new FileUtil("/bill/" + uid,folder);
            BillFolder bf = new BillFolder(uid,folder);
            List<BillFolder> list = new ArrayList<BillFolder>();
            list.add(bf);
            String firstBillFolderJson = gson.toJson(list);
            if(IsNet.isConnect(getActivity())){
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillFolderHandler,
                        BillUrl.ADD_SOME_BILLS_FOLDER_URL,firstBillFolderJson,0x124));
            } else {
                FileUtil fu = new FileUtil("/bill/" + uid + TALLY_FOLTER,TALLY_FOLTER_ADD_FOLDER);
                fu.appendToJsonListTail(firstBillFolderJson);
            }
            toastMeesage("已经自动添加一个名为 日常 的默认账单");
        }else{
            folder = names.get(0);
        }

        toAddBillBtn = (Button) view.findViewById(R.id.go_add_bill_btn);
        inMoneyTv = (TextView) view.findViewById(R.id.in_money_tv);
        outMoneyTv = (TextView) view.findViewById(R.id.out_money_tv);
        //点击跳转到添加Bill的Activity
        toAddBillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =
                        new Intent(getActivity(),AddBillActivity.class);
                i.putExtra("folder",folder);  //传过去文件夹,以备查看与修改
                i.putExtra("operate","添加"); //操作
                startActivityForResult(i,ADD_BILL_REQUEST_CODE);
            }
        });

        billListadapter = new BillListAdapter(this.getActivity());

        //此处检查用户的网络状况,有网络连接就先获取云端数据,然后与本地比较后保存在本地.
        if(IsNet.isConnect(getActivity())){

            //检查本地情况,是否有记录账单操作的标记文件
            FileUtil recordFile = new FileUtil("/bill/" + uid + TALLY_FOLTER);
            String addJsonStr = recordFile.readByFileName(TALLY_FOLTER_ADD);
            String deleteJsonStr = recordFile.readByFileName(TALLY_FOLTER_DELETE);
            String updateJsonStr = recordFile.readByFileName(TALLY_FOLTER_UPDATE);

            String addFolderJsonStr = recordFile.readByFileName(TALLY_FOLTER_ADD_FOLDER);
            String deleteFolderJsonStr = recordFile.readByFileName(TALLY_FOLTER_DELETE_FOLDER);
            String updateFolderJsonStr = recordFile.readByFileName(TALLY_FOLTER_UPDATE_FOLDER);

            if( !jsonIsEmpty(addJsonStr)          ||
                !jsonIsEmpty(deleteJsonStr)       ||
                !jsonIsEmpty(updateJsonStr)       ||
                !jsonIsEmpty(addFolderJsonStr)    ||
                !jsonIsEmpty(deleteFolderJsonStr) ||
                !jsonIsEmpty(updateFolderJsonStr)   ){
                toastMeesage("开始同步账单");
            }
            //无网时有添加
            if(!jsonIsEmpty(addJsonStr)){
                Log.i("lanbitou","还未同步的添加操作的数据为\n" + addJsonStr);
                //List<Bill> addBillList = gson.fromJson(addJsonStr,new TypeToken<List<Bill>>(){}.getType());
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillHandler,
                        BillUrl.ADD_SOME_BILLS_URL,addJsonStr,0x124));
            }
            //无网时有删除
            if(!jsonIsEmpty(deleteJsonStr)){
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillHandler,
                        BillUrl.DELETE_SOME_BILLS_URL,deleteJsonStr,0x126));    //删除时126
            }

            //无网时有修改
            if(!jsonIsEmpty(updateJsonStr)){
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillHandler,
                        BillUrl.UPDATE_SOME_BILLS_URL,updateJsonStr,0x128));    //修改时128
            }

            //无网时文件夹有添加
            if( !jsonIsEmpty(addFolderJsonStr) ){
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillFolderHandler,
                        BillUrl.ADD_SOME_BILLS_FOLDER_URL,addFolderJsonStr,0x124));
            }

            //无网时文件夹右删除
            if( !jsonIsEmpty(deleteFolderJsonStr) ){
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillFolderHandler,
                        BillUrl.DELETE_SOME_BILLS_FOLDER_URL,deleteFolderJsonStr, 0x126));
            }

            //无网时文件夹名有修改
            if( !jsonIsEmpty(updateFolderJsonStr) ){
                ThreadPoolUtils.execute(new HttpPostThread(postSomeBillFolderHandler,
                        BillUrl.UPDATE_FOLDER,updateFolderJsonStr,0x128));
            }
            downloadAndReviewAllBills();
        }

        //没有网络接入就直接加载本地数据
        setBillListAdapterFromSD();

        //在ListView中显示
        billListView = (ListView) view.findViewById(R.id.bill_list);
        billListView.setAdapter(billListadapter);
        billListView.setOnItemClickListener(new MyOnItemClickListener());

        inoutRatioPb = (ProgressBar) view.findViewById(R.id.inout_ratio_pb);
        //更新进度条
        updateInoutRatioPb();

        folderFragment = BillFolderFragment.getInstance();

        folderIb = (ImageButton) view.findViewById(R.id.show_folder_ib);
        getActivity().setTitle(folder + "的" + "账单");        //修改标题

        folderIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFolder(true);
            }
        });
        folderFragment.setOnFragmentReturnListener(this);

        showFolder(false);

        return view;
    }

    /**
     * 当文件夹碎片返回时获取文件夹名字,执行其他其他一些操作
     * @param folderName 得到的文件夹的名字
     */
    @Override
    public void onFragmentReturn(String folderName) {
        billListView.setEnabled(true);
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
        //添加返回
        if(requestCode == ADD_BILL_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                Bill newBill = (Bill) data.getSerializableExtra("newBill");
                newBill.setUid(uid);

                if(IsNet.isConnect(getActivity())){                           //有网就添加
                    String newBillJsonStr =  gson.toJson(newBill);
                    ThreadPoolUtils.execute(new HttpPostThread(postOneBillHandler,
                            BillUrl.ADD_ONE_BILL_URL,newBillJsonStr));
                }else{                                      //没网就先保存在本地记录中
                    newBill.setInClouded(false);            //标记为未同步
                    String newBillJsonStr =  gson.toJson(newBill);
                    FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER,TALLY_FOLTER_ADD);
                    fileUtil.appendToJsonListTail(newBillJsonStr);
                }

                //更新ListView
                billListadapter.addItemToTop(newBill);  //你变nowBillList也变
                billListadapter.notifyDataSetChanged();
            }
        //更新返回
        }else if(requestCode == UPDATE_BILL_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                Bill newBill = (Bill) data.getSerializableExtra("newBill");
                int position = data.getIntExtra("position",-1);
                newBill.setUid(uid);
                if(position != -1){
                    billListadapter.updateItem(position,newBill);
                    billListadapter.notifyDataSetChanged();
                    saveNowFolderBillListToSD();                    //存入到SD卡中
                    updateInoutRatioPb();
                }
                if (IsNet.isConnect(getActivity())) {                           //有网就直接更新到云端
                    String newBillJsonStr = gson.toJson(newBill);
                    ThreadPoolUtils.execute(new HttpPostThread(postOneBillHandler,
                            BillUrl.UPDATE_ONE_BILL_URL, newBillJsonStr));
                } else {                                                    //没网就先保存在本地记录中
                    if(!newBill.isInClouded()) {                            //修改的是未同步的.
                        //和删除相同,不必增加标记
                        FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER, TALLY_FOLTER_ADD);
                        List<Bill> notCloudBills = findAllNotCloudBills();
                        fileUtil.emptyFileContent();
                        if (!notCloudBills.isEmpty()) {               //不为空就重写
                            fileUtil.write(gson.toJson(notCloudBills));
                        }
                    }else{
                        newBill.setInClouded(false);                           //标记为未同步
                        String newBillJsonStr = gson.toJson(newBill);
                        FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER, TALLY_FOLTER_UPDATE);
                        fileUtil.appendToJsonListTail(newBillJsonStr);
                    }
                }

            }
        }
        //更新进度条
        updateInoutRatioPb();
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
     * 获取当前账单的list数据,存入SD卡
     */
    private void saveNowFolderBillListToSD(){
        //得到新的BillList的Json串
        String newBillListJsonStr = gson.toJson(nowBillList);
        Log.i("json",newBillListJsonStr);

        //有没有网写入到SD卡中
        FileUtil fileUtil = new FileUtil("/bill/" + uid, folder);
        fileUtil.write(newBillListJsonStr);
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
            inMoneyTv.setText("￥" + 0);
            outMoneyTv.setText("￥" +0);
        }else{
            inMoneyTv.setText("￥" + String.valueOf(inTotalMoney));
            outMoneyTv.setText("￥" +String.valueOf(totalMoney - inTotalMoney));
        }
        //更新进度条
        inoutRatioPb.setMax((int)totalMoney);
        inoutRatioPb.setProgress((int)inTotalMoney);


    }

    //点击修改或删除
    private class MyOnItemClickListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
            String[] items = { "修改收支", "删除收支" };

            android.app.AlertDialog.Builder builder =
                    new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("操作收支:" + nowBillList.get(position).getType())
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Intent i =
                                                new Intent(getActivity(),AddBillActivity.class);
                                        Bundle b = new Bundle();
                                        b.putSerializable("bill",nowBillList.get(position));
                                        b.putString("operate","修改");
                                        b.putInt("position",position);
                                        i.putExtras(b);
                                        startActivityForResult(i,UPDATE_BILL_REQUEST_CODE);
                                        break;
                                    case 1:
                                        confirmDialog(position);
                                        break;
                                    default:
                                        break;
                                }
                            }
                    });
            builder.create().show();
        }
    }


    private  Handler postOneBillHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            checkSynch(msg);
        }
    };

    private Handler postSomeBillHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x124:             //同步无网添加之后
                    checkSynch(msg,TALLY_FOLTER_ADD);
                    break;
                case 0x126:             //同步无网删除过后
                    checkSynch(msg,TALLY_FOLTER_DELETE);
                    break;
                case 0x128:             //同步无网更新过后
                    checkSynch(msg,TALLY_FOLTER_UPDATE);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * 下载所有的账单数据,并保存在本地
     */
    public void downloadAndReviewAllBills(){
        ThreadPoolUtils.execute(new HttpGetThread(getAllByUidHander, BillUrl.GET_SOME_BILLS_BY_UID + this.uid));
    }


    private Handler getAllByUidHander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    String billListJson = msg.obj.toString();
                    if(billListJson != null){
                        Log.i("lanbitou","用户--" + uid + " 的所有账单数据为:" + billListJson);
                        List<Bill> addBillList = gson.fromJson(billListJson,new TypeToken<List<Bill>>(){}.getType());
                        FileUtil fileUtil = new FileUtil("/bill/" + uid, null);

                        Set<String> folderNameSet = new TreeSet<>();
                        List<String> folderNameList = new ArrayList<>();
                        for(int i = 0; i < addBillList.size(); i ++){
                            folderNameSet.add(addBillList.get(i).getFolder());
                        }
                        folderNameList.addAll(folderNameSet);
                        //遍历,将在各个账单夹的收支分开保存到各个文件中
                        for(int i = 0; i < folderNameList.size(); i ++){
                            String name = folderNameList.get(i);
                            Log.i("lanbitou","文件夹名字为: " + name);
                            List<Bill> isNameBill = new LinkedList<>();
                            for(int j = addBillList.size() -1 ; j > -1; j --){
                                Bill bill = addBillList.get(j);
                                if(bill.getFolder().equals(name)){
                                    if (bill.getMoney() != 0) {
                                        isNameBill.add(bill);
                                    }
                                }
                            }
                            //分别写入不同的文件中
                            FileUtil fileUtil1 = new FileUtil("/bill/" + uid, name);
                            //先清空
                            fileUtil1.emptyFileContent();
                            fileUtil1.write(gson.toJson(isNameBill));
                        }
                    }

                    break;
                default:
                    break;
            }
            setBillListAdapterFromSD();             //下载完再设置
        }
    };

    private Handler postSomeBillFolderHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x124:                  //添加
                    checkSynch(msg,TALLY_FOLTER_ADD_FOLDER);
                    break;
                case 0x126:                  //删除
                    checkSynch(msg,TALLY_FOLTER_DELETE_FOLDER);
                    break;
                case 0x128:                  //编辑
                    checkSynch(msg,TALLY_FOLTER_UPDATE_FOLDER);
                    break;
                default:
                    break;
            }
        }
    };

    private void confirmDialog(final int position) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("删除后无法恢复");
        builder.setTitle("确认删除?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bill deleteBill = nowBillList.get(position);
                billListadapter.removeItem(position);
                billListadapter.notifyDataSetChanged();

                saveNowFolderBillListToSD();                    //存入到SD卡中
                updateInoutRatioPb();
                //有网
                if(IsNet.isConnect(getActivity())){
                    Log.i("lanbitou","要删除的收支的id为: " + deleteBill.getId());
                    ThreadPoolUtils.execute(new HttpGetThread(postOneBillHandler,
                            BillUrl.DELETE_ONE_BILL_URL+"/" + deleteBill.getId(),0x125));
                }else{    //没网
                    //先放在记录文件夹中

                    if(!deleteBill.isInClouded()){           //不在云端,说明是在删除新添加且为同步的收支
                                                             //就重新获取一遍内存中的BillList,放入SD卡,
                                                             //对于云端来说就是此人从未存在过.
                        //saveNowFolderBillListToSD();       //然后更新记录add中的内容
                        FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER,TALLY_FOLTER_ADD);
                        List<Bill> notCloudBills = findAllNotCloudBills();
                        fileUtil.emptyFileContent();
                        if(!notCloudBills.isEmpty()){               //不为空就重写
                            fileUtil.write(gson.toJson(notCloudBills));
                        }
                    }else{                                  //在云端,就加入到记录文件中
                        FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER,TALLY_FOLTER_DELETE);
                        fileUtil.appendToJsonListTail(gson.toJson(deleteBill));
                    }
                }

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void toastMeesage(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private boolean jsonIsEmpty(String jsonStr){
        return jsonStr == null || jsonStr.equals("") || jsonStr.isEmpty();
    }

    /**
     * 得到所有还未同步的收支
     * @return
     */
    private List<Bill> findAllNotCloudBills(){
        FileUtil notCloudInFile = new FileUtil("/bill/" + uid, null);
        List<String> fileNameList = notCloudInFile.getInterFileName();
        List<Bill> notInCloudBills = new ArrayList<Bill>();     //没有被同步的收支们
        for(int i = 0; i < fileNameList.size(); i++) {
            String jsonStr = notCloudInFile.readByFileName(fileNameList.get(i));
            //获取一下
            List<Bill> inFileBills = gson.fromJson(jsonStr, new TypeToken<List<Bill>>() { }.getType());
            for (int j = 0; j < inFileBills.size(); j++) {
                Bill b = inFileBills.get(j);
                if (!b.isInClouded()) {           //未同步,就添加进去
                    notInCloudBills.add(b);
                }
            }
        }
        return notInCloudBills;
    }

    /**
     * 检查同步返回的结果,如果同步成功,就重新获取一遍所有账单到本地,并清空相应的记录文件.
     * @param msg 服务器传来的结果
     * @param tallyFolder 要清空的文件
     */
    private void checkSynch(Message msg, String tallyFolder){
        if(msg.obj.toString() != "" && msg.obj.toString() != null){
            if(Integer.parseInt(msg.obj.toString()) > 0){
                downloadAndReviewAllBills();
                if(tallyFolder != null){             //清空记录数据
                    FileUtil fileUtil = new FileUtil("/bill/" + uid + TALLY_FOLTER,tallyFolder);
                    fileUtil.emptyFileContent();
                }
                toastMeesage("同步账单成功");
            } else { toastMeesage("同步账单失败!请检查网络状况."); }
        }
        else { toastMeesage("同步账单失败!请检查网络状况."); }
    }

    /**
     * 检查同步返回的结果,如果同步成功,就重新获取一遍所有账单到本地.
     * @param msg 服务器传来的结果
     */
    private void checkSynch(Message msg){
        checkSynch(msg, null);
    }

    private void showFolder(boolean hasTransition){

        fragmentTransaction = getFragmentManager().beginTransaction();
        if(hasTransition){
            //加个动画
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        }

        if(!folderFragment.isAdded()){
            fragmentTransaction.add(R.id.bill_folder_fra,folderFragment);
        }
        fragmentTransaction.show(folderFragment);
        fragmentTransaction.commit();
        billListView.setEnabled(false);
    }
}
