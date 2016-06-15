package com.lanbitou.activities;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.activities.NoteShowActivity;
import com.lanbitou.adapters.NoteAdapter;
import com.lanbitou.entities.NoteBookEntity;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.IsNet;
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
public class NoteBookActivity extends AppCompatActivity {

    private String POSTONE = "http://192.168.1.108:8082/lanbitou/note/postOne";
    private String GETONE = "http://192.168.1.108:8082/lanbitou/note/getOne";
    private String GETSOME = "http://192.168.1.108:8082/lanbitou/note/getSome";
    private String UPDATEALL = "http://192.168.1.108:8082/lanbitou/note/updateAll";
    private String DELETEALL = "http://192.168.1.108:8082/lanbitou/note/deleteAll";
    private String POSTALL = "http://192.168.1.108:8082/lanbitou/note/postAll";

    private int bid = 1;

    private TextView textView;
    private ListView listView;
    private ImageView back;
    private NoteAdapter noteAdapter;
    private List<NoteEntity> noteListItems = new ArrayList<NoteEntity>();
    private Gson gson = new Gson();
    private Type noteListType = new TypeToken<List<NoteEntity>>() {}.getType();
    private FileUtil noteFileUtil = new FileUtil("/note", "/note.lan");
    private FileUtil updateFileUtil = new FileUtil("/note", "/update.lan");
    private FileUtil postFileUtil = new FileUtil("/note", "/post.lan");
    private FileUtil deleteFileUtil = new FileUtil("/note", "/delete.lan");

    private Context context = NoteBookActivity.this;


    Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0x123://返回get数据
                    String json = (String) msg.obj;
                    if(json != null && !json.equals("")) {//返回get请求
                        noteListItems.clear();

                        List<NoteEntity> newListItems = gson.fromJson(json, noteListType);

                        for(NoteEntity ne : newListItems) {
                            if (ne.getBid() == bid) {
                                noteListItems.add(ne);
                                Log.i("tag",ne.toString());
                            }
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_notebook);

        textView = (TextView) findViewById(R.id.textview);
        listView = (ListView) findViewById(R.id.listview);

        back = (ImageView) findViewById(R.id.back);
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        back.setOnClickListener(myOnClickListener);

        Intent intent = getIntent();
        String nbeJson = intent.getStringExtra("nbeJson");
        NoteBookEntity noteBookEntity = gson.fromJson(nbeJson, NoteBookEntity.class);
        bid = noteBookEntity.getBid();


        textView.setText(noteBookEntity.getName());

        noteListItems.clear();
        noteAdapter = new NoteAdapter(this, noteListItems);
        listView.setAdapter(noteAdapter);

        Log.i("","");



        String result = "";
        if(!(result = noteFileUtil.read()).equals("")) {
            Message msg = new Message();
            msg.what = 0x123;
            msg.obj = result;
            msg.arg1 = 0;//表示从本地获取的数据
            handler.sendMessage(msg);

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

                Intent intent = new Intent(context, NoteShowActivity.class);
                String neJson= gson.toJson(ne);
                intent.putExtra("isNew", false);
                intent.putExtra("neJson",neJson);
                intent.putExtra("itemid", id);
                intent.putExtra("actityid", 2);
                startActivityForResult(intent, 2);
            }
        });

    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    finish();
                    break;
                default:
                    break;

            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == 100) {

            Toast.makeText(context, "aaaaaaaaaaaa", Toast.LENGTH_SHORT).show();
            noteListItems.clear();

            String notelistJson = noteFileUtil.read();

            List<NoteEntity> newListItems = gson.fromJson(notelistJson, noteListType);

            for(NoteEntity ne : newListItems) {

                if (ne.getBid() == bid) {

                    noteListItems.add(ne);
                    Log.i("tag",ne.toString());
                }
            }
            noteAdapter.notifyDataSetChanged();




//            boolean isNew = data.getBooleanExtra("isNew", false);
//            boolean isDelete = data.getBooleanExtra("isDelete", false);
//            Log.i("tag",isNew+"");
//            long itemid = data.getLongExtra("itemid", -1);
//            String newjson = data.getStringExtra("newjson");
//            NoteEntity newNoteEntity = gson.fromJson(newjson, NoteEntity.class);
//
//            if (isNew) {
//                noteListItems.add(newNoteEntity);
//            }
//            else if (isDelete) {
//                noteListItems.remove((int)itemid);
//            }
//            else {
//                noteListItems.remove((int)itemid);
//                noteListItems.add((int)itemid, newNoteEntity);
//            }
//
//            noteAdapter.notifyDataSetChanged();
        }

    }

    private void refresh() {

        if (IsNet.isConnect(context)) {
            ThreadPoolUtils.execute(new HttpGetThread(handler, GETSOME + "/" + bid));
        }

    }
}