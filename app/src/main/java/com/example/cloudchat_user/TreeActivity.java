package com.example.cloudchat_user;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class TreeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tree);

        RadioGroup genderGroup = findViewById(R.id.rg_gender);
        Spinner gradeSpinner = findViewById(R.id.sp_grade);
        EditText hobbyInput = findViewById(R.id.et_hobby);
        EditText chatInput = findViewById(R.id.et_chat_input);
        Button submitButton = findViewById(R.id.btn_submit_tree);

        // 提交按钮点击事件
        submitButton.setOnClickListener(v -> {
            // 获取性别
            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            String gender = selectedGenderId != -1
                    ? ((RadioButton) findViewById(selectedGenderId)).getText().toString()
                    : "未选择";

            // 获取年级
            String grade = gradeSpinner.getSelectedItem().toString();

            // 获取兴趣爱好
            String hobby = hobbyInput.getText().toString().trim();

            // 获取聊天内容
            String chatMessage = chatInput.getText().toString().trim();

            // 显示提交内容
            Toast.makeText(TreeActivity.this,
                    "性别: " + gender + "\n年级: " + grade + "\n兴趣爱好: " + hobby + "\n聊天内容: " + chatMessage,
                    Toast.LENGTH_LONG).show();
        });
    }
}

