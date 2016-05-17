package com.lanbitou.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.lanbitou.R;
import com.lanbitou.entities.Bill;

import java.util.Calendar;
import java.util.Date;

/**
 * 添加
 * Created by Henvealf on 16-5-14.
 */
public class AddBillActivity extends Activity {

    ImageButton backImgBtn;
    EditText typeEt;            //消费类型
    EditText moneyEt;           //多少钱
    RadioGroup inOutRg;         //收入还是支出
    DatePicker datePicker;      //日期选择器
    EditText remarkEt;           //备注
    Button addBtn;              //添加按钮

    Bill bill;

    boolean isIn = true;        //是不是收入

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        bill = new Bill();

        backImgBtn = (ImageButton) findViewById(R.id.add_bill_back_btn);
        backImgBtn.setOnClickListener(new View.OnClickListener() {
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
        findViewById(R.id.add_bill_rb_in);
        findViewById(R.id.add_bill_rb_out);

        // 获取当前的年、月、日、小时、分钟
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        datePicker = (DatePicker) findViewById(R.id.add_bill_date);
        datePicker.init(year,month,day,new BillDateChangedListener());

        addBtn = (Button) findViewById(R.id.add_bill_btn);
        addBtn.setOnClickListener(new AddBtnClickListener());


    }

    private class InOutCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
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

            int uid = getIntent().getIntExtra("uid",0);
            String type = typeEt.getText().toString();
            double money = Double.parseDouble(moneyEt.getText().toString());
            String remark = remarkEt.getText().toString();
            String folder = getIntent().getStringExtra("folder");
            bill.setUid(uid);
            bill.setType(type);

            if(!isIn){
                money = -money;
            }
            bill.setMoney(money);
            bill.setFolder(folder);
            bill.setRemark(remark);

            //返回账单数据到主Activity
            Intent toBillFragI = new Intent(AddBillActivity.this,MainActivity.class);
            Bundle b = new Bundle();
            b.putSerializable("newBill",bill);              //传递实现了Serializable接口的对象
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
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            bill.setBillDate(cal.getTime());
        }
    }

}
