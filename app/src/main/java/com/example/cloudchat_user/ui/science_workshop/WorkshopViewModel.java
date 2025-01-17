package com.example.cloudchat_user.ui.science_workshop;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class WorkshopViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public WorkshopViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is 科普课堂 fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}