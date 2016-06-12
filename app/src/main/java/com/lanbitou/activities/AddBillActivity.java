package com.lanbitou.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.lanbitou.R;
import com.lanbitou.entities.Bill;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 添加
 * Created by Henvealf on 16-5-14.
 */
public class AddBillActivity extends Activity {

    ImageView backImgView;
    EditText typeEt;            //消费类型
    EditText moneyEt;           //多少钱
    RadioGroup inOutRg;         //收入还是支出
    RadioButton inRb, outRb;
    DatePicker datePicker;      //日期选择器
    EditText remarkEt;           //备注
    Button addBtn;              //添加按钮
    TextView folderTv;
    Bill bill;
    String operate = null;     //操作
    boolean isIn = true;        //是不是收入

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        bill = new Bill();
        bill.setFolder(getIntent().getStringExtra("folder"));
        operate = getIntent().getStringExtra("operate");      //获得操作类型

        Log.i("lanbitou", "现在的操作为:" + operate);
        backImgView = (ImageView) findViewById(R.id.back);
        backImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        typeEt = (EditText) findViewById(R.id.add_bill_type_et);
        moneyEt = (EditText) findViewById(R.id.add_bill_money_et);

        inOutRg = (RadioGroup) findViewById(R.id.add_bill_inout_rg);
        inOutRg.setOnCheckedChangeListener(new InOutCheckedChangeListener());

        remarkEt = (EditText) findViewById(R.id.add_bill_remark_et);
        inRb = (RadioButton) findViewById(R.id.add_bill_rb_in);
        outRb = (RadioButton) findViewById(R.id.add_bill_rb_out);

        // 获取当前的年、月、日
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        datePicker = (DatePicker) findViewById(R.id.add_bill_date);
        datePicker.init(year,month,day,new BillDateChangedListener());

        addBtn = (Button) findViewById(R.id.add_bill_btn);
        addBtn.setOnClickListener(new AddBtnClickListener());

        folderTv = (TextView) findViewById(R.id.add_bill_show_folder_tv);

        folderTv.setText(bill.getFolder());

        if(operate.equals("修改")){
            addBtn.setText("确认修改");
            //获得旧的
            Bill oldBill = (Bill) getIntent().getSerializableExtra("bill");
            typeEt.setText(oldBill.getType());
            moneyEt.setText(String.valueOf(oldBill.getMoney()));
            remarkEt.setText(oldBill.getRemark());
            folderTv.setText(oldBill.getFolder());
            bill.setId(oldBill.getId());
            bill.setFolder(oldBill.getFolder());
            bill.setInClouded(oldBill.isInClouded());
            //设置旧的时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = oldBill.getBillDate();
            try {
                Date oldDate = sdf.parse(dateStr);
                c.setTime(oldDate);
                int oldYear = c.get(Calendar.YEAR);
                int oldMonth = c.get(Calendar.MONTH);
                int oldDay = c.get(Calendar.DAY_OF_MONTH);
                datePicker.init(oldYear,oldMonth,oldDay,new BillDateChangedListener());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(oldBill.getMoney() > 0){
                isIn = true;
                inRb.setChecked(true);
            }else if(oldBill.getMoney() < 0){
                isIn = false;
                outRb.setChecked(true);
            }
        }
    }

    private class InOutCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            isIn = true;
            switch (i){
                case R.id.add_bill_rb_in:
                    isIn = true;
                    break;
                case R.id.add_bill_rb_out:
                    isIn = false;
                    break;
            }
        }
    }


    private class AddBtnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            //int uid = getIntent().getIntExtra("uid",0);
            String type = typeEt.getText().toString();
            double money = Math.abs(Double.parseDouble(moneyEt.getText().toString()));
            String remark = remarkEt.getText().toString();
            //bill.setUid(uid);
            bill.setType(type);

            if(!isIn){
                money = -money;
            }
            bill.setMoney(money);
            bill.setRemark(remark);
            bill.setBillDate(getBillDate(datePicker));

            //返回账单数据到主Activity
            Intent toBillFragI = new Intent(AddBillActivity.this,MainActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("newBill",bill);              //传递实现了Serializable接口的对象
            b.putInt("position",getIntent().getIntExtra("position",-1));
            toBillFragI.putExtras(b);
            setResult(RESULT_OK,toBillFragI);
            finish();
        }
    }

    /**
     * 时间选择器的监听器
     */
    private class BillDateChangedListener implements DatePicker.OnDateChangedListener {
        @Override
        public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
        }
    }

    /**
     * 获取当前的时间选择器上的时间.并格式化
     * @param datePicker
     * @return
     */
    private String getBillDate(DatePicker datePicker){
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(cal.getTime());
        Log.i("lanbitou","账单时间: " +dateStr);
        return dateStr;
    }

}
