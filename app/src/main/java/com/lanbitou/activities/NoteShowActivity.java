package com.lanbitou.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lanbitou.R;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.IsNet;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.FileUtil;

public class NoteShowActivity extends AppCompatActivity {

    private EditText title;
    private EditText content;
    private ImageView back;
    private ImageView ok;

    private Gson gson = new Gson();
    private NoteEntity noteEntity;
    private FileUtil fileUtil;
    private FileUtil updateFileUtil;

    private Context context = this;

    private static String UPDATEONE = "http://192.168.1.105:8082/lanbitou/note/updateOne";
    private String postJson = "";

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x123:

                    String result = (String) msg.obj;

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

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        back = (ImageView) findViewById(R.id.back);
        ok = (ImageView) findViewById(R.id.ok);

        MyOnClickListener myOnClickListener = new MyOnClickListener();
        back.setOnClickListener(myOnClickListener);
        ok.setOnClickListener(myOnClickListener);

        Intent intent = getIntent();
        String neJson = intent.getStringExtra("neJson");
        noteEntity = gson.fromJson(neJson, NoteEntity.class);

        title.setText(noteEntity.getTitle());
        content.setText(noteEntity.getContent());

        fileUtil = new FileUtil("/note","/" + noteEntity.getNid() + ".lan");
        updateFileUtil = new FileUtil("/note", "/update.lan");

    }


    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    finish();
                    break;
                case R.id.ok:
                    noteEntity.setTitle(title.getText() + "");
                    noteEntity.setContent(content.getText() + "");
                    postJson = gson.toJson(noteEntity);
                    fileUtil.write(postJson, false);
                    if (IsNet.isConnect(context)) {
                        Toast.makeText(context, "update", Toast.LENGTH_LONG).show();
                        ThreadPoolUtils.execute(new HttpPostThread(handler, UPDATEONE, postJson));
                    }
                    else {
                        Toast.makeText(context, "writeupdate", Toast.LENGTH_LONG).show();
                        updateFileUtil.write(noteEntity.getNid() + "#", true);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}