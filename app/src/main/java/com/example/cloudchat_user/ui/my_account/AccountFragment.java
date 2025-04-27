package com.example.cloudchat_user.ui.my_account;

import static com.example.cloudchat_user.dao.UserDao.get_password;
import static com.example.cloudchat_user.dao.UserDao.get_userinfo;
import static com.example.cloudchat_user.dao.UserDao.delete_account;
import static com.example.cloudchat_user.dao.UserDao.update_password;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.example.cloudchat_user.R;
import com.example.cloudchat_user.dao.DiaryDao;
import com.example.cloudchat_user.dao.UserDao;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
    private Button signOutButton; // 退出登录按钮
    private String username = "user123"; // 默认用户名
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 加载 fragment_account.xml 布局
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // 初始化 SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

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
        signOutButton = view.findViewById(R.id.signOutButton); // 绑定退出登录按钮

        // 检查登录状态
        checkLoginState();

        // 登录按钮监听器
        loginButton.setOnClickListener(v -> showLoginDialog());

        // 注册按钮监听器
        registerButton.setOnClickListener(v -> showRegisterDialog());

        // 个人信息管理按钮
        manageInfoButton.setOnClickListener(v -> showManageInfoDialog());
        changePasswordButton.setOnClickListener(v -> showOldPasswordDialog());
        logoutButton.setOnClickListener(v -> logout());
        signOutButton.setOnClickListener(v -> handleSignOut());
        return view;
    }

    private void checkLoginState() {
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");

        if (isLoggedIn && !savedUsername.isEmpty()) {
            // 用户已登录，显示用户界面
            loginLayout.setVisibility(View.GONE);
            userLayout.setVisibility(View.VISIBLE);
            username = savedUsername;
        } else {
            // 用户未登录，显示登录界面
            loginLayout.setVisibility(View.VISIBLE);
            userLayout.setVisibility(View.GONE);
        }
    }

    private void showManageInfoDialog() {
        String currentUsername = sharedPreferences.getString(KEY_USERNAME, "");
        if (currentUsername.isEmpty()) {
            showToast("获取用户信息失败");
            return;
        }

        new Thread(() -> {
            try {
                ArrayList<String> userInfo = get_userinfo(currentUsername);

                if (userInfo.isEmpty() || userInfo.get(0).equals("ConnectionFailed")) {
                    showToast("网络错误，请检查连接");
                    return;
                }

                if (userInfo.get(0).contains("error")) {
                    showToast("获取用户信息失败");
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_manage_info, null);

                    EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
                    EditText firstNameInput = dialogView.findViewById(R.id.firstNameInput);
                    EditText lastNameInput = dialogView.findViewById(R.id.lastNameInput);
                    EditText ageInput = dialogView.findViewById(R.id.ageInput);
                    EditText emailInput = dialogView.findViewById(R.id.emailInput);
                    EditText birthdateInput = dialogView.findViewById(R.id.birthdateInput);
                    EditText hobbyInput = dialogView.findViewById(R.id.hobbyInput);

                    Spinner educationSpinner = dialogView.findViewById(R.id.educationSpinner);
                    Spinner gradeSpinner = dialogView.findViewById(R.id.gradeSpinner);
                    RadioGroup genderRadioGroup = dialogView.findViewById(R.id.genderRadioGroup);

                    usernameInput.setText(currentUsername);
                    usernameInput.setEnabled(false);
                    firstNameInput.setEnabled(false);
                    lastNameInput.setEnabled(false);

                    // 设置 Spinner 适配器
                    ArrayAdapter<CharSequence> educationAdapter = ArrayAdapter.createFromResource(
                            getContext(), R.array.education_options, android.R.layout.simple_spinner_item);
                    educationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    educationSpinner.setAdapter(educationAdapter);

                    ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(
                            getContext(), R.array.grade_options, android.R.layout.simple_spinner_item);
                    gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    gradeSpinner.setAdapter(gradeAdapter);

                    // 获取并填充用户数据
                    String userFirstName = userInfo.get(1);
                    String userLastName = userInfo.get(2);
                    String userAge = userInfo.get(3);
                    String userEmail = userInfo.get(4);
                    String userGender = userInfo.get(5);
                    String userEducation = userInfo.get(6);
                    String userBirthdate = userInfo.get(7);
                    String userHobby = userInfo.get(8);
                    String userGrade = userInfo.get(9);

                    firstNameInput.setText(userFirstName);
                    lastNameInput.setText(userLastName);
                    ageInput.setText(userAge);
                    emailInput.setText(userEmail);
                    birthdateInput.setText(userBirthdate);
                    hobbyInput.setText(userHobby);

                    educationSpinner.setSelection(educationAdapter.getPosition(userEducation));
                    gradeSpinner.setSelection(gradeAdapter.getPosition(userGrade));

                    if ("男".equals(userGender)) {
                        genderRadioGroup.check(R.id.maleRadioButton);
                    } else {
                        genderRadioGroup.check(R.id.femaleRadioButton);
                    }

                    // 设置日期选择器
                    birthdateInput.setOnClickListener(v -> {
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                                (view, year, month, dayOfMonth) -> {
                                    String date = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth);
                                    birthdateInput.setText(date);
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        datePickerDialog.show();
                    });

                    Button modifyButton = dialogView.findViewById(R.id.modifyButton);
                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setView(dialogView)
                            .create();

                    modifyButton.setOnClickListener(v -> {
                        String selectedFirstName = firstNameInput.getText().toString().trim();
                        String selectedLastName = lastNameInput.getText().toString().trim();
                        String selectedAge = ageInput.getText().toString().trim();
                        String selectedEmail = emailInput.getText().toString().trim();
                        String selectedGender = genderRadioGroup.getCheckedRadioButtonId() == R.id.maleRadioButton ? "男" : "女";
                        String selectedEducation = educationSpinner.getSelectedItem().toString();
                        String selectedBirthdate = birthdateInput.getText().toString().trim();
                        String selectedHobby = hobbyInput.getText().toString().trim();
                        String selectedGrade = gradeSpinner.getSelectedItem().toString();

                        // 判断修改内容
                        ArrayList<String> updateInfo = new ArrayList<>();
                        updateInfo.add(currentUsername);
                        updateInfo.add(selectedFirstName.equals(userFirstName) ? "null" : selectedFirstName);
                        updateInfo.add(selectedLastName.equals(userLastName) ? "null" : selectedLastName);
                        updateInfo.add(selectedAge.equals(userAge) ? "null" : selectedAge);
                        updateInfo.add(selectedEmail.equals(userEmail) ? "null" : selectedEmail);
                        updateInfo.add(selectedGender.equals(userGender) ? "null" : selectedGender);
                        updateInfo.add(selectedEducation.equals(userEducation) ? "null" : selectedEducation);
                        updateInfo.add(selectedBirthdate.equals(userBirthdate) ? "null" : selectedBirthdate);
                        updateInfo.add(selectedHobby.equals(userHobby) ? "null" : selectedHobby);
                        updateInfo.add(selectedGrade.equals(userGrade) ? "null" : selectedGrade);

                        boolean hasChanges = false;
                        for (String value : updateInfo) {
                            if (!"null".equals(value)) {
                                hasChanges = true;
                                break;
                            }
                        }

                        if (!hasChanges) {
                            showToast("未修改任何内容");
                            return;
                        }

                        new Thread(() -> {
                            try {
                                String result = UserDao.update_userinfo(updateInfo);
                                requireActivity().runOnUiThread(() -> {
                                    if ("ConnectionFailed".equals(result)) {
                                        showToast("更新失败，请检查网络连接");
                                    } else if (result.contains("error")) {
                                        showToast("更新失败：" + result);
                                    } else {
                                        showToast("更新成功");
                                        dialog.dismiss();
                                    }
                                });
                            } catch (IOException e) {
                                showToast("更新失败：" + e.getMessage());
                            }
                        }).start();
                    });

                    dialog.show();
                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
                });
            } catch (IOException e) {
                showToast("获取用户信息失败：" + e.getMessage());
            }
        }).start();
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
            if (oldPassword.isEmpty()) {
                Toast.makeText(getContext(), "请输入旧密码", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取当前用户名
            String username = sharedPreferences.getString(KEY_USERNAME, "");
            if (username.isEmpty()) {
                Toast.makeText(getContext(), "用户名为空，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 在后台线程执行密码验证
            new Thread(() -> {
                try {
                    String currentPassword = get_password(username);

                    if ("ConnectionFailed".equals(currentPassword)) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "网络错误，请检查连接", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    if (currentPassword.contains("error")) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "获取密码失败，请稍后再试", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }

                    // 验证输入的旧密码是否正确
                    if (currentPassword.equals(oldPassword)) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "密码正确", Toast.LENGTH_SHORT).show();
                            dialog.dismiss(); // 关闭旧密码弹窗
                            showNewPasswordDialog(dialog); // 进入新密码设置界面
                        });
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "密码错误，请重新输入", Toast.LENGTH_SHORT).show()
                        );
                    }
                } catch (IOException e) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "验证密码异常: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
    }


    // 显示新密码设置弹窗
    private void showNewPasswordDialog(AlertDialog oldPasswordDialog) {
        oldPasswordDialog.dismiss();
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

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "请输入新密码", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(getContext(), "密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                return;
            }

            // 获取当前用户名
            String username = sharedPreferences.getString(KEY_USERNAME, "").trim();
            Log.d("UserDao", "获取到的用户名: " + username);
            if (username.isEmpty()) {
                Toast.makeText(getContext(), "用户名为空，请重新登录", Toast.LENGTH_SHORT).show();
                return;
            }

            // 在后台线程执行密码更新
            new Thread(() -> {
                try {
                    String result = update_password(username, newPassword);
                    Log.d("UserDao", "update_password 返回值: " + result);

                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> {
                            if ("ConnectionFailed".equals(result)) {
                                Toast.makeText(getContext(), "网络错误，请检查连接", Toast.LENGTH_SHORT).show();
                            } else if (result.contains("error")) {
                                Toast.makeText(getContext(), "修改密码失败，请稍后再试", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "密码修改成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }

                } catch (IOException e) {
                    Log.e("UserDao", "修改密码异常: " + e.getMessage());
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(getContext(), "修改密码异常: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            }).start();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
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
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "请填写账号和密码", Toast.LENGTH_SHORT).show();
                return;
            }

            // 在后台线程执行登录请求
            new Thread(() -> {
                try {
                    ArrayList<String> userInfo = get_userinfo(username);
                    if (userInfo.isEmpty() || userInfo.get(0).equals("ConnectionFailed")) {
                        // 网络连接失败
                        showToast("网络错误，请检查连接");
                        return;
                    }

                    if (userInfo.get(0).contains("error")) {
                        // 服务器返回错误，说明用户不存在
                        showToast("该用户不存在，请注册");
                        return;
                    }

                    // 获取服务器返回的用户名和密码
                    String dbUsername = username;  // 服务器已根据 username 查询
                    String dbPassword = get_password(username);
                    if (dbUsername.equals(username)) {
                        if (dbPassword.equals(password)) {
                            // 登录成功
                            showToast("登录成功\n账号: " + username);
                            requireActivity().runOnUiThread(() -> {
                                handleLoginSuccess(username);
                                dialog.dismiss();
                            });
                        } else {
                            // 密码错误
                            showToast("密码不正确，请重新输入");
                        }
                    }
                } catch (IOException e) {
                    showToast("登录异常: " + e.getMessage());
                }
            }).start();
        });

        // 显示弹窗
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
    }

    // 显示 Toast，确保在 UI 线程运行
    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
        );
    }


    private void showRegisterDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_resister, null);

        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText firstNameInput = dialogView.findViewById(R.id.firstNameInput);
        EditText lastNameInput = dialogView.findViewById(R.id.lastNameInput);
        EditText ageInput = dialogView.findViewById(R.id.ageInput);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        RadioGroup genderGroup = dialogView.findViewById(R.id.genderGroup);
        Spinner educationSpinner = dialogView.findViewById(R.id.educationSpinner);
        EditText birthdateInput = dialogView.findViewById(R.id.birthdateInput);
        EditText hobbyInput = dialogView.findViewById(R.id.hobbyInput);
        Spinner gradeSpinner = dialogView.findViewById(R.id.gradeSpinner);
        Button registerSubmitButton = dialogView.findViewById(R.id.registerSubmitButton);

        // 学历下拉框
        ArrayAdapter<CharSequence> eduAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.education_options, android.R.layout.simple_spinner_item);
        eduAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        educationSpinner.setAdapter(eduAdapter);

        // 年级下拉框
        ArrayAdapter<CharSequence> gradeAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.grade_options, android.R.layout.simple_spinner_item);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);

        // 设置生日点击弹出日期选择器
        birthdateInput.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        birthdateInput.setText(dateStr);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        registerSubmitButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String age = ageInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String education = educationSpinner.getSelectedItem().toString();
            String birthdate = birthdateInput.getText().toString().trim();
            String hobby = hobbyInput.getText().toString().trim();
            String selectedGrade = gradeSpinner.getSelectedItem().toString();

            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            String gender = "";
            if (selectedGenderId == R.id.genderMale) {
                gender = "男";
            } else if (selectedGenderId == R.id.genderFemale) {
                gender = "女";
            }

            if (!username.isEmpty() && !password.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() &&
                    !age.isEmpty() && !email.isEmpty() && !gender.isEmpty() &&
                    !education.isEmpty() && !birthdate.isEmpty()) {

                ArrayList<String> signupData = new ArrayList<>();
                signupData.add(username);
                signupData.add(password);
                signupData.add(firstName);
                signupData.add(lastName);
                signupData.add(age);
                signupData.add(email);
                signupData.add(gender);
                signupData.add(education);
                signupData.add(birthdate);
                signupData.add(hobby);
                signupData.add(selectedGrade);

                new Thread(() -> {
                    try {
                        String result = UserDao.signup(signupData);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if ("ConnectionFailed".equals(result)) {
                                    Toast.makeText(getContext(), "注册失败，连接服务器失败", Toast.LENGTH_SHORT).show();
                                } else if (result.contains("error")) {
                                    Toast.makeText(getContext(), "用户名重复：" + result, Toast.LENGTH_SHORT).show();
                                } else {
                                    new Thread(() -> {
                                        try {
                                            DiaryDao.set_diary(username, null);
                                        } catch (IOException e) {
                                            Log.e("RegisterDebug", "设置日记失败", e);
                                        }
                                    }).start();
                                    Toast.makeText(getContext(), "注册成功\n账号: " + username, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e("RegisterDebug", "注册异常", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "注册失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }
                }).start();
            } else {
                Toast.makeText(getContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
    }


    private void logout() {
        // 获取当前登录的用户名
        String username = sharedPreferences.getString(KEY_USERNAME, "");

        // 在后台线程执行注销请求
        new Thread(() -> {
            try {
                // 调用删除用户的方法（从数据库中删除该用户）
                String result = delete_account(username);

                if ("ConnectionFailed".equals(result)) {
                    showToast("网络错误，请检查连接");
                    return;
                }
                if (result.contains("error")) {
                    showToast("注销失败，请稍后再试");
                    return;
                }
                // 清除本地登录状态
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_IS_LOGGED_IN, false);
                editor.putString(KEY_USERNAME, "");
                editor.apply();

                // 更新界面
                requireActivity().runOnUiThread(() -> {
                    loginLayout.setVisibility(View.VISIBLE);
                    userLayout.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "注销成功，返回登录界面", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                showToast("注销异常: " + e.getMessage());
            }
        }).start();
    }


    private void handleLoginSuccess(String username) {
        // 保存登录状态
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply();

        // 更新界面
        loginLayout.setVisibility(View.GONE);
        userLayout.setVisibility(View.VISIBLE);
        this.username = username;
    }

    // 退出登录
    private void handleSignOut() {
        // 清除 SharedPreferences 中的所有数据
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();  // 清除所有数据
        editor.apply();
        loginLayout.setVisibility(View.VISIBLE);
        userLayout.setVisibility(View.GONE);
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
    }

}
