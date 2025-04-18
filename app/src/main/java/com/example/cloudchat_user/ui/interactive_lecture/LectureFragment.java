package com.example.cloudchat_user.ui.interactive_lecture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.dao.MaterialDao;
import com.example.cloudchat_user.network.PhotoWebSocketManager;
import com.example.cloudchat_user.databinding.FragmentLectureBinding;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class LectureFragment extends Fragment {
    private FragmentLectureBinding binding;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri;
    private Button uploadButton;
    private String selectedGradeCategory, selectedGradeLevel, selectedSubject;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLectureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        PhotoWebSocketManager.getInstance();
        Button JumpToDanmaku = binding.danmaku;
        Button selectGradeSubjectButton = binding.selectGradeSubjectButton;
        Button chooseImageButton = binding.uploadButton;
        uploadButton = binding.uploadFinalButton;

        uploadButton.setVisibility(View.GONE);

        selectGradeSubjectButton.setOnClickListener(v -> showGradeSelectionDialog());
        chooseImageButton.setOnClickListener(v -> dispatchPickImageIntent());
        uploadButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadFile(selectedImageUri);
            } else {
                Log.e("LectureFragment", "未选择图片");
            }
        });
        JumpToDanmaku.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_danmaku);
        });

        return root;
    }

    private void showGradeSelectionDialog() {
        SelectOptionsDialogFragment dialog = new SelectOptionsDialogFragment();
        dialog.setOnOptionsSelectedListener((gradeCategory, gradeLevel, subject) -> {
            saveSelectionToPreferences(gradeCategory, gradeLevel, subject);
            binding.selectGradeSubjectButton.setText(gradeCategory + " " + gradeLevel + " - " + subject);
        });
        dialog.show(getParentFragmentManager(), "SelectOptionsDialog");
    }
    private void saveSelectionToPreferences(String gradeCategory, String gradeLevel, String subject) {
        if (getContext() == null) return;

        getContext().getSharedPreferences("UserSelection", Context.MODE_PRIVATE)
                .edit()
                .putString("gradeCategory", gradeCategory)
                .putString("gradeLevel", gradeLevel)
                .putString("subject", subject)
                .apply();
    }

    private void dispatchPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        handleImagePick(result.getData().getData());
                    }
                }
        );
    }

    private void handleImagePick(Uri imageUri) {
        if (imageUri != null) {
            selectedImageUri = imageUri;
            showImagePopup(imageUri);
        }
    }

    private void showImagePopup(Uri imageUri) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.popup_image_upload, null);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        ImageView popupImageView = popupView.findViewById(R.id.popupImageView);
        Button popupUploadButton = popupView.findViewById(R.id.popupUploadButton);

        popupImageView.setImageURI(imageUri);
        popupUploadButton.setOnClickListener(v -> {
            uploadFile(selectedImageUri);
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(binding.getRoot(), Gravity.CENTER, 0, 0);
    }

    private void uploadFile(Uri fileUri) {
        Log.d("LectureFragment", "开始上传图片: " + fileUri.toString());
        loadSelectionFromPreferences();
        if (selectedGradeCategory == null || selectedGradeLevel == null || selectedSubject == null) {
            showErrorDialog("请先选择年级和科目！");
            return;
        }
        String remark = selectedGradeCategory + " - " + selectedGradeLevel + " - " + selectedSubject + " - ";

        new Thread(() -> {
            try {
                // 获取文件名
                String originalFileName = getFileName(fileUri);
                if (originalFileName == null) {
                    originalFileName = "default.jpg";
                }
                String timestamp = String.valueOf(System.currentTimeMillis());
                String fileName = timestamp + "_" + originalFileName;
                String fileUrl = "http://47.94.207.38/ljh/questions/" + fileName;

                // 读取文件数据并转换为 Base64
                String base64Image = encodeImageToBase64(fileUri);

                // 构造 JSON 消息
                JSONObject json = new JSONObject();
                json.put("fileName", fileName);
                json.put("imageData", base64Image);
                // 发送给 WebSocket 服务器
                PhotoWebSocketManager.getInstance().sendMessage(json.toString());
                MaterialDao.set_url_remark(fileUrl, remark);
                requireActivity().runOnUiThread(() -> showSuccessDialog("图片上传成功！"));
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showErrorDialog("上传异常: " + e.getMessage()));
            }
        }).start();
    }

    private String encodeImageToBase64(Uri imageUri) throws Exception {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void loadSelectionFromPreferences() {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("UserSelection", Context.MODE_PRIVATE);
        selectedGradeCategory = prefs.getString("gradeCategory", null);
        selectedGradeLevel = prefs.getString("gradeLevel", null);
        selectedSubject = prefs.getString("subject", null);
    }

    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex >= 0) {
                result = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return result;
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext()).setMessage(message).setPositiveButton("确定", null).show();
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(requireContext()).setMessage(message).setPositiveButton("确定", null).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
