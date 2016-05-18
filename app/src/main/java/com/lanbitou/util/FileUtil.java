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

    private String filename = "";
    private File path = null;

    public FileUtil(String filename) {
        this.filename = filename;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String pathname = Environment.getExternalStorageDirectory().toString() + "/lanbitou" +filename;
            path = new File(pathname);
        }
    }


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
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
