package com.example.cloudchat_user.ui.interactive_lecture;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.DialogFragment;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.databinding.FragmentLectureBinding;

public class LectureFragment extends Fragment {

    private FragmentLectureBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLectureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button uploadButton = binding.uploadButton;

        uploadButton.setOnClickListener(v -> {
            SelectOptionsDialogFragment dialog = new SelectOptionsDialogFragment();
            dialog.show(getParentFragmentManager(), "SelectOptionsDialog");
        });
        // 设置已接单/待接单区域的文本颜色
        TextView textView = binding.textView;
        SpannableString spannableString = new SpannableString("已接单/待接单");
        ForegroundColorSpan blueSpan = new ForegroundColorSpan(getResources().getColor(android.R.color.holo_blue_light));
        spannableString.setSpan(blueSpan, 3, 7, 0); // 设置 "待接单" 为蓝色
        textView.setText(spannableString);
        textView.setGravity(android.view.Gravity.CENTER); // 设置文本居中
        // 设置已讲解视频区域的点击事件
        binding.previousVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow(v);
            }
        });

        return root;
    }

    private void showPopupWindow(View anchorView) {
        // 创建一个TextView作为PopupWindow的内容
        TextView popupContent = new TextView(getContext());
        popupContent.setText("您暂未有已讲解视频");
        popupContent.setGravity(Gravity.CENTER);
        popupContent.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // 创建PopupWindow
        final PopupWindow popupWindow = new PopupWindow(popupContent,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        // 设置背景和点击外部区域关闭PopupWindow
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_window_background)); // 使用自定义背景
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // 显示PopupWindow
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}