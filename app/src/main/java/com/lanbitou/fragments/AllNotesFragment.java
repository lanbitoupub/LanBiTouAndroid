package com.lanbitou.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.lanbitou.activities.NoteBookActivity;
import com.lanbitou.activities.NoteShowActivity;
import com.lanbitou.adapters.NoteAdapter;
import com.lanbitou.adapters.NoteBookAdapter;
import com.lanbitou.entities.NoteBookEntity;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.IsNet;
import com.lanbitou.net.NoteUrl;
import com.lanbitou.service.SyncService;
import com.lanbitou.thread.HttpGetThread;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.ArrayUtil;
import com.lanbitou.util.FileUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by joyce on 16-5-13.
 */
public class AllNotesFragment extends Fragment{

    private static final String TAG = "ALlNoteFragment";



    private TextView textView;
    private ListView listView;
    private Button addNoteBook;
    private NoteBookAdapter noteBookAdapter;
    private List<NoteBookEntity> noteBooklistItems = new ArrayList<NoteBookEntity>();
    private static Gson gson = new Gson();
    private static Type noteBookListType = new TypeToken<List<NoteBookEntity>>() {}.getType();
    private static FileUtil noteBookFileUtil = new FileUtil("/notebook", "/notebook.lan");
    private static FileUtil updateFileUtil = new FileUtil("/notebook", "/update.lan");
    private static FileUtil postFileUtil = new FileUtil("/notebook", "/post.lan");
    private static FileUtil deleteFileUtil = new FileUtil("/notebook", "/delete.lan");
    private NoteBookEntity noteBookEntity;

