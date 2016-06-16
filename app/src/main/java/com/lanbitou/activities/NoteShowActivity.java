package com.lanbitou.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.entities.NoteBookEntity;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.IsNet;
import com.lanbitou.net.NoteUrl;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteShowActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "NoteShowActivity";
    private EditText title;
    private EditText content;
    private Spinner select_notebook;

    private ImageView back;
    private ImageView ok;
    private ImageView delete;

    private Gson gson = new Gson();
    private NoteEntity noteEntity;
    private NoteEntity oldEntity;
    private Type noteBookListType = new TypeToken<List<NoteBookEntity>>() {}.getType();
    private Type noteListType = new TypeToken<List<NoteEntity>>() {}.getType();
    private List<NoteBookEntity> noteBookListItems = new ArrayList<NoteBookEntity>();

    private FileUtil noteBookFileUtil = new FileUtil("/notebook", "/notebook.lan");
    private FileUtil postFileUtil = new FileUtil("/note", "/post.lan");
    private FileUtil deleteFileUtil = new FileUtil("/note", "/delete.lan");
    private FileUtil notesFileUtil = new FileUtil("/note", "/note.lan");
    private FileUtil updateFileUtil = new FileUtil("/note", "/update.lan");


    private List<NoteEntity> noteListItems = new ArrayList<NoteEntity>();


    private Context context = this;



    private String postJson = "";

    private int activityid = 1;
    private long itemid;
    private boolean isNew = false;
    private boolean isNew_result = false;

    private int bid = 1;

    private SharedPreferences preferences;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x124://返回post数据
                    String result = (String) msg.obj;
                    if (result.substring(0,7).equals("postOne")) {
                        int nid = Integer.valueOf(result.substring(7, result.length()));

                        //给本地笔记设置数据库获取的新nid
                        noteEntity.setNid(nid);
                        noteListItems.remove((int)itemid);
                        noteListItems.add((int)itemid, noteEntity);
                        String notesJson = gson.toJson(noteListItems, noteListType);
                        notesFileUtil.write(notesJson);


                    }
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();

                    break;
                case 0x456:

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_note_show);

        //初始化控件
        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        select_notebook = (Spinner) findViewById(R.id.select_notebook_sp);
        select_notebook.setOnItemSelectedListener(this);

        String noteBook = noteBookFileUtil.read();
        noteBookListItems = gson.fromJson(noteBook, noteBookListType);
        String[] notebookName = new String[noteBookListItems.size()];
        for (int i = 0; i < noteBookListItems.size(); i++) {
                notebookName[i] = noteBookListItems.get(i).getName();
        }
        ArrayAdapter<String> notebookAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, notebookName);
        select_notebook.setAdapter(notebookAdapter);

        back = (ImageView) findViewById(R.id.back);
        ok = (ImageView) findViewById(R.id.ok);
        delete = (ImageView) findViewById(R.id.delete);

        MyOnClickListener myOnClickListener = new MyOnClickListener();
        back.setOnClickListener(myOnClickListener);
        ok.setOnClickListener(myOnClickListener);
        delete.setOnClickListener(myOnClickListener);


        Intent intent = getIntent();
        String neJson = intent.getStringExtra("neJson");
        activityid = intent.getIntExtra("activityid", 1);



        /***如果是新添加笔记，id从-1开始递减***/
        /***如果是已存在，直接打开***/
        if(isNew = intent.getBooleanExtra("isNew", false)) {
            String result = "";
            int id;
            if((result = postFileUtil.read()).equals(""))  {
                id = -1;
            }
            else {
                Log.i(TAG, result);
                String num = result.substring(result.length()-3, result.length()-1);
                Log.i(TAG, num);
                id = Integer.valueOf(num);
                id--;
            }

            noteEntity = new NoteEntity(id,1,1,"", "",false, null);
            isNew_result = true;


            /***[6.15]原本每一个note都新建一个文件保存信息，有利于同步***/
//            fileUtil = new FileUtil("/note","/" + noteEntity.getNid() + ".lan");


        } else {
            itemid = intent.getLongExtra("itemid", -1);
            noteEntity = gson.fromJson(neJson, NoteEntity.class);
            oldEntity = noteEntity;

            title.setText(noteEntity.getTitle());
            content.setText(noteEntity.getContent());
            int index = 0;
            for(int i = 0; i < noteBookListItems.size();i++) {
                if (noteBookListItems.get(i).getBid() == noteEntity.getBid()) {
                    index = i;
                }
            }
            select_notebook.setSelection(index);

            /***[6.15]原本每一个note都新建一个文件保存信息，有利于同步***/
//            fileUtil = new FileUtil("/note","/" + noteEntity.getNid() + ".lan");
//            fileUtil.write(neJson);
        }



        /***初始化noteListItems***/
        String notelistJson = notesFileUtil.read();
        noteListItems = gson.fromJson(notelistJson, noteListType);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        bid = noteBookListItems.get(position).getBid();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    String newjson = gson.toJson(noteEntity);
                    Intent intent = null;
                    if (activityid == 1) {
                        intent = new Intent(context, MainActivity.class);
                    }
                    else if (activityid == 2 ){
                        intent = new Intent(context, NoteBookActivity.class);
                    }
                    intent.putExtra("isNew", isNew_result);
                    intent.putExtra("itemid", itemid);
                    intent.putExtra("newjson", newjson);
                    setResult(100,intent);
                    finish();
                    break;
                case R.id.ok:
                    noteEntity.setTitle(title.getText() + "");
                    noteEntity.setContent(content.getText() + "");
                    noteEntity.setBid(bid);
                    postJson = gson.toJson(noteEntity);

                    /***[6.15]原本每一个note都新建一个文件保存信息，用于同步***/
                    //fileUtil.write(postJson, false);

                    if (noteEntity.getNid() < 0) {

                        if(isNew) {

                            //修改noteListItems
                            noteListItems.add(noteEntity);
                            String notesJson = gson.toJson(noteListItems, noteListType);
                            itemid = noteListItems.size() - 1;
                            notesFileUtil.write(notesJson);
                            isNew = false;


                            if (IsNet.isConnect(context)) {
                                ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTE_POSTONE, postJson));
                            } else {
                                postFileUtil.write(noteEntity.getNid() + "#", true);
                            }
                        }
                        else {
                            //修改noteListItems
                            /***按item进行修改只是适用于newsetNoteFragment***/
//                            noteListItems.remove((int)itemid);
//                            noteListItems.add((int)itemid, noteEntity);

                            for(int i = 0; i < noteListItems.size();i++) {

                                if (noteListItems.get(i).getNid() == noteEntity.getNid()) {
                                    noteListItems.remove(i);
                                    noteListItems.add(i, noteEntity);
                                }

                            }


                            String notesJson = gson.toJson(noteListItems, noteListType);
                            notesFileUtil.write(notesJson);

                            if (IsNet.isConnect(context)) {
                                ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTE_POSTONE, postJson));

                            } else {
                                postFileUtil.write(noteEntity.getNid() + "#", true);
                            }
                        }

                    } else {

                        //修改noteListItems
                        //noteListItems.remove(oldEntity);
                        //noteListItems.add(noteEntity);

                        for(int i = 0; i < noteListItems.size();i++) {

                            if (noteListItems.get(i).getNid() == noteEntity.getNid()) {
                                noteListItems.remove(i);
                                noteListItems.add(i, noteEntity);
                            }

                        }


                        String notesJson = gson.toJson(noteListItems, noteListType);
                        notesFileUtil.write(notesJson);


                        if (IsNet.isConnect(context)) {
                            Toast.makeText(context, "update", Toast.LENGTH_LONG).show();
                            ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTE_UPDATEONE, postJson));
                        }
                        else {
                            Toast.makeText(context, "writeupdate", Toast.LENGTH_LONG).show();
                            updateFileUtil.write(noteEntity.getNid() + "#", true);
                        }

                    }

                    break;
                case R.id.delete:
                    postJson = gson.toJson(noteEntity);


                    //修改noteListItems
