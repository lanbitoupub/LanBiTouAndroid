package com.lanbitou.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lanbitou.entities.NoteBookEntity;
import com.lanbitou.entities.NoteEntity;
import com.lanbitou.net.NoteUrl;
import com.lanbitou.thread.HttpPostThread;
import com.lanbitou.thread.ThreadPoolUtils;
import com.lanbitou.util.ArrayUtil;
import com.lanbitou.util.FileUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class SyncService extends Service {

    private static final String TAG = "SyncService";
    private Gson gson = new Gson();

    /***笔记本的***/
    private static Type noteBookListType = new TypeToken<List<NoteBookEntity>>() {}.getType();
    private static FileUtil noteBookFileUtil = new FileUtil("/notebook", "/notebook.lan");
    private static FileUtil noteBookupdateFileUtil = new FileUtil("/notebook", "/update.lan");
    private static FileUtil noteBookpostFileUtil = new FileUtil("/notebook", "/post.lan");
    private static FileUtil noteBookdeleteFileUtil = new FileUtil("/notebook", "/delete.lan");



    private List<NoteBookEntity> noteBookEntityList = new ArrayList<>();

    /**笔记的****/
    private Type noteListType = new TypeToken<List<NoteEntity>>() {}.getType();
    private FileUtil notesFileUtil = new FileUtil("/note", "/note.lan");
    private FileUtil updateFileUtil = new FileUtil("/note", "/update.lan");
    private FileUtil postFileUtil = new FileUtil("/note", "/post.lan");
    private FileUtil deleteFileUtil = new FileUtil("/note", "/delete.lan");


    private List<NoteEntity> noteEntityList = new ArrayList<>();



    private boolean flag = true;


    Handler noteSynchandler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 0x124) {
                String result = (String) msg.obj;
                if (result.equals("postAll")) {
                    postFileUtil.write("");

                } else if (result.equals("updateAll")) {
                    updateFileUtil.write("");

                } else if (result.equals("deleteAll")){
                    deleteFileUtil.write("");

                }
            }

        }
    };

    Handler noteBookSynchandler = new Handler() {
        public void handleMessage(Message msg) {

            if (msg.what == 0x124) {
                String result = (String) msg.obj;
                if (result.substring(0,7).equals("postAll")) {

                    int newestbid;
                    int newestfid;

                    String noteBookStr = result.substring(8, result.length());
                    Log.i(TAG, noteBookStr);
                    String[] noteBookArr = noteBookStr.split("@");
                    for (int i = 0; i < noteBookArr.length; i++) {
                        Log.i(TAG, noteBookArr[i]);
                        String[] newestid = noteBookArr[i].split("#");
                        newestbid = Integer.valueOf(newestid[0]);
                        newestfid = Integer.valueOf(newestid[1]);

                        /***把本地笔记本的bid改成最新的，把网络笔记本和本地笔记本的的fid都改成0***/
                        String noteBooklistJson = "";
                        List<NoteBookEntity> localNoteBookListItems = new ArrayList<>();

                        if (!(noteBooklistJson = noteBookFileUtil.read()).equals("")) {
                            List<NoteBookEntity> newListItems = gson.fromJson(noteBooklistJson, noteBookListType);
                            for(NoteBookEntity ne : newListItems) {
                                localNoteBookListItems.add(ne);
                                Log.i("tag",ne.toString());
                            }
                        }
                        for(int j = 0; j < localNoteBookListItems.size(); j++) {
                            Log.i(TAG, "J" + localNoteBookListItems.get(j).getBid());
                             if (localNoteBookListItems.get(j).getFid() == newestfid)
                             {
                                 localNoteBookListItems.get(j).setBid(newestbid);
                                 localNoteBookListItems.get(j).setFid(0);
                                 Log.i(TAG, localNoteBookListItems.get(j).getBid() +
                                            ":::::::" + localNoteBookListItems.get(j).getBid());

                                 String postJson = gson.toJson(localNoteBookListItems.get(j));
                                 ThreadPoolUtils.execute(new HttpPostThread(noteBookSynchandler, NoteUrl.NOTEBOOK_UPDATEONE, postJson));
                             }
                        }
                        String noteBookJson = gson.toJson(localNoteBookListItems, noteBookListType);
                        noteBookFileUtil.write(noteBookJson);


                        /***为未同步文件夹下的文件修改新的bid***/
                        List<NoteEntity> localNoteListItems = new ArrayList<>();
                        String notelistJson = "";
                        if (!(notelistJson = notesFileUtil.read()).equals("")) {
                            List<NoteEntity> newListItems = gson.fromJson(notelistJson, noteListType);
                            for(NoteEntity ne : newListItems) {
                                localNoteListItems.add(ne);
                                Log.i(TAG,ne.toString());
                            }
                        }
                        for(int j = 0; j < localNoteListItems.size(); j++) {
                             if (localNoteListItems.get(j).getBid() == newestfid) {
                                    localNoteListItems.get(j).setBid(newestbid);
                             }
                        }
                        String notesJson = gson.toJson(localNoteListItems, noteListType);
                        notesFileUtil.write(notesJson);

                    }

                    flag = true;
                    checkNoteCache();

                    noteBookpostFileUtil.write("");

                } else if (result.equals("updateAll")) {
                    noteBookupdateFileUtil.write("");

                } else if (result.equals("deleteAll")){
                    noteBookdeleteFileUtil.write("");

                }

            }

        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "HELLO");
        checkNoteBookCache();
        if (flag) {
            checkNoteCache();
        }


        return START_STICKY;
    }

    private void checkNoteCache() {

        //检查是否有断网时未同步的文件

        List<NoteEntity> noteCacheListItems = new ArrayList<>();
        String notelistJson = "";
        if (!(notelistJson = notesFileUtil.read()).equals("")) {
            List<NoteEntity> newListItems = gson.fromJson(notelistJson, noteListType);
            for(NoteEntity ne : newListItems) {
                noteCacheListItems.add(ne);
                Log.i(TAG,ne.toString());
            }
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

            for(int i : finalid) {
                for (NoteEntity noteEntity : noteCacheListItems) {
                    if (noteEntity.getNid() == i) {
                        updateNoteEntity.add(noteEntity);
                    }
                }
            }


            String param = gson.toJson(updateNoteEntity, noteListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(noteSynchandler, NoteUrl.NOTE_UPDATEALL, param));
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

            for(int i : finalid) {

                deleteNoteEntity.add(new NoteEntity(i));

            }

            String param = gson.toJson(deleteNoteEntity, noteListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(noteSynchandler, NoteUrl.NOTE_DELETEALL, param));
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
            ThreadPoolUtils.execute(new HttpPostThread(noteSynchandler, NoteUrl.NOTE_POSTALL, param));
        }

    }

    public void checkNoteBookCache() {

        //检查是否有断网时未同步的文件

        String notelistJson = "";
        List<NoteBookEntity> noteCacheListItems = new ArrayList<>();

        if (!(notelistJson = noteBookFileUtil.read()).equals("")) {
            List<NoteBookEntity> newListItems = gson.fromJson(notelistJson, noteBookListType);
            for(NoteBookEntity ne : newListItems) {
                noteCacheListItems.add(ne);
                Log.i("tag",ne.toString());
            }
        }


        //检查修改的文件
        String update = "";
        if (!(update = noteBookupdateFileUtil.read()).equals("")) {

            Log.i("TAG",update);

            String[] updateid = update.split("#");
            int[] id = new int[updateid.length];
            for(int i = 0;i < updateid.length;i++) {
                id[i] = Integer.valueOf(updateid[i]);
            }

            int[] finalid = ArrayUtil.removeSame(id);
            List<NoteBookEntity> updateNoteBookEntity = new ArrayList<NoteBookEntity>();

            for(int i : finalid) {
                for (NoteBookEntity noteBookEntity : noteCacheListItems) {
                    if (noteBookEntity.getBid() == i) {
                        updateNoteBookEntity.add(noteBookEntity);
                    }

                }

            }

            String param = gson.toJson(updateNoteBookEntity, noteBookListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(noteBookSynchandler, NoteUrl.NOTEBOOK_UPDATEALL, param));
        }


        //检查删除的文件
        String delete = "";
        if (!(delete = noteBookdeleteFileUtil.read()).equals("")) {

            Log.i("TAG",delete);

            String[] postid = delete.split("#");
            int[] id = new int[postid.length];
            for(int i = 0;i < postid.length;i++) {
                id[i] = Integer.valueOf(postid[i]);
            }

            int[] finalid = ArrayUtil.removeSame(id);
            List<NoteBookEntity> deleteNoteBookEntity = new ArrayList<NoteBookEntity>();

            for(int i : finalid) {
                deleteNoteBookEntity.add(new NoteBookEntity(i));
            }

            String param = gson.toJson(deleteNoteBookEntity, noteBookListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(noteBookSynchandler, NoteUrl.NOTEBOOK_DELETEALL, param));
        }


        //检查添加的文件
        String post = "";
        if (!(post = noteBookpostFileUtil.read()).equals("")) {

            Log.i("TAG",post);


            String[] postid = post.split("#");
            int[] id = new int[postid.length];
            for(int i = 0;i < postid.length;i++) {
                id[i] = Integer.valueOf(postid[i]);
            }

            int[] finalid = ArrayUtil.removeSame(id);
            List<NoteBookEntity> postNoteBookEntity = new ArrayList<NoteBookEntity>();

            for(int i : finalid) {
                for (NoteBookEntity noteBookEntity : noteCacheListItems) {
                    if (noteBookEntity.getBid() == i) {
                        postNoteBookEntity.add(noteBookEntity);
                    }
                }
            }

            String param = gson.toJson(postNoteBookEntity, noteBookListType);

            //发送更新
            ThreadPoolUtils.execute(new HttpPostThread(noteBookSynchandler, NoteUrl.NOTEBOOK_POSTALL, param));
            flag = false;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}