    private int uid;

    Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0x123://返回get数据
                    String json = (String) msg.obj;
                    if(json != null && !json.equals("")) {//返回get请求
                        if (msg.arg1 == 1) {
                            noteBooklistItems.clear();
                            noteBookFileUtil.write(json);

                        }

                        List<NoteBookEntity> newListItems = gson.fromJson(json, noteBookListType);
                        for(NoteBookEntity nbe : newListItems) {
                            noteBooklistItems.add(nbe);
                            Log.i("tag",nbe.getName());
                        }

                        noteBookAdapter.notifyDataSetChanged();
                    }
                    break;
                case 0x124://返回post数据
                    String result = (String) msg.obj;
                    if (result.substring(0,7).equals("postOne")) {

                        String bid_fid =  result.substring(8, result.length());
                        String[] newstid = bid_fid.split("#");
                        int newestbid = Integer.valueOf(newstid[0]);

                        noteBooklistItems.get(noteBooklistItems.size()-1).setBid(newestbid);
                        String notesJson = gson.toJson(noteBooklistItems, noteBookListType);
                        noteBookFileUtil.write(notesJson);
                    }
                    else if (result.equals("updateOne")) {//返回updateOne请求

                    }
                    else if (result.equals("updateAll")) {//返回updateAll请求
                        //清空update.lan文件
                        updateFileUtil.write("");
                    }
                    else if (result.equals("postAll")) {
                        postFileUtil.write("");

                    }
                    else if (result.equals("deleteAll")) {
                        deleteFileUtil.write("");

                    }
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_all_notes, container,false);

        textView = (TextView) view.findViewById(R.id.textview);
        listView = (ListView) view.findViewById(R.id.listview);
        addNoteBook = (Button) view.findViewById(R.id.add_notebook_btn);

        noteBooklistItems.clear();
        noteBookAdapter = new NoteBookAdapter(this.getActivity(), noteBooklistItems);
        listView.setAdapter(noteBookAdapter);

        Log.i("","");

        SharedPreferences preferences = getActivity().getSharedPreferences("lanbitou", Context.MODE_PRIVATE);

        uid = preferences.getInt("uid", 0);


        String result = "";
        if(!(result = noteBookFileUtil.read()).equals("")) {
            Message msg = new Message();
            msg.what = 0x123;
            msg.obj = result;
            msg.arg1 = 0;//表示从本地获取的数据
            handler.sendMessage(msg);

            if (IsNet.isConnect(getActivity())) {
                Intent intent = new Intent(getActivity(), SyncService.class);
                getActivity().startService(intent);
            }

            Timer timer = new Timer(true);
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    Looper.prepare();
                    refresh();
                    Looper.loop();
                }
            };
            timer.schedule(task, 3 * 1000);
        }
        else {
            refresh();
        }

        addNoteBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_addnotebook, null);

                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("添加笔记本")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                addNoteBook(view);

                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub

                            }
                        });
                dialog.show();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                NoteBookEntity nbe = (NoteBookEntity) listView.getItemAtPosition(position);
                Log.i("TAG",nbe.getName());

                Intent intent = new Intent(getActivity(), NoteBookActivity.class);
                String nbeJson= gson.toJson(nbe);
                intent.putExtra("nbeJson", nbeJson);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

                String[] items = { "删除笔记本", "重命名笔记本" };
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity())
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case 0:
                                        deleteNoteBook(position);
                                        break;
                                    case 1:
                                        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_addnotebook, null);
                                        AlertDialog.Builder updatedialog = new AlertDialog.Builder(getActivity())
                                                .setTitle("重命名笔记本")
                                                .setView(view)
                                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // TODO Auto-generated method stub
                                                        updateNoteBook(view ,position);

                                                    }
                                                })
                                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // TODO Auto-generated method stub

                                                    }
                                                });
                                        updatedialog.show();
                                        break;
                                    default:
                                        break;
                                }

                            }


                        });

                builder.create().show();


                return true;
            }
        });


        return view;
    }

    private void updateNoteBook(View view, int position) {

        EditText notebookname_et = (EditText) view.findViewById(R.id.notebookname_et);
        String notebookname = notebookname_et.getText().toString();
        Toast.makeText(getActivity(), notebookname, Toast.LENGTH_SHORT).show();


        noteBooklistItems.get(position).setName(notebookname);
        NoteBookEntity noteBookEntity = noteBooklistItems.get(position);
        String listJson = gson.toJson(noteBooklistItems, noteBookListType);
        noteBookFileUtil.write(listJson);
        noteBookAdapter.notifyDataSetChanged();

        String postJson = gson.toJson(noteBookEntity);
        if (IsNet.isConnect(getActivity())) {
            if (noteBookEntity.getBid() > 0)
            {
                ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTEBOOK_UPDATEONE, postJson));
            }
            else {
                ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTEBOOK_POSTONE, postJson));
            }

        }
        else {
            if (noteBookEntity.getBid() > 0)
            {
                updateFileUtil.write(noteBookEntity.getBid() + "#", true);
            }
            else {
                postFileUtil.write(noteBookEntity.getBid() + "#", true);

            }

        }


    }

    private void deleteNoteBook(int position) {

        NoteBookEntity noteBookEntity = noteBooklistItems.get(position);
        noteBooklistItems.remove(position);
        String listJson = gson.toJson(noteBooklistItems, noteBookListType);
        noteBookFileUtil.write(listJson);

        noteBookAdapter.notifyDataSetChanged();

        String postJson = gson.toJson(noteBookEntity);
        if (IsNet.isConnect(getActivity())) {

            ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTEBOOK_DELETEONE, postJson));
        }
        else {
            deleteFileUtil.write(noteBookEntity.getBid() + "#", true);

            //检查未联网情况下增加和修改的笔记并给予删除防止进行同步
            String postString = postFileUtil.read();
            String patternString = noteBookEntity.getBid() + "#";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(postString);
            String result = matcher.replaceAll("");
            postFileUtil.write(result);
            Log.i(TAG, result);

            String updateString = updateFileUtil.read();
            Matcher matcher_update = pattern.matcher(updateString);
            String result_update = matcher_update.replaceAll("");
            updateFileUtil.write(result_update);
            Log.i(TAG, result_update);



        }


    }

    private void addNoteBook(View view) {

        EditText notebookname_et = (EditText) view.findViewById(R.id.notebookname_et);
        String notebookname = notebookname_et.getText().toString();
        Toast.makeText(getActivity(), notebookname, Toast.LENGTH_SHORT).show();

        String result = "";
        int id;
        if((result = postFileUtil.read()).equals(""))  {
            id = -1;
        }
        else {
            Log.i("All", result);
            String num = result.substring(result.length()-3, result.length()-1);
            Log.i("All", num);
            id = Integer.valueOf(num);
            id--;
        }

        NoteBookEntity noteBookEntity = new NoteBookEntity(id, uid, notebookname, id);
        noteBooklistItems.add(noteBookEntity);
        String postJson = gson.toJson(noteBookEntity);

        String listJson = gson.toJson(noteBooklistItems, noteBookListType);
        noteBookFileUtil.write(listJson);


        if (IsNet.isConnect(getActivity())) {

            ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTEBOOK_POSTONE, postJson));
        }
        else {
            postFileUtil.write(noteBookEntity.getBid() + "#",true);
        }

    }



    private void refresh() {

        if (IsNet.isConnect(getActivity())) {
            ThreadPoolUtils.execute(new HttpGetThread(handler, NoteUrl.NOTEBOOK_GETALL));
        }

    }
}