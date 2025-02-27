package com.example.cloudchat_user.ui.my_account;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.example.cloudchat_user.R;
import com.example.cloudchat_user.dao.UserDao;

import java.io.IOException;

public class AccountFragment extends Fragment {

    private ImageView profileImage;
    private Button loginButton;
    private Button registerButton;
    private View loginLayout;
    private View userLayout;
    private ImageView userProfileImage;
    private Button manageInfoButton;
    private Button changePasswordButton;
    private Button logoutButton;  // 添加注销按钮
    private String username = "user123"; // 默认用户名
    private String grade = "一年级"; // 默认年级
    private String gender = "男"; // 当前性别
    private String hobby = "篮球, 电影"; // 当前爱好

    String response;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载 fragment_account.xml 布局
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // 初始化视图
        loginLayout = view.findViewById(R.id.loginLayout);
        userLayout = view.findViewById(R.id.userLayout);
        profileImage = view.findViewById(R.id.profileImage);
        userProfileImage = view.findViewById(R.id.userProfileImage);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        manageInfoButton = view.findViewById(R.id.manageInfoButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        logoutButton = view.findViewById(R.id.logoutButton);  // 绑定注销按钮

        // 登录按钮监听器
        loginButton.setOnClickListener(v -> showLoginDialog());

        // 注册按钮监听器
        registerButton.setOnClickListener(v -> showRegisterDialog());

        // 个人信息管理按钮
        manageInfoButton.setOnClickListener(v -> showManageInfoDialog());
        changePasswordButton.setOnClickListener(v -> showOldPasswordDialog());
        logoutButton.setOnClickListener(v -> logout());
        return view;
    }
    private void showManageInfoDialog() {
        // 创建个人信息管理弹窗
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_manage_info, null);

        // 获取个人信息输入框
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        Spinner gradeSpinner = dialogView.findViewById(R.id.gradeSpinner);
        RadioGroup genderRadioGroup = dialogView.findViewById(R.id.genderRadioGroup);
        EditText hobbyInput = dialogView.findViewById(R.id.hobbyInput);

        // 设置默认值
        usernameInput.setText(username);
        hobbyInput.setText(hobby);

        // 设置年级选择框适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.grade_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(adapter);

        // 设置默认年级
        int gradePosition = adapter.getPosition(grade);
        gradeSpinner.setSelection(gradePosition);

        // 设置性别选择默认值
        if ("男".equals(gender)) {
            genderRadioGroup.check(R.id.maleRadioButton);
        } else {
            genderRadioGroup.check(R.id.femaleRadioButton);
        }

        Button modifyButton = dialogView.findViewById(R.id.modifyButton);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        modifyButton.setOnClickListener(v -> {
            // 获取修改后的信息
            String newUsername = usernameInput.getText().toString().trim();
            String selectedGrade = gradeSpinner.getSelectedItem().toString();
            String selectedGender = genderRadioGroup.getCheckedRadioButtonId() == R.id.maleRadioButton ? "男" : "女";
            String newHobby = hobbyInput.getText().toString().trim();

            // 更新信息
            if (!newUsername.isEmpty() && !newHobby.isEmpty()) {
                username = newUsername;
                grade = selectedGrade;
                gender = selectedGender;
                hobby = newHobby;

                Toast.makeText(getContext(), "个人信息修改成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();  // 关闭弹窗
            } else {
                Toast.makeText(getContext(), "请填写完整的个人信息", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    // 显示旧密码验证弹窗
    private void showOldPasswordDialog() {
        // 创建旧密码验证弹窗
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_old_password, null);
        EditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
        Button verifyButton = dialogView.findViewById(R.id.verifyButton);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        verifyButton.setOnClickListener(v -> {
            String oldPassword = oldPasswordInput.getText().toString().trim();
            if (!oldPassword.isEmpty()) {
                // 假设验证旧密码成功，弹出新密码设置界面
                showNewPasswordDialog(dialog);
            } else {
                Toast.makeText(getContext(), "请输入旧密码", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    // 显示新密码设置弹窗
    private void showNewPasswordDialog(AlertDialog oldPasswordDialog) {
        // 关闭旧密码弹窗
        oldPasswordDialog.dismiss();

        // 创建新密码设置弹窗
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_password, null);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);
        Button submitButton = dialogView.findViewById(R.id.submitButton);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        submitButton.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (!newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
                // 修改密码成功
                Toast.makeText(getContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    // 显示登录弹窗
    private void showLoginDialog() {
        // 加载登录弹窗布局
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_login, null);

        // 获取输入框和按钮
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        Button loginSubmitButton = dialogView.findViewById(R.id.loginSubmitButton);

        // 创建登录弹窗
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();


        // 登录按钮点击事件
        loginSubmitButton.setOnClickListener(v -> {
            // 获取输入的用户名和密码
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();


            // 检查账号和密码是否为空
            if (!username.isEmpty() && !password.isEmpty()) {
                // 如果信息有效，进行登录操作

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            response = UserDao.get_password(username);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start();
                while(response==null);
                if(response.contains("error")){
                    Toast.makeText(getContext(),"用户未注册成功！",Toast.LENGTH_SHORT).show();
                    return;
                } else if (response.contains("ConnectionFailed")) {
                    Toast.makeText(getContext(),"网络出错！",Toast.LENGTH_SHORT).show();
                    return;
                } else if (!response.equals(password)) {
                    Toast.makeText(getContext(),"帐号或密码错误！",Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Toast.makeText(getContext(),"登录成功！",Toast.LENGTH_SHORT).show();
                }
                loginLayout.setVisibility(View.GONE);  // 隐藏登录界面
                userLayout.setVisibility(View.VISIBLE); // 显示用户界面
                Toast.makeText(getContext(), "登录成功\n账号: " + username, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                // 如果信息为空，提示用户填写完整
                Toast.makeText(getContext(), "请填写账号和密码", Toast.LENGTH_SHORT).show();
            }
        });

        // 显示弹窗
        dialog.show();
    }


    // 显示注册弹窗
    private void showRegisterDialog() {
        // 加载注册弹窗布局
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_resister, null);

        // 获取输入框和按钮
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        Spinner gradeSpinner = dialogView.findViewById(R.id.gradeSpinner); // 获取年级选择框
        Button registerSubmitButton = dialogView.findViewById(R.id.registerSubmitButton);

        // 设置年级选择框适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.grade_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        registerSubmitButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String selectedGrade = gradeSpinner.getSelectedItem().toString();

            if (!username.isEmpty() && !password.isEmpty()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                    }
                }).start();
                Toast.makeText(getContext(), "注册成功\n账号: " + username + "\n年级: " + selectedGrade, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "请填写账号和密码", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    private void logout() {
        // 隐藏用户界面，显示登录界面
        loginLayout.setVisibility(View.VISIBLE);
        userLayout.setVisibility(View.GONE);
        Toast.makeText(getContext(), "注销成功，返回登录界面", Toast.LENGTH_SHORT).show();
    }
}
