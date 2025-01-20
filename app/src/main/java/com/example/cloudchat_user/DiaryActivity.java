
package com.example.cloudchat_user;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DiaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_diary);

        EditText diaryInput = findViewById(R.id.et_diary_input);
        Button submitButton = findViewById(R.id.btn_submit_diary);

        // 提交按钮点击事件
        submitButton.setOnClickListener(v -> {
            String inputText = diaryInput.getText().toString().trim();
            if (!inputText.isEmpty()) {
                Toast.makeText(DiaryActivity.this, "提交成功: " + inputText, Toast.LENGTH_SHORT).show();
                diaryInput.setText(""); // 清空输入框
            } else {
                Toast.makeText(DiaryActivity.this, "请输入内容后提交", Toast.LENGTH_SHORT).show();
            }
        });
    }
}