package com.example.cloudchat_user.ui.mind_sanctuary;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cloudchat_user.R;
import com.example.cloudchat_user.dao.DiaryDao;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class DiaryFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private static final String KEY_USERNAME = "username";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        EditText etDiaryInput = view.findViewById(R.id.et_diary_input);
        Button btnSubmit = view.findViewById(R.id.btn_submit_diary);
        Button btnViewDiary = view.findViewById(R.id.btn_view_diary);

        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        if (username.isEmpty()) {
            Toast.makeText(getContext(), "获取用户信息失败，请重新登录", Toast.LENGTH_SHORT).show();
            return view;
        }

        // 提交日记
        btnSubmit.setOnClickListener(v -> {
            String diaryText = etDiaryInput.getText().toString().trim();
            if (diaryText.isEmpty()) {
                Toast.makeText(getContext(), "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                try {
                    String result = DiaryDao.update_diary(username, diaryText);
                    requireActivity().runOnUiThread(() -> {
                        if ("ConnectionFailed".equals(result)) {
                            Toast.makeText(getContext(), "提交失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        } else if (result.contains("error")) {
                            Toast.makeText(getContext(), "提交失败：" + result, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "提交成功！", Toast.LENGTH_SHORT).show();
                            etDiaryInput.setText("");
                        }
                    });
                } catch (IOException e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "提交失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        // 查看日记
        btnViewDiary.setOnClickListener(v -> showDiaryDialog(username));

        return view;
    }

    // 弹出日记管理窗口
    private void showDiaryDialog(String username) {
        new Thread(() -> {
            try {
                String diaryData = DiaryDao.get_my_diary(username);
                if ("ConnectionFailed".equals(diaryData)) {
                    showToast("获取日记失败，请检查网络连接");
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("我的日记");

                    ScrollView scrollView = new ScrollView(getContext());
                    LinearLayout layout = new LinearLayout(getContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    scrollView.addView(layout);

                    ArrayList<EditText> diaryEdits = new ArrayList<>();
                    String[] diaryEntries = diaryData.split("\n");

                    for (String entry : diaryEntries) {
                        if (entry.isEmpty()) continue;

                        LinearLayout diaryLayout = new LinearLayout(getContext());
                        diaryLayout.setOrientation(LinearLayout.HORIZONTAL);

                        EditText editText = new EditText(getContext());
                        editText.setText(entry);
                        editText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                        diaryEdits.add(editText);

                        // 去掉删除按钮的部分
                        diaryLayout.addView(editText);
                        layout.addView(diaryLayout);
                    }

                    Button updateButton = new Button(getContext());
                    updateButton.setText("修改");
                    updateButton.setOnClickListener(v -> {
                        new Thread(() -> {
                            try {
                                for (EditText editText : diaryEdits) {
                                    String newContent = editText.getText().toString().trim();
                                    if (!newContent.isEmpty()) {
                                        DiaryDao.update_diary(username, newContent);
                                    }
                                }
                                requireActivity().runOnUiThread(() -> showToast("修改成功！"));
                            } catch (IOException e) {
                                showToast("修改失败：" + e.getMessage());
                            }
                        }).start();
                    });

                    layout.addView(updateButton);
                    builder.setView(scrollView);
                    builder.setNegativeButton("关闭", null);
                    builder.show();
                });
            } catch (IOException | JSONException e) {
                showToast("获取日记失败：" + e.getMessage());
                Log.e("DiaryFrag", e.toString());
            }
        }).start();
    }

    private void showToast(String message) {
        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }
}
