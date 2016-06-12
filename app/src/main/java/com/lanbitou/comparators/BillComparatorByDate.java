package com.lanbitou.comparators;

import com.lanbitou.entities.Bill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * 根据时间
 * Created by Henvealf on 16-6-8.
 */
public class BillComparatorByDate implements Comparator<Bill>{
    @Override
    public int compare(Bill b1, Bill b2) {

        String s1 = b1.getBillDate();
        String s2 = b2.getBillDate();

        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = simpleDateFormat.parse(s1);
            Date d2 = simpleDateFormat.parse(s2);

            if(d1.after(d2)){
                return -1;
            }else if(d1.before(d2)){
                return 1;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
