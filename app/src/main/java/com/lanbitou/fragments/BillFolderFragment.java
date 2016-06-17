package com.lanbitou.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.adapters.BillFolderAdapter;
import com.lanbitou.entities.Bill;
import com.lanbitou.entities.BillFolder;
import com.lanbitou.net.BillUrl;
import com.lanbitou.net.IsNet;
import com.lanbitou.thread.HttpGetThread;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.FileUtil;
import com.lanbitou.views.EditDialog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * Created by Henvealf on 16-5-19.
 */
public class BillFolderFragment extends Fragment
                                implements AdapterView.OnItemClickListener,
                                           AdapterView.OnItemLongClickListener{

    private ListView listView;
    private OnFragmentReturnListener mListener;
    private Button addFolderBtn;
    private static BillFolderAdapter billFolderAdapter;
    private int uid = -1;

    Gson gson;

    EditDialog editDialog;
    /**
     *
     * @return
     */
    public static BillFolderFragment getInstance(){
        BillFolderFragment billFolderFragment = new BillFolderFragment();
        //Bundle b = new Bundle();
        //b.putString("billFolderName",billFolderName);
       // billFolderFragment.setArguments(b);
        return billFolderFragment;

    }

    public BillFolderFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bill_folder,container,false);
        //TODO uid每次都要获取一遍,待解决.
        //获取uid
        SharedPreferences sharedPreferences =
                getActivity().getSharedPreferences("lanbitou", Context.MODE_PRIVATE);
        uid = sharedPreferences.getInt("uid",-1);

        listView = (ListView) view.findViewById(R.id.bill_folder_lv);
        billFolderAdapter = new BillFolderAdapter(getActivity(),uid);
        listView.setAdapter(billFolderAdapter);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        addFolderBtn = (Button) view.findViewById(R.id.add_bill_folder_btn);
        addFolderBtn.setOnClickListener(new AddBtnOnClickListener());
        gson = new Gson();
        return view;
    }

    public void setOnFragmentReturnListener(OnFragmentReturnListener mListener){
        this.mListener = mListener;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        TextView folderNameTv = (TextView) view.findViewById(R.id.bill_folder_list_item_tv);
        String folderName = folderNameTv.getText().toString();

        mListener.onFragmentReturn(folderName);
    }

    /**
     * 在这里弹出编辑或者删除选项
     * @param adapterView
     * @param view
     * @param position
     * @param l
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view,final int position, long l) {
        String[] items = { "重命名", "删除该账单" };
        final BillFolder oldBillFolder = (BillFolder)billFolderAdapter.getItem(position);   //旧名字
        oldBillFolder.setUid(uid);
        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle("操作收支:" + oldBillFolder.getName())
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:                                                 //修改
                                        editDialog = new EditDialog(getActivity());
                                        editDialog.setOldText(oldBillFolder.getName());     //显示旧名字
                                        editDialog.setOnFinishListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                EditText editText = editDialog.getTextEdite();
                                                String newFolderName = editText.getText().toString().trim();    //得到新名字

                                                if(!newFolderName.isEmpty()){

                                                    String raResult = new FileUtil("/bill/" + uid)
                                                                            .rename(oldBillFolder.getName(),newFolderName);
                                                    billFolderAdapter.setFolderList();
                                                    billFolderAdapter.notifyDataSetChanged();
                                                    if(raResult == null){               //没消息,说明更名成功

                                                        BillFolder newBillFolder = new BillFolder(uid,newFolderName);
                                                        String newFolderJson = gson.toJson(newBillFolder);
                                                        //String oldFolderJson = gson.toJson(oldBillFolder);

                                                        if(IsNet.isConnect(getActivity())){                         //有网,直接同步修改
                                                            List<BillFolder> goUpdateList = new ArrayList<BillFolder>();
                                                            goUpdateList.add(oldBillFolder);
                                                            goUpdateList.add(newBillFolder);
                                                            String goUpdateJson = gson.toJson(goUpdateList);
                                                            ThreadPoolUtils.execute(new HttpPostThread(postFolderHandler,
                                                                    BillUrl.UPDATE_FOLDER,goUpdateJson,0x128));
                                                        }else{                                          //没网,记录
                                                            renameFolderInBillFolder(newFolderName);
                                                            //检查记录add bill 的文件中之后有处于被命名的文件夹中的BIll,有就更新
                                                            billTallyFolderHasInReFolderBill(oldBillFolder.getName(),newFolderName,BillFragment.TALLY_FOLTER_ADD);
                                                                //检查记录update bill
                                                            billTallyFolderHasInReFolderBill(oldBillFolder.getName(),newFolderName,BillFragment.TALLY_FOLTER_UPDATE);


                                                            if(updateAddTallySuccess(oldBillFolder.getName(),newFolderName)){//先检查记录的Folder add中是否有该文件,有就更新,成功了
                                                            }else {                         //上一步没成功,说明是已经在云端的账单夹,放在记录文件夹中
                                                                String oldFolderName = oldBillFolder.getName();
                                                                FileUtil updateFolderTallylFile = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,BillFragment.TALLY_FOLTER_UPDATE_FOLDER);
                                                                String updateFolderTallyJson = updateFolderTallylFile.read();               //读出来
                                                                if(!jsonIsEmpty(updateFolderTallyJson)){        //不为空
                                                                    List<BillFolder> updateFolderTallyList
                                                                            = gson.fromJson(updateFolderTallyJson,new TypeToken<List<BillFolder>>(){}.getType());
                                                                    boolean isFind = false;
                                                                    for (int i = 0; i < updateFolderTallyList.size(); i++) {
                                                                        if(i % 2 == 1){//为奇数  1,3,5,7
                                                                            BillFolder billFolder = updateFolderTallyList.get(i);
                                                                            if(billFolder.getName().equals(oldFolderName)){         //与第二相同,说明是第二次更新
                                                                                billFolder.setName(newFolderName);                  //更新
                                                                                isFind = true;
                                                                            }
                                                                        }
                                                                    }
                                                                    if(isFind){                         //找到是再次更新//重写文件
                                                                        updateFolderTallyJson = gson.toJson(updateFolderTallyList);
                                                                        updateFolderTallylFile.write(updateFolderTallyJson);
                                                                    }else{                          //没找到,就直接添加
                                                                        String oldJson = gson.toJson(oldBillFolder);
                                                                        String newJson = gson.toJson(newBillFolder);
                                                                        updateFolderTallylFile.appendToJsonListTail(oldJson);
                                                                        updateFolderTallylFile.appendToJsonListTail(newJson);
                                                                    }
                                                                }else{                  //为空就直接添加
                                                                    String oldJson = gson.toJson(oldBillFolder);
                                                                    String newJson = gson.toJson(newBillFolder);
                                                                    updateFolderTallylFile.appendToJsonListTail(oldJson);
                                                                    updateFolderTallylFile.appendToJsonListTail(newJson);
                                                                }
                                                            }
                                                        }

                                                        editDialog.dismiss();
                                                    } else {                            //更名失败,显示失败原因
                                                        toastMeesage(raResult);
                                                    }

                                                }else{
                                                    toastMeesage("名字不能为空");
                                                }
                                            }
                                        });

                                        editDialog.setOnCancelListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                editDialog.dismiss();
                                            }
                                        });
                                        editDialog.show();
                                        break;
                                    case 1:                     //删除
                                        confirmDialog(position);
                                        break;
                                }
                            }
                        });
        builder.create().show();
        return false;
    }

    /**
     * 检查是否当前更名的文件夹下的Bill在bill记录有没有,就更新
     * @param
     * @param newFolderName
     * @return
     */
    private boolean billTallyFolderHasInReFolderBill(String oldName, String newFolderName, String tallyFolder) {
        FileUtil billTallyFile = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,tallyFolder);
        String billTallyJson = billTallyFile.read();
        if(!jsonIsEmpty(billTallyJson)){
            List<Bill> billTallyList = gson.fromJson(billTallyJson,new TypeToken<List<Bill>>(){}.getType());
            for(int i = 0; i < billTallyList.size(); i ++){
                Bill bill = billTallyList.get(i);
                if(bill.getFolder().equals(oldName)){           //有
                    bill.setFolder(newFolderName);
                }
            }

            billTallyJson = gson.toJson(billTallyList);
            billTallyFile.write(billTallyJson);
            return true;
        }else {
            return false;
        }

    }

    /**
     * 实现碎片之间的数据传递
     */
    public interface OnFragmentReturnListener{
        /**
         *
         * @param folderName 得到的文件夹的名字
         */
        public void onFragmentReturn(String folderName);
    }

    /**
     * 添加新账单夹事件
     */
    private class AddBtnOnClickListener implements View.OnClickListener{
        String newFolderName;
        @Override
        public void onClick(View view) {

            editDialog = new EditDialog(getActivity());
            //点击完成
            editDialog.setOnFinishListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText folderNameEt = editDialog.getTextEdite();
                    newFolderName = folderNameEt.getText().toString();
                    newFolderName = newFolderName.trim();
                    if(!newFolderName.isEmpty()){
                        if(new FileUtil("/bill/" + uid).isExists(newFolderName)){
                            toastMeesage("该名字已经存在");
                        }else{
                            new FileUtil("/bill/" + uid, newFolderName);            //新建文件夹

                            BillFolder newBillFolder = new BillFolder(uid,newFolderName);

                            if(IsNet.isConnect(getActivity())){                     //有网,直接上传
                                String folderJson = gson.toJson(newBillFolder);
                                ThreadPoolUtils.execute(new HttpPostThread(postFolderHandler,
                                        BillUrl.ADD_ONE_FOLDER,folderJson));
                            }else{                                                  //无网
                                FileUtil fileUtil
                                        = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,
                                                    BillFragment.TALLY_FOLTER_ADD_FOLDER);
                                newBillFolder.setInClouded(false);
                                String folderJson = gson.toJson(newBillFolder);
                                fileUtil.appendToJsonListTail(folderJson);          //记录文件夹名字\
                            }
                            billFolderAdapter.addItem(newBillFolder);
                            billFolderAdapter.notifyDataSetChanged();

                            editDialog.dismiss();
                        }
                    }else {
                        Toast.makeText(getActivity(),"不能为空",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //点击取消
            editDialog.setOnCancelListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                editDialog.dismiss();
                }
            });
            editDialog.show();
        }
    }

    Handler postFolderHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            checkSynch(msg);
        }
    };

    private void toastMeesage(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void confirmDialog(final int position) {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("删除后无法恢复");
        builder.setTitle("确认删除?");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                BillFolder willDelete = (BillFolder) billFolderAdapter.getItem(position);
                List<BillFolder> deleteList = new ArrayList<BillFolder>();
                String deleteName = willDelete.getName();
                deleteList.add(willDelete);

                String deleteListJson = gson.toJson(deleteList);

                FileUtil fileUtil = new FileUtil("/bill/" + uid);
                fileUtil.ldelete(deleteName);

                String deleteJson = gson.toJson(willDelete);

                if(IsNet.isConnect(getActivity())){                 //有网
                    ThreadPoolUtils.execute(new HttpPostThread(postFolderHandler,
                            BillUrl.DELETE_SOME_BILLS_FOLDER_URL,deleteListJson,0x126));
                } else {                                            //无网
                    if(deleteAddTallySuccess(deleteName)){    //删除的是未同步时的添加,就把相应记录删除.

                    }else if(afterUpdateThenDelete(deleteName)){//删除的是已经同步后无网更新的文件夹.就把更新记录更新.

                    }
                    else {                                  //删除的是已经同步的文件夹,就记录
                        FileUtil deleteTallyFile = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,BillFragment.TALLY_FOLTER_DELETE_FOLDER);
                        deleteTallyFile.appendToJsonListTail(deleteJson);
                    }
                }

                billFolderAdapter.setFolderList();
                billFolderAdapter.notifyDataSetChanged();
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

    /**
     * 删除的是同步后无网有更新的账单夹
     * @param deleteName
     * @return
     */
    private boolean afterUpdateThenDelete(String deleteName) {
        //先检查记录文件夹update的记录文件是否有内容
        FileUtil updateTallyFile = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,BillFragment.TALLY_FOLTER_UPDATE_FOLDER);
        String updateTallyJson = updateTallyFile.read();
        List<BillFolder> updateTallyList= gson.fromJson(updateTallyJson,new TypeToken<List<BillFolder>>(){}.getType());
        if(updateTallyList == null || updateTallyList.isEmpty()){
            return false;
        }
        for (int i = 0; i < updateTallyList.size(); i++) {
            if(i % 2 == 1){          //取位置在偶数的记录
                BillFolder billFolder = updateTallyList.get(i);
                if(billFolder.getName().equals(deleteName)){    //找到同名,意思就是之前更新过
                    updateTallyList.remove(i);                  //删除匹配的两对
                    updateTallyList.remove(i - 1);
                }
            }
        }

        updateTallyJson = gson.toJson(updateTallyList);
        updateTallyFile.write(updateTallyJson);
        return true;
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
                    FileUtil fileUtil = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,tallyFolder);
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

    /**
     * 判断添加账单夹的记录中是否存在某记录.存在就修改后写入
     * @param folderName
     * @return
     */
    private boolean updateAddTallySuccess( String folderName, String newName ){
        FileUtil fileUtil = new FileUtil("/bill/" + uid + BillFragment.TALLY_FOLTER,BillFragment.TALLY_FOLTER_ADD_FOLDER);
        //本地要改名字的账单对应的文件夹,在后面要将其里面的Bill们都更名

        String addJson = fileUtil.read();
        List<BillFolder> addBillFolders =  gson.fromJson(addJson,new TypeToken<List<BillFolder>>(){}.getType());
        if(addBillFolders == null || addBillFolders.isEmpty()){
            return false;
        }

        for(int i = 0; i < addBillFolders.size(); i++){
            BillFolder bf = addBillFolders.get(i);
            if(bf.getName().equals(folderName)){            //名字存在
                if(newName == null) {                       //为空表示为删除操作
                    addBillFolders.remove(bf);
                }else{                                      //不为空便为更名

                    bf.setName(newName);                    //改名字
                }
                if(!addBillFolders.isEmpty()){
                    String newJson = gson.toJson(addBillFolders);  //生成新的
                    fileUtil.write(newJson);                    //重新写入
                } else {
                    fileUtil.emptyFileContent();
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 判断添加账单夹的记录中是否存在某记录.存在就删除该记录
     * @param folderName
     * @return
     */
    private boolean deleteAddTallySuccess( String folderName  ){
        return updateAddTallySuccess(folderName, null);
    }


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
            updateListView();
        }
    };

    /**
     * 将文件名newName的文件中的所有json串里的folder字段更新为newName
     * @param newName
     */
    private void renameFolderInBillFolder(String newName){
        FileUtil billFolderFile = new FileUtil("/bill/" + uid,newName);  //在这之前已经本地已经更名,所以是读取新的
        String billsJson = billFolderFile.read();           //读取旧文件中的bill们
        //Log.i("lanbitou","被重命名之前的账单夹里面的账单的json " + billsJson);
        //将bill们的所在的文件夹也更名
        if(!jsonIsEmpty(billsJson)){
            List<Bill>  billsList = gson.fromJson(billsJson,new TypeToken<List<Bill>>(){}.getType());
            for(int j = 0; j < billsList.size(); j++){
                //Log.i("lanbitou","被重命名之前的账单夹里面的账单的folder: " + billsList.get(j).getFolder());
                billsList.get(j).setFolder(newName);
                //Log.i("lanbitou","-被重命名之后的账单夹里面的账单的folder: " + billsList.get(j).getFolder());
            }
            String newBillsJson = gson.toJson(billsList);
            billFolderFile.write(newBillsJson);
        }
    }


    private boolean jsonIsEmpty(String jsonStr){
        return jsonStr == null || jsonStr.equals("") || jsonStr.isEmpty() || jsonStr.equals("[]");
    }

    public static void updateListView(){
        billFolderAdapter.setFolderList();
        billFolderAdapter.notifyDataSetChanged();
    }


}
