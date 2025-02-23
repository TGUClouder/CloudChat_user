package com.example.cloudchat_user.ui.interactive_lecture;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.databinding.FragmentLectureBinding;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;
import androidx.activity.result.ActivityResultLauncher;

public class LectureFragment extends Fragment
        implements SelectOptionsDialogFragment.OnOptionsSelectedListener{
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> pickFileLauncher;
    private FragmentLectureBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_FILE_PICK = 3;
    private Uri photoURI;
    private String selectedGradeCategory;
    private String selectedGradeLevel;
    private String selectedSubject;

    private static final int PERMISSION_REQUEST_CODE = 100;
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
        textView.setGravity(Gravity.CENTER); // 设置文本居中

        // 设置已讲解视频区域的点击事件
        binding.previousVideos.setOnClickListener(v -> showPopupWindow(v));

        return root;
    }

    private void proceedWithUpload() {
        Log.d("LectureFragment", "进入上传流程");
        if (selectedGradeCategory == null || selectedGradeLevel == null || selectedSubject == null) {
            Log.e("LectureFragment", "数据不完整: 年级大类=" + selectedGradeCategory + ", 具体年级=" + selectedGradeLevel + ", 科目=" + selectedSubject);
            showErrorDialog("请先完整选择年级和科目");
            return;
        }
        showUploadOptions();
    }
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }
    private void showUploadOptions() {
        Log.d("LectureFragment", "显示上传方式弹窗");
        final CharSequence[] options = {"拍照", "从图库选择", "上传附件"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("选择上传方式")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            dispatchTakePictureIntent();
                            break;
                        case 1:
                            dispatchPickImageIntent();
                            break;
                        case 2:
                            dispatchPickFileIntent();
                            break;
                    }
                });
        builder.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.cloudchat_user.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private void dispatchPickImageIntent() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    private void dispatchPickFileIntent() {
        Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickFileIntent.setType("*/*");
        startActivityForResult(pickFileIntent, REQUEST_FILE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    // Handle the captured image
                    handleImageCapture();
                    break;
                case REQUEST_IMAGE_PICK:
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        handleImagePick(selectedImage);
                    }
                    break;
                case REQUEST_FILE_PICK:
                    if (data != null) {
                        Uri selectedFile = data.getData();
                        handleFilePick(selectedFile);
                    }
                    break;
            }
        }
    }

    private void handleImageCapture() {
        // 处理拍照后的逻辑
        if (photoURI != null) {
            // 这里可以上传照片并保存年级和学科信息
            uploadFile(photoURI, "image");
        }
    }

    private void handleImagePick(Uri selectedImage) {
        // 处理从图库选择图片后的逻辑
        if (selectedImage != null) {
            // 这里可以上传图片并保存年级和学科信息
            uploadFile(selectedImage, "image");
        }
    }

    private void handleFilePick(Uri selectedFile) {
        // 处理选择文件后的逻辑
        if (selectedFile != null) {
            // 这里可以上传文件并保存年级和学科信息
            uploadFile(selectedFile, "file");
        }
    }

    private void uploadFile(Uri fileUri, String fileType) {
        // 这里实现上传文件的逻辑
        // 你可以使用 selectedGradeLevel 和 selectedSubject 来保存年级和学科信息
        // 例如：
        // String grade = selectedGradeLevel;
        // String subject = selectedSubject;
        // 然后调用你的上传API
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = getContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
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
    public void onGradeAndSubjectSelected(String gradeCategory, String gradeLevel, String subject) {
        Log.d("LectureFragment", "收到回调: 年级大类=" + gradeCategory + ", 具体年级=" + gradeLevel + ", 科目=" + subject);
        this.selectedGradeCategory = gradeCategory;
        this.selectedGradeLevel = gradeLevel;
        this.selectedSubject = subject;

        if (isAdded() && !isDetached()) {
            proceedWithUpload();
        }
    }

    private void showSelectOptionsDialog() {
        SelectOptionsDialogFragment dialog = new SelectOptionsDialogFragment();
        dialog.setOnOptionsSelectedListener(this);
        dialog.show(getParentFragmentManager(), "SelectOptionsDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}