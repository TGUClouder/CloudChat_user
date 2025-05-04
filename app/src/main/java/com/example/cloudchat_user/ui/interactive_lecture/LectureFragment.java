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
import android.util.JsonReader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.dao.MaterialDao;
import com.example.cloudchat_user.dao.UserDao;
import com.example.cloudchat_user.network.PhotoWebSocketManager;
import com.example.cloudchat_user.databinding.FragmentLectureBinding;
import com.example.cloudchat_user.ui.PlayerActivity;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class LectureFragment extends Fragment {
    private FragmentLectureBinding binding;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri selectedImageUri;
    private Button uploadButton;
    private String selectedGradeCategory, selectedGradeLevel, selectedSubject;
    private boolean keepCheckingStatus = false;
    private final int CHECK_INTERVAL_MS = 5000; // 每5秒检查一次

    private void startStatusPolling() {
        keepCheckingStatus = true;
        new Thread(() -> {
            while (keepCheckingStatus) {
                checkPhotoStatus();
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopStatusPolling() {
        keepCheckingStatus = false;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLectureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        TextView acceptedOrderLink = binding.acceptedOrderLink;
        acceptedOrderLink.setEnabled(false);
        acceptedOrderLink.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
        checkPhotoStatus();
        startStatusPolling();
        PhotoWebSocketManager.getInstance();
        Button selectGradeSubjectButton = binding.selectGradeSubjectButton;
        uploadButton = binding.uploadFinalButton;
        uploadButton.setVisibility(View.GONE);
        selectGradeSubjectButton.setOnClickListener(v -> {
            new Thread(() -> {
                boolean canUpload = canUploadNewImage();
                requireActivity().runOnUiThread(() -> {
                    if (canUpload) {
                        showGradeSelectionDialogThenPickImage();
                    } else {
                        showErrorDialog("当前图片还未被接单，请等待讲解完成后再上传新图片。");
                    }
                });
            }).start();
        });


        uploadButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                uploadFile(selectedImageUri);
            } else {
                Log.e("LectureFragment", "未选择图片");
            }
        });
        acceptedOrderLink.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlayerActivity.class);
            startActivity(intent);
        });



        return root;
    }

    private void checkPhotoStatus() {
        new Thread(() -> {
            try {
                SharedPreferences prefs = requireActivity().getSharedPreferences("upload_prefs", Context.MODE_PRIVATE);
                String uploadId = prefs.getString("upload_id", null);
                if (uploadId == null) return;
                HashMap<String, ArrayList<String>> allData = MaterialDao.get_all_res();
                if (allData.containsKey(uploadId)) {
                    ArrayList<String> data = allData.get(uploadId);
                    if (data.size() >= 3) {
                        String status = data.get(2);
                        requireActivity().runOnUiThread(() -> {
                            TextView acceptedOrderLink = binding.acceptedOrderLink;
                            if ("未接单".equals(status)) {
                                acceptedOrderLink.setText("未接单，请等待");
                                acceptedOrderLink.setEnabled(false);
                                acceptedOrderLink.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
                            } else {
                                acceptedOrderLink.setText("已接单，点击查看讲解");
                                stopStatusPolling();
                                acceptedOrderLink.setEnabled(true);
                            }
                        });
                    } else {
                        Log.e("LectureFragment", "data 长度不足 3，无法获取 status");
                    }
                } else {
                    Log.e("LectureFragment", "allData 中不包含 uploadId: " + uploadId);
                }
            } catch (Exception e) {
                Log.e("LectureFragment", "checkPhotoStatus error", e);
            }
        }).start();
    }




    private void showGradeSelectionDialogThenPickImage() {
        SelectOptionsDialogFragment dialog = new SelectOptionsDialogFragment();
        dialog.setOnOptionsSelectedListener(new SelectOptionsDialogFragment.OnOptionsSelectedListener() {
            @Override
            public void onGradeAndSubjectSelected(String gradeCategory, String gradeLevel, String subject) {
                saveSelectionToPreferences(gradeCategory, gradeLevel, subject);
                binding.selectGradeSubjectButton.setText(gradeCategory + " " + gradeLevel + " - " + subject);
            }

            @Override
            public void onUploadImageRequested(DialogFragment dialog) {
                dispatchPickImageIntent(); // 打开图片选择器
                dialog.dismiss(); // 选择图片后关闭弹窗
            }
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
    private boolean canUploadNewImage() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("upload_prefs", Context.MODE_PRIVATE);
        String uploadId = prefs.getString("upload_id", null);
        if (uploadId == null) return true; // 没有上传记录，可以上传

        try {
            HashMap<String, ArrayList<String>> allData = MaterialDao.get_all_res();
            if (allData.containsKey(uploadId)) {
                ArrayList<String> data = allData.get(uploadId);
                if (data.size() >= 3) {
                    String status = data.get(2);
                    return !"未接单".equals(status); // 只有不是“未接单”才可以上传新图片
                }
            }
            return true; // 没有找到对应数据，默认允许上传
        } catch (Exception e) {
            e.printStackTrace();
            return true; // 异常时默认允许上传，防止用户卡住
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

                // 转为 Base64
                String base64Image = encodeImageToBase64(fileUri);

                // 构造 JSON 消息
                JSONObject json = new JSONObject();
                json.put("fileName", fileName);
                json.put("imageData", base64Image);

                // 获取 WebSocket 实例
                PhotoWebSocketManager wsManager = PhotoWebSocketManager.getInstance();
                int retryCount = 0;
                while ((wsManager == null || !wsManagerIsConnected(wsManager)) && retryCount < 10) {
                    Log.d("LectureFragment", "等待 WebSocket 连接...");
                    Thread.sleep(500);
                    retryCount++;
                }

                if (wsManager != null && wsManagerIsConnected(wsManager)) {
                    wsManager.sendMessage(json.toString());
                    String id = MaterialDao.set_url_remark(fileUrl, remark);
                    Log.d("LectureFragment", "上传成功，返回的ID为: " + id);
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("upload_prefs", Context.MODE_PRIVATE);
                    sharedPreferences.edit().putString("upload_id", id).apply();


                    requireActivity().runOnUiThread(() -> showSuccessDialog("图片上传成功！"));
                } else {
                    requireActivity().runOnUiThread(() -> showErrorDialog("WebSocket 未连接，上传失败"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> showErrorDialog("上传异常: " + e.getMessage()));
            }
        }).start();
    }

    // 判断 WebSocket 是否连接
    private boolean wsManagerIsConnected(PhotoWebSocketManager manager) {
        try {
            java.lang.reflect.Field field = PhotoWebSocketManager.class.getDeclaredField("webSocketClient");
            field.setAccessible(true);
            WebSocketClient client = (WebSocketClient) field.get(manager);
            return client != null && client.isOpen();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        stopStatusPolling();
        binding = null;

    }
}
