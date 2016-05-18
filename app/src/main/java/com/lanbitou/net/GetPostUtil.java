package com.lanbitou.net;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by joyce on 16-5-14.
 */
public class GetPostUtil {

    public static String doGet(String url) {

        BufferedInputStream inputStream;
        String result = "";

        try {
            URL realUrl = new URL(url);
            URLConnection urlConnection = realUrl.openConnection();

            urlConnection.setRequestProperty("accept", "*/*");
            urlConnection.setRequestProperty("connection", "Keep-Alive");
            urlConnection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

            urlConnection.connect();

            inputStream = new BufferedInputStream(urlConnection.getInputStream());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1)
            {
                result += new String(buffer, 0, bytesRead);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String doPost(String url, String params) {

        BufferedInputStream inputStream;
        String result = "";
        PrintWriter out = null;

        try {
            URL realUrl = new URL(url);
            URLConnection urlConnection = realUrl.openConnection();

            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("accept", "*/*");
            urlConnection.setRequestProperty("connection", "Keep-Alive");
            urlConnection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            out = new PrintWriter(urlConnection.getOutputStream());
            out.print(params);
            out.flush();

            inputStream = new BufferedInputStream(urlConnection.getInputStream());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while((bytesRead = inputStream.read(buffer)) != -1)
            {
                result += new String(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;

    }


}