package com.lanbitou.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.R;
import com.lanbitou.adapters.NoteAdapter;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.thread.HttpGetThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Henvealf on 16-5-13.
 */
public class NewestNotesFragment extends Fragment{

    private String POSTONE = "http://192.168.1.105:8080/lanbitou/note/postOne";
    private String GETONE = "http://192.168.1.105:8080/lanbitou/note/getOne";
    private String GETALL = "http://192.168.1.105:8080/lanbitou/note/getAll";

    private TextView textView;
    private ListView listView;
    private NoteAdapter noteAdapter;
    private List<NoteEntity> listItems = new ArrayList<NoteEntity>();
    private Gson gson = new Gson();
    private Type listType = new TypeToken<List<NoteEntity>>() {}.getType();
    private FileUtil fileUtil;

    Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 0x123:
                    List<NoteEntity> newListItems = gson.fromJson((String) msg.obj, listType);
                    for(NoteEntity ne : newListItems) {
                        listItems.add(ne);
                        Log.i("tag",ne.toString());
                    }
                    noteAdapter.notifyDataSetChanged();

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
        noteAdapter = new NoteAdapter(this.getActivity(), listItems);
        listView.setAdapter(noteAdapter);

        fileUtil = new FileUtil("/note/note.tou");
        fileUtil.write("hello");
        String s = fileUtil.read();
        textView.setText(s);


        ThreadPoolUtils.execute(new HttpGetThread(handler, GETALL));
        Log.i("","");

        return view;
    }
}
