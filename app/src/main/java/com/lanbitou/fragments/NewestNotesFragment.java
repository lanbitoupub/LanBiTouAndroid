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
import com.lanbitou.thread.ThreadPoolUtils;
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

    private String POSTONE = "http://192.168.1.105:8082/lanbitou/note/postOne";
    private String GETONE = "http://192.168.1.105:8082/lanbitou/note/getOne";
    private String GETALL = "http://192.168.1.105:8082/lanbitou/note/getAll";
    private String UPDATEALL = "http://192.168.1.105:8082/lanbitou/note/updateAll";

    private TextView textView;
    private ListView listView;
    private NoteAdapter noteAdapter;
    private List<NoteEntity> listItems = new ArrayList<NoteEntity>();
    private Gson gson = new Gson();
    private Type listType = new TypeToken<List<NoteEntity>>() {}.getType();
    private FileUtil fileUtil;
    private FileUtil updateFileUtil;
    private List<NoteEntity> updateNoteEntity = new ArrayList<NoteEntity>();
    private NoteEntity oneEntity;


    Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0x123://返回网络数据

                    String json = (String) msg.obj;
                    if(json != null && !json.equals("")) {
                        if (msg.arg1 == 1) {
                            listItems.clear();
                            fileUtil.write(json, false);
                        }

                        List<NoteEntity> newListItems = gson.fromJson(json, listType);
                        for(NoteEntity ne : newListItems) {
                            listItems.add(ne);
                            Log.i("tag",ne.toString());
                        }
                        noteAdapter.notifyDataSetChanged();
                    }
                    break;
                case 0x456:
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_LONG).show();
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

        listItems.clear();
        noteAdapter = new NoteAdapter(this.getActivity(), listItems);
        listView.setAdapter(noteAdapter);

        Log.i("","");



        if (IsNet.isConnect(getActivity())) {
            Toast.makeText(getActivity(), "能连上", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getActivity(), "连不上", Toast.LENGTH_LONG).show();
        }





        fileUtil = new FileUtil("/note", "/note.lan");
        updateFileUtil = new FileUtil("/note", "/update.lan");
        String result = "";
        if(!(result = fileUtil.read()).equals("")) {
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

                Intent intent = new Intent(getActivity(), NoteShowActivity.class);
                String neJson= gson.toJson(ne);
                intent.putExtra("neJson",neJson);
                startActivity(intent);
            }
        });


        return view;
    }


    private void refresh() {

        if (IsNet.isConnect(getActivity())) {
            ThreadPoolUtils.execute(new HttpGetThread(handler, GETALL));
        }


        //检查是否有断网时未同步的文件
        String update = "";
        if (!(update = updateFileUtil.read()).equals("")) {

            Log.i("TAG",update);
            Toast.makeText(getActivity(), update, Toast.LENGTH_LONG).show();
            String[] updateid = update.split("#");
            int[] id = new int[updateid.length];
            for(int i = 0;i < updateid.length;i++) {
                id[i] = Integer.valueOf(updateid[i]);
            }

            for(int i = 0; i < id.length; i++) {
                oneEntity = gson.fromJson(FileUtil.read("/note/" +  id[i] + ".lan"), NoteEntity.class);
                updateNoteEntity.add(oneEntity);
            }

            //清空update.lan文件
            updateFileUtil.write("",false);
            //ThreadPoolUtils.execute(new HttpGetThread(handler, GETALL));



        }


    }
}