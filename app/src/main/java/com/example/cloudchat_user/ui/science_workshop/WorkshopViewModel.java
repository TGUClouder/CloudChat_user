package com.example.cloudchat_user.ui.science_workshop;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.cloudchat_user.dao.VotesDao;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WorkshopViewModel extends ViewModel {
    private final MutableLiveData<Integer> progress = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, ArrayList<String>>> voteLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> detail_switch = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<String>> details_list = new MutableLiveData<>();
    private final MutableLiveData<String> vote_id = new MutableLiveData<>();
    private final MutableLiveData<Integer> checked_position = new MutableLiveData<>();


    private HashMap<String, ArrayList<String>> arrayListMap = null;

    public WorkshopViewModel(){
        this.detail_switch.setValue(false);
        this.checked_position.setValue(-1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    arrayListMap = VotesDao.get_all_votes();
                    voteLiveData.postValue(arrayListMap);
                } catch (IOException | JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public MutableLiveData<Boolean> getDetail_switch() {
        return this.detail_switch;
    }
    public void setDetail_switch(Boolean flag){
        this.detail_switch.postValue(flag);
    }


    public MutableLiveData<HashMap<String, ArrayList<String>>> getVoteLiveData() {
        return this.voteLiveData;
    }

    public void setVoteLiveData(HashMap<String, ArrayList<String>> hashMap){
        this.voteLiveData.postValue(hashMap);
    }


    public MutableLiveData<ArrayList<String>> getDetails_list() {
        return this.details_list;
    }

    public void setDetails_list(ArrayList<String> arrayList) {
        this.details_list.postValue(arrayList);
    }

    public void setVote_id(String id){
        this.vote_id.postValue(id);
    }
    public MutableLiveData<String> getVote_id(){
        return this.vote_id;
    }

    public MutableLiveData<Integer> getChecked_position() {
        return this.checked_position;
    }

    public void setChecked_position(Integer integer){
        this.checked_position.postValue(integer);
    }

    public MutableLiveData<Integer> getProgress() {
        return this.progress;
    }

    public void setProgress(Integer integer){
        this.progress.postValue(integer);
    }
}