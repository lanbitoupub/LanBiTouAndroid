package com.lanbitou.views;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lanbitou.R;

/**
 * 带有编辑框的对话框
 * Created by Henvealf on 16-5-20.
 */
public class EditDialog extends Dialog{

    private EditText folderNameEt;
    private Button finishBtn, cancelBtn;

    public EditDialog(Context context) {
        super(context);
        setFolderDialog();
    }


    private void setFolderDialog(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_add_folder,null);
        folderNameEt = (EditText) view.findViewById(R.id.folder_name_et);
        finishBtn = (Button) view.findViewById(R.id.finish_btn);
        cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        super.setContentView(view);
    }

    /**
     * 设置原本的字符串
     * @param oldText
     */
    public void setOldText(String oldText){
        folderNameEt.setText(oldText);
    }

    public EditText getTextEdite(){
        return this.folderNameEt;
    }

    /**
     * 确定键监听器
     * @param listener
     */
    public void setOnFinishListener(View.OnClickListener listener){
        finishBtn.setOnClickListener(listener);
    }

    /**
     * 取消键监听器
     * @param listener
     */
    public void setOnCancelListener(View.OnClickListener listener){
        cancelBtn.setOnClickListener(listener);
    }

}
