
package com.example.cloudchat_user.ui.mind_sanctuary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cloudchat_user.R;

public class TreeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tree, container, false);

        RadioGroup rgGender = view.findViewById(R.id.rg_gender);
        Spinner spGrade = view.findViewById(R.id.sp_grade);
        EditText etHobby = view.findViewById(R.id.et_hobby);
        Button btnSubmit = view.findViewById(R.id.btn_submit_tree);

        // 提交按钮点击事件
        btnSubmit.setOnClickListener(v -> {
            int selectedGenderId = rgGender.getCheckedRadioButtonId();
            RadioButton selectedGender = view.findViewById(selectedGenderId);
            String gender = (selectedGender != null) ? selectedGender.getText().toString() : "未选择";

            String grade = spGrade.getSelectedItem().toString();
            String hobby = etHobby.getText().toString().trim();

            if (!hobby.isEmpty()) {
                // 处理配对逻辑
                Toast.makeText(getContext(), "性别: " + gender + ", 年级: " + grade + ", 爱好: " + hobby, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
