package com.lanbitou.util;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by joyce on 16-5-18.
 */
public class FileUtil {

    private File path = null;

    /**
     * 所要操作文件相对于/mnt/sdcard/lanbitou路径
     *
     * 例如FileUtil("/note", "note.txt") 就对应于 /mnt/sdcard/lanbitou/note/note.txt
     *
     * @param filePath
     * @param fileName
     */
    public FileUtil(String filePath, String fileName) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String wholePath = Environment.getExternalStorageDirectory().toString() + "/lanbitou" +filePath;

            File checkFilePath = new File(wholePath);
            //检查路径是否存在
            if(!checkFilePath.exists()){
                checkFilePath.mkdirs();
            }

            //得到文件.
            path = new File(checkFilePath,fileName);
            try {
                //检查文件是否存在
                if(!path.exists()){
                    path.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
           // Log.i("lanbitou","文件完整路径" + path);
        }
    }

    /**
     * 直接写要要读的文件名.
     * @return
     */
    public String read() {
        String result = "";
        if (path != null) {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(path));
                byte[] bytes = new byte[1024];
                int count;
                while((count = inputStream.read(bytes)) != -1)
                {
                    result += new String(bytes, 0, count);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public void write(String data) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();       //清空缓存区
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
