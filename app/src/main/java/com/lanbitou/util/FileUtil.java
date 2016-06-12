package com.lanbitou.util;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by joyce on 16-5-18.
 */
public class FileUtil {

    private File path = null;
    private File folderPath = null;
    private static String root = "/mnt/sdcard/lanbitou";

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

            this.folderPath = new File(wholePath);
            //检查路径是否存在
            if(!folderPath.exists()){
                folderPath.mkdirs();
            }
            //文件名补位空
            if(fileName != null){
                //得到文件.
                path = new File(folderPath,fileName);
                try {
                    //检查文件是否存在
                    if(!path.exists()){
                        path.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

           // Log.i("lanbitou","文件完整路径" + path);
        }
    }

    public FileUtil(String filePath){
        this( filePath, null );
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

    /**
     * 静态read，用在要同时读多个文件的场景
     * @param mypath /lanbitou下面的文件路径
     * @return 读出的数据
     */
    public static String read(String mypath) {
        String result = "";
        File realpath = new File(root + mypath);
        if (realpath.exists()) {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(realpath));
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

    /**
     * 根据名字读取,此时FileUtil的第二个构造参数需为null
     * @param fileName
     * @return
     */
    public String readByFileName(String fileName){
        String result = "";
        //文件名补位空
        File newPath = null;
        if(fileName != null){
            //得到文件.
            newPath = new File(folderPath,fileName);
            try {
                //检查文件是否存在
                if(!newPath.exists()){
                    newPath.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (newPath != null) {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(newPath));
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

    /**
     * 向json List文件中追加新的item
     * @param jsonStr
     */
    public void appendToJsonListTail(String jsonStr) {
        RandomAccessFile ra = null;
        try {
            ra = new RandomAccessFile(path,"rw");
            long fileLength = ra.length();
            StringBuilder sb = new StringBuilder(jsonStr);
            if(fileLength == 0){            //开始时文件内容为空
                sb.insert(0,'[');
                sb.append(']');
                jsonStr = sb.toString();
            }else{                          //不为空,在前面添加,分割符
                sb.insert(0,',');
                sb.append(']');
                jsonStr = sb.toString();
                ra.seek(fileLength-1);
            }
            ra.write(jsonStr.getBytes());
            ra.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                ra.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    /**
     *
     * @param data 要写入的数据
     * @param append    是否追加
     */
    public void write(String data, boolean append) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path, append);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.flush();       //清空缓存区
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取某一目录下的所有文件的名字
     * @return 存有文件的List
     */
    public List<String> getInterFileName(){
        List<String> nameList = new ArrayList<>();
        String[] names = folderPath.list();
        for(String n : names){
            if(!n.equals(".tallyLastOperate")){             //排除临时文件夹
                nameList.add(n);
            }
        }
        return nameList;
    }

    public static void delete(String path) {
        File file = new File(root + path);
        file.deleteOnExit();
    }

    public void ldelete(String fileName){
        File file = new File(folderPath,fileName);
        file.delete();
    }

    /**
     * 文件更名
     * @param newName
     * @return 返回更名消息 null说明更名成功
     */
    public synchronized String rename(String oldName,String newName){
        if(!oldName.equals(newName)){            //新的文件名和以前文件名不同时,才有必要进行重命名

            File oldFile = new File(folderPath+"/"+oldName);
            File newFile = new File(folderPath+"/"+newName);

            if(!oldFile.exists()){                //重命名文件不存在
                return "要重命名的账单不存在";
            } else if (newFile.exists()) {        //若在该目录下已经有一个文件和新文件名相同，则不允许重命名
                return "已经存在同名账单";
            } else {
                oldFile.renameTo(newFile);
                oldFile.delete();
                return null;
            }
        } else {
            return "账单名字未做改变";
        }
    }


    /**
     * 判断文件是否存在
     * @param fileName
     * @return
     */
    public boolean isExists(String fileName){
        File file = new File(folderPath + "/" +fileName);
        return file.exists();
    }

    /**
     * 清空文件中的数据
     */
    public void emptyFileContent(){
        this.write("");
    }



}
