package com.example.cloudchat_user.ui.interactive_lecture;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cloudchat_user.R;

public class SelectOptionsDialogFragment extends DialogFragment {

    private String selectedGradeLevel;
    private String selectedSubject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_options, container, false);

        Button selectGradeButton = view.findViewById(R.id.selectGradeButton);
        Button selectSubjectButton = view.findViewById(R.id.selectSubjectButton);
        Button closeButton = view.findViewById(R.id.closeButton);

        selectGradeButton.setOnClickListener(v -> showGradeOptions());

        selectSubjectButton.setOnClickListener(v -> {
            if (selectedGradeLevel != null) {
                showSubjectOptions();
            } else {
                new AlertDialog.Builder(getContext())
                        .setMessage("请先选择年级")
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        closeButton.setOnClickListener(v -> dismiss()); // 关闭对话框

        return view;
    }

    private void showGradeOptions() {
        final String[] grades = {"高中", "初中", "小学"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext()); // 使用 requireContext()
        builder.setTitle("选择年级")
                .setItems(grades, (dialog, which) -> {
                    switch (which) {
                        case 0: // 高中
                            selectedGradeLevel = "高中";
                            showSpecificGradeOptions(new String[]{"高一", "高二", "高三"});
                            break;
                        case 1: // 初中
                            selectedGradeLevel = "初中";
                            showSpecificGradeOptions(new String[]{"初一", "初二", "初三"});
                            break;
                        case 2: // 小学
                            selectedGradeLevel = "小学";
                            showSpecificGradeOptions(new String[]{"一年级", "二年级", "三年级", "四年级", "五年级", "六年级"});
                            break;
                    }
                });
        builder.show();
    }

    private void showSpecificGradeOptions(String[] options) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择具体年级")
                .setItems(options, (dialog, which) -> {
                    String selectedGrade = options[which];
                    LectureFragment fragment = (LectureFragment) getParentFragment();
                    if (fragment != null) {
                        fragment.setSelectedGradeLevel(selectedGrade);
                    }
                    // 不调用 proceedWithUpload，等待选择科目后再调用
                });
        builder.show();
    }

    private void showSubjectOptions() {
        String[] subjects;
        switch (selectedGradeLevel) {
            case "高中":
                subjects = new String[]{"语文", "数学", "英语", "物理", "历史", "化学", "政治", "地理", "生物"};
                break;
            case "初中":
                subjects = new String[]{"语文", "数学", "英语", "物理", "历史", "化学", "政治", "地理", "生物"};
                break;
            case "小学":
                subjects = new String[]{"语文", "数学", "英语"};
                break;
            default:
                subjects = new String[]{};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择科目")
                .setItems(subjects, (dialog, which) -> {
                    selectedSubject = subjects[which];
                    LectureFragment fragment = (LectureFragment) getParentFragment();
                    if (fragment != null) {
                        fragment.setSelectedSubject(selectedSubject);
                        fragment.proceedWithUpload(); // 在选择科目后调用
                    }
                    dismiss(); // 关闭对话框
                });
        builder.show();
    }

}
