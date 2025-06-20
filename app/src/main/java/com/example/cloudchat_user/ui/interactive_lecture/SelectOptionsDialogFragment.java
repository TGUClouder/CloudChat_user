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

    private String selectedGradeCategory; // 保存大类：高中/初中/小学
    private String selectedGradeLevel;     // 保存具体年级：高一/初二等
    private String selectedSubject;

    public interface OnOptionsSelectedListener {
        void onGradeAndSubjectSelected(String gradeCategory, String gradeLevel, String subject);
        void onUploadImageRequested(DialogFragment dialog);
    }

    private OnOptionsSelectedListener listener;

    public void setOnOptionsSelectedListener(OnOptionsSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_options, container, false);

        Button selectGradeButton = view.findViewById(R.id.selectGradeButton);
        Button selectSubjectButton = view.findViewById(R.id.selectSubjectButton);

        selectGradeButton.setOnClickListener(v -> showGradeOptions());
        Button selectImageButton = view.findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(v -> {
            if (selectedGradeCategory == null || selectedGradeLevel == null || selectedSubject == null) {
                new AlertDialog.Builder(getContext())
                        .setMessage("请先完成年级和科目选择")
                        .setPositiveButton("确定", null)
                        .show();

                return;
            }

            if (listener != null) {
                listener.onUploadImageRequested(SelectOptionsDialogFragment.this); // 传当前弹窗实例
            }
        });


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

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            // 设置弹窗宽度为屏幕宽度的 85%
            int width = (int) (requireContext().getResources().getDisplayMetrics().widthPixels * 0.85);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void showGradeOptions() {
        final String[] categories = {"高中", "初中", "小学"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择年级类型")
                .setItems(categories, (dialog, which) -> {
                    selectedGradeCategory = categories[which]; // 保存大类
                    showSpecificGradeOptions(which);
                });
        builder.show();
    }

    private void showSpecificGradeOptions(int categoryIndex) {
        String[] grades;
        switch (categoryIndex) {
            case 0: // 高中
                grades = new String[]{"高一", "高二", "高三"};
                break;
            case 1: // 初中
                grades = new String[]{"初一", "初二", "初三"};
                break;
            case 2: // 小学
                grades = new String[]{"一年级", "二年级", "三年级", "四年级", "五年级", "六年级"};
                break;
            default:
                grades = new String[]{};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择具体年级")
                .setItems(grades, (dialog, which) -> {
                    selectedGradeLevel = grades[which]; // 保存具体年级
//                    showSubjectOptions(); // 自动触发科目选择
                });
        builder.show();
    }

    private void showSubjectOptions() {
        String[] subjects;
        switch (selectedGradeCategory) {
            case "高中":
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

                    // 保存到本地
                    saveSelectionToPreferences(selectedGradeCategory, selectedGradeLevel, selectedSubject);

                    if (listener != null) {
                        listener.onGradeAndSubjectSelected(
                                selectedGradeCategory,
                                selectedGradeLevel,
                                selectedSubject
                        );
                    }
                });
        builder.show();
    }

    private void saveSelectionToPreferences(String gradeCategory, String gradeLevel, String subject) {
        if (getContext() == null) return; // 避免空指针

        getContext().getSharedPreferences("UserSelection", getContext().MODE_PRIVATE)
                .edit()
                .putString("gradeCategory", gradeCategory)
                .putString("gradeLevel", gradeLevel)
                .putString("subject", subject)
                .apply(); // 异步存储
    }

}
