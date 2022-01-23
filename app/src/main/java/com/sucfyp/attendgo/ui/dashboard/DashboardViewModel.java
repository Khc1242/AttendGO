package com.sucfyp.attendgo.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private MutableLiveData<String> mText2;
    private MutableLiveData<String> mText3;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public DashboardViewModel() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mText = new MutableLiveData<>();
        mText.setValue(currentUser.getDisplayName());
        mText2 = new MutableLiveData<>();
        mText2.setValue(currentUser.getEmail());
        mText3 = new MutableLiveData<>();
        mText3.setValue(currentUser.getUid());
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getMail() {
        return mText2;
    }

    public LiveData<String> getUid() { return mText3;}
}