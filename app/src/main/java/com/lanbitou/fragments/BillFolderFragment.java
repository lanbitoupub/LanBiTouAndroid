package com.lanbitou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lanbitou.R;
import com.lanbitou.adapters.BillFolderAdapter;
import com.lanbitou.util.FileUtil;
import com.lanbitou.views.AddFolderDialog;

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
    private BillFolderAdapter billFolderAdapter;
    private int uid = 1;

    AddFolderDialog addFolderDialog;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_bill_folder,container,false);
        listView = (ListView) view.findViewById(R.id.bill_folder_lv);
        billFolderAdapter = new BillFolderAdapter(getActivity());
        listView.setAdapter(billFolderAdapter);

        listView.setOnItemClickListener(this);

        addFolderBtn = (Button) view.findViewById(R.id.add_bill_folder_btn);
        addFolderBtn.setOnClickListener(new AddBtnOnClickListener());

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
     * 在这里编辑
     * @param adapterView
     * @param view
     * @param i
     * @param l
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }

    /**
     * 该接口需要在Activity中实现,以Activity为媒介,实现碎片之间的数据传递
     */
    public interface OnFragmentReturnListener{
        /**
         *
         * @param folderName 得到的文件夹的名字
         */
        public void onFragmentReturn(String folderName);
    }

    private class AddBtnOnClickListener implements View.OnClickListener{
        String newFolderName;
        @Override
        public void onClick(View view) {

            addFolderDialog = new AddFolderDialog(getActivity());
            //点击完成
            addFolderDialog.setOnFinishListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText folderNameEt = addFolderDialog.getTextEdite();
                    newFolderName = folderNameEt.getText().toString();

                    if(!newFolderName.equals("")){
                        new FileUtil("/bill/" + uid, newFolderName);
                        billFolderAdapter.addItem(newFolderName);
                        billFolderAdapter.notifyDataSetChanged();

                        addFolderDialog.dismiss();
                    }else {
                        Toast.makeText(getActivity(),"不能为空",Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //点击取消
            addFolderDialog.setOnCancelListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                addFolderDialog.dismiss();
                }
            });
            addFolderDialog.show();
        }
    }


}
