
package com.example.cloudchat_user.ui.interactive_lecture;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cloudchat_user.R;

public class SelectOptionsDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_options, container, false);

        Button selectGradeButton = view.findViewById(R.id.selectGradeButton);
        Button selectSubjectButton = view.findViewById(R.id.selectSubjectButton);
        Button closeButton = view.findViewById(R.id.closeButton);

        selectGradeButton.setOnClickListener(v -> {
            // 处理选择年级的逻辑
        });

        selectSubjectButton.setOnClickListener(v -> {
            // 处理选择科目的逻辑
        });

        closeButton.setOnClickListener(v -> dismiss()); // 关闭对话框

        return view;
    }

}