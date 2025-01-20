package com.example.cloudchat_user.ui.mind_sanctuary;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cloudchat_user.R;

public class DiaryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        EditText etDiaryInput = view.findViewById(R.id.et_diary_input);
        Button btnSubmit = view.findViewById(R.id.btn_submit_diary);

        // 提交按钮点击事件
        btnSubmit.setOnClickListener(v -> {
            String diaryText = etDiaryInput.getText().toString().trim();
            if (!diaryText.isEmpty()) {
                // 处理用户输入的内容
                Toast.makeText(getContext(), "提交成功！内容：" + diaryText, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
