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
    private String POSTONE = "http://192.168.1.108:8082/lanbitou/note/postOne";
    private String GETONE = "http://192.168.1.108:8082/lanbitou/note/getOne";
    private String GETALL = "http://192.168.1.108:8082/lanbitou/note/getAll";
    private String UPDATEALL = "http://192.168.1.108:8082/lanbitou/note/updateAll";
    private String DELETEALL = "http://192.168.1.108:8082/lanbitou/note/deleteAll";
    private String POSTALL = "http://192.168.1.108:8082/lanbitou/note/postAll";

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
                checkNoteCache();
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

            ThreadPoolUtils.execute(new HttpGetThread(handler, GETALL));

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



    private void checkNoteCache() {

        //检查是否有断网时未同步的文件


        String notelistJson = notesFileUtil.read();
        List<NoteEntity> noteCacheListItems = new ArrayList<>();
        List<NoteEntity> newListItems = gson.fromJson(notelistJson, noteListType);
        for(NoteEntity ne : newListItems) {
            noteCacheListItems.add(ne);
            Log.i("tag",ne.toString());
        }

        //检查修改的文件
        String update = "";
        if (!(update = updateFileUtil.read()).equals("")) {

            Log.i("TAG",update);

            String[] updateid = update.split("#");
            int[] id = new int[updateid.length];
            for(int i = 0;i < updateid.length;i++) {
                id[i] = Integer.valueOf(updateid[i]);
            }

            int[] finalid = ArrayUtil.removeSame(id);
            List<NoteEntity> updateNoteEntity = new ArrayList<NoteEntity>();
            Toast.makeText(getActivity(), finalid.length + "", Toast.LENGTH_SHORT).show();

            for(int i : finalid) {
                for (NoteEntity noteEntity : noteCacheListItems) {
                    if (noteEntity.getNid() == i) {
                        updateNoteEntity.add(noteEntity);
                    }
                }
            }


            String param = gson.toJson(updateNoteEntity, noteListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(handler, UPDATEALL, param));
        }


        //检查删除的文件
        String delete = "";
        if (!(delete = deleteFileUtil.read()).equals("")) {

            Log.i("TAG",delete);

            String[] deleteid = delete.split("#");
            int[] id = new int[deleteid.length];
            for(int i = 0;i < deleteid.length;i++) {
                id[i] = Integer.valueOf(deleteid[i]);
            }

            int[] finalid = ArrayUtil.removeSame(id);
            List<NoteEntity> deleteNoteEntity = new ArrayList<NoteEntity>();
            Toast.makeText(getActivity(), finalid.length + "", Toast.LENGTH_SHORT).show();

            for(int i : finalid) {

                deleteNoteEntity.add(new NoteEntity(i));

            }

            String param = gson.toJson(deleteNoteEntity, noteListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(handler, DELETEALL, param));
        }


        //检查添加的文件
        String post = "";
        if (!(post = postFileUtil.read()).equals("")) {

            Log.i("TAG",post);


            String[] postid = post.split("#");
            int[] id = new int[postid.length];
            for(int i = 0;i < postid.length;i++) {
                id[i] = Integer.valueOf(postid[i]);
            }

            int[] finalid = ArrayUtil.removeSame(id);
            List<NoteEntity> postNoteEntity = new ArrayList<NoteEntity>();
            Toast.makeText(getActivity(), finalid.length + "", Toast.LENGTH_SHORT).show();

            for(int i : finalid) {
                for (NoteEntity noteEntity : noteCacheListItems) {
                    if (noteEntity.getNid() == i) {
                        postNoteEntity.add(noteEntity);
                        Log.i(TAG, noteEntity.getTitle());
                    }
                }
            }

            String param = gson.toJson(postNoteEntity, noteListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(handler, POSTALL, param));
        }

    }
}