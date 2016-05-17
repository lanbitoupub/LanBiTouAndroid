package com.lanbitou.activities;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import com.lanbitou.R;
import com.lanbitou.entities.Bill;

/**
 * 添加
 * Created by Henvealf on 16-5-14.
 */
public class AddBillActivity extends Activity {

    ImageButton backImgBtn;
    EditText typeEt;        //消费类型
    EditText moneyEt;       //多少钱
    RadioGroup inOutRg;         //收入还是支出
    DatePicker datePicker;      //日期选择器
    Button addBtn;              //添加按钮

    Bill bill;

    boolean isIn = true;        //是不是收入

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        backImgBtn = (ImageButton) findViewById(R.id.add_bill_back_btn);
        backImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddBillActivity.this.finish();
            }
        });

        typeEt = (EditText) findViewById(R.id.add_bill_type_et);
        moneyEt = (EditText) findViewById(R.id.add_bill_money_et);

        inOutRg = (RadioGroup) findViewById(R.id.add_bill_inout_rg);
        findViewById(R.id.add_bill_rb_in);
        findViewById(R.id.add_bill_rb_out);

        //inOutRg.setOnCheckedChangeListener();

        datePicker = (DatePicker) findViewById(R.id.add_bill_date);

        addBtn = (Button) findViewById(R.id.add_bill_btn);

        bill = new Bill();
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

            String type = typeEt.getText().toString();
            double money = Double.parseDouble(moneyEt.getText().toString());

            bill.setUid(1);
            bill.setFolder("日常");
            //bill.setType();
        }
    }


}
