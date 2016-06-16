package com.lanbitou.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.activities.NoteShowActivity;
import com.lanbitou.adapters.NoteAdapter;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.IsNet;
import com.lanbitou.net.NoteUrl;
import com.lanbitou.service.SyncService;
import com.lanbitou.thread.HttpGetThread;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.ArrayUtil;
import com.lanbitou.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by joyce on 16-5-13.
 */
public class NewestNotesFragment extends Fragment{

    private static final String TAG = "NewestNotesFragment";

    private TextView textView;
    private ListView listView;
    private NoteAdapter noteAdapter;
    private List<NoteEntity> noteListItems = new ArrayList<NoteEntity>();
    private Gson gson = new Gson();
    private Type noteListType = new TypeToken<List<NoteEntity>>() {}.getType();
    private FileUtil notesFileUtil = new FileUtil("/note", "/note.lan");
    private FileUtil updateFileUtil = new FileUtil("/note", "/update.lan");
    private FileUtil postFileUtil = new FileUtil("/note", "/post.lan");
    private FileUtil deleteFileUtil = new FileUtil("/note", "/delete.lan");
    private NoteEntity oneEntity;


    Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0x123://返回get数据
                    String json = (String) msg.obj;
                    if(json != null && !json.equals("")) {//返回get请求
                        if (msg.arg1 == 1) {
                            noteListItems.clear();
                            notesFileUtil.write(json);
                        }

//                        noteListItems = gson.fromJson(json, noteListType);
                        List<NoteEntity> newListItems = gson.fromJson(json, noteListType);
                        for(NoteEntity ne : newListItems) {
                            noteListItems.add(ne);
                            Log.i("tag",ne.toString());
                        }
                        noteAdapter.notifyDataSetChanged();
                    }
                    break;
                case 0x124://返回post数据
                    String result = (String) msg.obj;
                    if (result.equals("updateOne")) {//返回updateOne请求

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_newest_notes, container,false);

        textView = (TextView) view.findViewById(R.id.textview);
        listView = (ListView) view.findViewById(R.id.listview);

        noteListItems.clear();
        noteAdapter = new NoteAdapter(this.getActivity(), noteListItems);
        listView.setAdapter(noteAdapter);

        Log.i("","");


        String result = "";
        if(!(result = notesFileUtil.read()).equals("")) {
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                NoteEntity ne = (NoteEntity) listView.getItemAtPosition(position);
                Log.i("TAG",ne.getTitle());

                Intent intent = new Intent(getActivity(), NoteShowActivity.class);
                String neJson= gson.toJson(ne);
                intent.putExtra("isNew", false);
                intent.putExtra("neJson",neJson);
                intent.putExtra("itemid", id);
                intent.putExtra("activityid", 1);
                startActivityForResult(intent, 1);
            }
        });


        return view;
    }

    private void refresh() {

        if (IsNet.isConnect(getActivity())) {

            ThreadPoolUtils.execute(new HttpGetThread(handler, NoteUrl.NOTE_GETALL));

        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == 100) {

            noteListItems.clear();
            String notelistJson = notesFileUtil.read();
            List<NoteEntity> newListItems = gson.fromJson(notelistJson, noteListType);
            for(NoteEntity ne : newListItems) {
                noteListItems.add(ne);
                Log.i("tag",ne.toString());
            }
//            noteListItems = gson.fromJson(notelistJson, noteListType);
            noteAdapter.notifyDataSetChanged();



            /***[6.15]原本是获取activity返回的增删改查等数据的变化，并相应修改listview***/
//            boolean isNew = data.getBooleanExtra("isNew", false);
//            boolean isDelete = data.getBooleanExtra("isDelete", false);
//            Log.i("tag",isNew+"");
//            long itemid = data.getLongExtra("itemid", -1);
//            String newjson = data.getStringExtra("newjson");
//            NoteEntity newNoteEntity = gson.fromJson(newjson, NoteEntity.class);
//
//            if (isNew) {
//                listItems.add(newNoteEntity);
//            }
//            else if (isDelete) {
//                listItems.remove((int)itemid);
//            }
//            else {
//                listItems.remove((int)itemid);
//                listItems.add((int)itemid, newNoteEntity);
//            }
//
//            fileUtil.write(gson.toJson(listItems, listType));
//            noteAdapter.notifyDataSetChanged();
        }

    }
}