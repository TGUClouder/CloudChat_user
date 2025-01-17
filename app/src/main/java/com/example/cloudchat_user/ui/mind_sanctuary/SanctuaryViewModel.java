package com.example.cloudchat_user.ui.mind_sanctuary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SanctuaryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SanctuaryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is 心灵树洞 fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}