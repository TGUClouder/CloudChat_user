package com.example.cloudchat_user.ui.interactive_lecture;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LectureViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LectureViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is 互动讲题 fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}