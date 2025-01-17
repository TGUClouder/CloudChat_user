package com.example.cloudchat_user.ui.my_account;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AccountViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public AccountViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is 用户中心 fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
