package com.lanbitou.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    private static final String TAG = "NoteShowActivity";
    private EditText title;
    private EditText content;
    private ImageView back;
    private ImageView ok;
    private ImageView delete;

    private Gson gson = new Gson();
    private NoteEntity noteEntity;
    private FileUtil postFileUtil = new FileUtil("/note", "/post.lan");
    private FileUtil deleteFileUtil = new FileUtil("/note", "/delete.lan");
    private FileUtil fileUtil;
    private FileUtil updateFileUtil = new FileUtil("/note", "/update.lan");

    private Context context = this;

    private static String UPDATEONE = "http://192.168.1.105:8082/lanbitou/note/updateOne";
    private String POSTONE = "http://192.168.1.105:8082/lanbitou/note/postOne";
    private String DELETEONE = "http://192.168.1.105:8082/lanbitou/note/deleteOne";

    private String postJson = "";

    private long itemid;
    private boolean isNew = false;
    private boolean isNew_result = false;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0x124://返回post数据
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
        delete = (ImageView) findViewById(R.id.delete);

        MyOnClickListener myOnClickListener = new MyOnClickListener();
        back.setOnClickListener(myOnClickListener);
        ok.setOnClickListener(myOnClickListener);
        delete.setOnClickListener(myOnClickListener);

        Intent intent = getIntent();
        String neJson = intent.getStringExtra("neJson");

        if(isNew = intent.getBooleanExtra("isNew", false)) {
            String result = "";
            int id;
            if((result = postFileUtil.read()).equals(""))  {
                id = -1;
            }
            else {
                Log.i(TAG, result);
                id = Integer.valueOf(result);
                id--;
            }

            noteEntity = new NoteEntity(id,1,1,"", "",false, null);
            isNew_result = true;

            fileUtil = new FileUtil("/note","/" + noteEntity.getNid() + ".lan");


        } else {
            itemid = intent.getLongExtra("itemid", -1);
            noteEntity = gson.fromJson(neJson, NoteEntity.class);

            title.setText(noteEntity.getTitle());
            content.setText(noteEntity.getContent());

            fileUtil = new FileUtil("/note","/" + noteEntity.getNid() + ".lan");
            fileUtil.write(neJson);
        }



    }


    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.back:
                    String newjson = gson.toJson(noteEntity);
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("isNew", isNew_result);
                    intent.putExtra("itemid", itemid);
                    intent.putExtra("newjson", newjson);
                    setResult(100,intent);
                    finish();
                    break;
                case R.id.ok:
                    noteEntity.setTitle(title.getText() + "");
                    noteEntity.setContent(content.getText() + "");
                    postJson = gson.toJson(noteEntity);
                    fileUtil.write(postJson, false);
                    if (IsNet.isConnect(context)) {

                        if (isNew) {
                            Toast.makeText(context, "post", Toast.LENGTH_LONG).show();
                            ThreadPoolUtils.execute(new HttpPostThread(handler, POSTONE, postJson));
                            isNew = false;
                        } else {
                            Toast.makeText(context, "update", Toast.LENGTH_LONG).show();
                            ThreadPoolUtils.execute(new HttpPostThread(handler, UPDATEONE, postJson));
                        }
                    }
                    else {
                        if (isNew) {
                            postFileUtil.write(noteEntity.getNid() + "");
                        } else {
                            Toast.makeText(context, "writeupdate", Toast.LENGTH_LONG).show();
                            updateFileUtil.write(noteEntity.getNid() + "#", true);
                        }

                    }
                    break;
                case R.id.delete:
                    postJson = gson.toJson(noteEntity);

                    if (IsNet.isConnect(context)) {
                        ThreadPoolUtils.execute(new HttpPostThread(handler, DELETEONE, postJson));
                        FileUtil.delete("/note/" + noteEntity.getNid() + ".lan");
                    } else {
                        deleteFileUtil.write(noteEntity.getNid() + "#", true);
                    }

                    Intent deleteIntent = new Intent(context, MainActivity.class);
                    deleteIntent.putExtra("isNew", isNew_result);
                    deleteIntent.putExtra("isDelete", true);
                    deleteIntent.putExtra("itemid", itemid);
                    deleteIntent.putExtra("newjson", postJson);
                    setResult(100,deleteIntent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
}