//                    noteListItems.remove((int)itemid);

                    for(int i = 0; i < noteListItems.size();i++) {

                        if (noteListItems.get(i).getNid() == noteEntity.getNid()) {
                            noteListItems.remove(i);
                        }

                    }

                    String notesJson = gson.toJson(noteListItems, noteListType);
                    notesFileUtil.write(notesJson);


                    if (IsNet.isConnect(context)) {
                        ThreadPoolUtils.execute(new HttpPostThread(handler, NoteUrl.NOTE_DELETEONE, postJson));
                        FileUtil.delete("/note/" + noteEntity.getNid() + ".lan");
                    } else {
                        deleteFileUtil.write(noteEntity.getNid() + "#", true);

                        //检查未联网情况下增加和修改的笔记并给予删除防止进行同步
                        String postString = postFileUtil.read();
                        String patternString = noteEntity.getNid() + "#";
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

                        /***[6.15]原本每一个note都新建一个文件保存信息，有利于同步***/
//                        FileUtil.delete("/note/" + noteEntity.getNid() + ".lan");

                    }

                    Intent deleteIntent = null;
                    if (activityid == 1) {
                        deleteIntent = new Intent(context, MainActivity.class);
                    }
                    else if (activityid == 2 ){
                        deleteIntent = new Intent(context, NoteBookActivity.class);
                    }

                    /***[6.15]原本是将每一个修改返回给上一个界面***/
//                    deleteIntent.putExtra("isNew", isNew_result);
//                    deleteIntent.putExtra("isDelete", true);
//                    deleteIntent.putExtra("itemid", itemid);
//                    deleteIntent.putExtra("newjson", postJson);
                    setResult(100,deleteIntent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
}