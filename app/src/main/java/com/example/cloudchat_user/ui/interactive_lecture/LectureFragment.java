package com.example.cloudchat_user.ui.interactive_lecture;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.cloudchat_user.R;
import com.example.cloudchat_user.databinding.FragmentLectureBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import androidx.activity.result.ActivityResultLauncher;

public class LectureFragment extends Fragment
        implements SelectOptionsDialogFragment.OnOptionsSelectedListener ,
        SelectOptionsDialogFragment.OnUploadMethodSelectedListener {
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> pickFileLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;    private FragmentLectureBinding binding;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;
    private static final int REQUEST_FILE_PICK = 3;
    private Uri photoURI;
    private String selectedGradeCategory;
    private String selectedGradeLevel;
    private String selectedSubject;
    // 在 LectureFragment 类中添加
    private int currentMethod = -1;
    private ActivityResultLauncher<String[]> permissionLauncher;
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

        View view = inflater.inflate(R.layout.fragment_lecture, container, false);

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
                            Log.d("Upload", "拍照选项被点击");
                            dispatchTakePictureIntent();
                            break;
                        case 1:
                            Log.d("Upload", "从图库选择选项被点击");
                            dispatchPickImageIntent();
                            break;
                        case 2:
                            Log.d("Upload", "选取文件选项被点击");
                            dispatchPickFileIntent();
                            break;
                    }
                });
        builder.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        dispatchTakePictureIntent();
                    } else {
                        // 处理权限被拒绝的情况
                        showErrorDialog("需要相机权限才能拍照");
                    }
                }
        );
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = true;
                    for (Boolean permission : result.values()) {
                        if (!permission) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        // 所有权限都已授予，继续上传流程
                        proceedWithUpload();
                    } else {
                        // 处理权限被拒绝的情况
                        showErrorDialog("需要权限才能上传文件");
                    }});
        // 初始化拍照Launcher
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d("LectureFragment", "Picture taken successfully");
                        handleImageCapture();
                    } else {
                        Log.d("LectureFragment", "Failed to take picture or result not OK");
                    }
                }
        );

        // 初始化图片选择Launcher
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImagePick(result.getData().getData());
                    }
                });

        // 初始化文件选择Launcher
        pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleFilePick(result.getData().getData());
                    }
                });
    }

    public void dispatchTakePictureIntent() {
        Log.d("LectureFragment", "Dispatching take picture intent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Handle error
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.cloudchat_user.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureLauncher.launch(takePictureIntent);
            }
        }else {
            Log.e("LectureFragment", "No activity found to handle the intent");}
    }



    public void dispatchPickImageIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    public void dispatchPickFileIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "image/*",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        });
        pickFileLauncher.launch(intent);
    }
    private void checkCameraPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            Log.d("LectureFragment", "Camera permission granted, proceeding to take picture.");
            dispatchTakePictureIntent();
        }
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
        if (photoURI != null) {
            // 将照片显示在界面上
            displayCapturedImage(photoURI);
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
        // 确保信息存在
        if (selectedGradeCategory == null || selectedGradeLevel == null || selectedSubject == null) {
            showErrorDialog("信息不完整，请重新选择");
            return;
        }

        // 获取文件信息
        String fileName = getFileName(fileUri);
        String mimeType = requireContext().getContentResolver().getType(fileUri);

        // 构建上传对象
        UploadRequest request = new UploadRequest(
                selectedGradeCategory,
                selectedGradeLevel,
                selectedSubject,
                fileUri.toString(),
                fileName,
                mimeType
        );

        // TODO: 实现实际上传逻辑
        Log.d("Upload", request.toString());
    }

    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = null; // 在try块之外声明cursor变量
        if (uri.getScheme().equals("content")) {
            try {
                cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close(); // 确保在finally块中关闭cursor
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // 上传请求数据类
    private static class UploadRequest {
        final String gradeCategory;
        final String gradeLevel;
        final String subject;
        final String fileUri;
        final String fileName;
        final String mimeType;

        UploadRequest(String gradeCategory, String gradeLevel, String subject,
                      String fileUri, String fileName, String mimeType) {
            // 初始化所有字段...
            // 使用 this 明确赋值给字段
            this.gradeCategory = gradeCategory;
            this.gradeLevel = gradeLevel;
            this.subject = subject;
            this.fileUri = fileUri;
            this.fileName = fileName;
            this.mimeType = mimeType;
        }

        @Override
        public String toString() {
            return "UploadRequest{" +
                    "gradeCategory='" + gradeCategory + '\'' +
                    ", gradeLevel='" + gradeLevel + '\'' +
                    ", subject='" + subject + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", mimeType='" + mimeType + '\'' +
                    '}';
        }
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
        // 关闭年级科目选择对话框
        Fragment prev = getParentFragmentManager().findFragmentByTag("SelectOptionsDialog");
        if (prev != null) {
            ((DialogFragment) prev).dismiss();
        }

        // 检查权限后执行上传
        checkPermissionsAndProceed(-1);
    }

    private void checkPermissionsAndProceed(int method) {
        String[] requiredPermissions = getRequiredPermissions(method);
        if (checkSelfPermissions(requiredPermissions)) {
            handleMethodSelection(method);
        } else {
            requestPermissions(requiredPermissions, PERMISSION_REQUEST_CODE);
        }
    }
    // 添加成员变量

    private void handleMethodSelection(int method) {
        switch (method) {
            case 0: // 拍照
                dispatchTakePictureIntent();
                break;
            case 1: // 从图库选择
                dispatchPickImageIntent();
                break;
            case 2: // 选取文件
                dispatchPickFileIntent();
                break;
        }
    }
    private void showSelectOptionsDialog() {
        SelectOptionsDialogFragment dialog = new SelectOptionsDialogFragment();
        dialog.setOnUploadMethodSelectedListener(this); // 关键设置
        dialog.show(getParentFragmentManager(), "SelectOptionsDialog");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 使用保存的method值
                handleMethodSelection(currentMethod);
            } else {
                showErrorDialog("需要权限才能上传文件");
            }
        }

    }
    @Override
    public void onUploadMethodSelected(int method, String grade, String level, String subject) {
        // 保存选择信息
        this.selectedGradeCategory = grade;
        this.selectedGradeLevel = level;
        this.selectedSubject = subject;
        this.currentMethod = method;
        // 检查权限
        checkPermissionsAndProceed(method);
    }




    private void displayCapturedImage(Uri imageUri) {
        // 在界面上显示图片
        ImageView imageView = getView().findViewById(R.id.capturedImageView);
        imageView.setImageURI(imageUri);
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(v -> viewCapturedImage(imageUri));
    }

    private void viewCapturedImage(Uri imageUri) {
        // 处理点击图片的逻辑，例如放大查看
        Intent intent = new Intent(getContext(), FullScreenImageActivity.class);
        intent.setData(imageUri);
        startActivity(intent);
    }

    public class FullScreenImageActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_full_screen_image);

            ImageView imageView = findViewById(R.id.fullScreenImageView);
            Uri imageUri = getIntent().getData();
            imageView.setImageURI(imageUri);
        }
    }

    private boolean checkSelfPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] getRequiredPermissions(int method) {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if (method == 0) { // 拍照需要相机权限
            permissions.add(Manifest.permission.CAMERA);
        }

        return permissions.toArray(new String[0]);
    }

}