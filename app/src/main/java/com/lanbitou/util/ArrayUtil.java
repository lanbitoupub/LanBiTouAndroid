package com.lanbitou.util;

import java.util.Arrays;

/**
 * Created by joyce on 16-5-21.
 */
public class ArrayUtil {


    /**
     * 去除数组中相同的元素
     * @param a 传入的数组
     * @return 返回数组
     */
    public static int[] removeSame(int[] a) {
        int[] b = new int[9];


        b[0] = a[0];
        int count = 1;
        boolean flag = true;


        for (int i = 1; i < a.length;i++) {

            for (int j = 0; j < b.length; j++) {
                if (a[i] == b[j]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                b[count] = a[i];
                count++;
            }
            else {
                flag = true;
            }

        }

        int[] c = Arrays.copyOf(b,count);
        return c;
    }
}
