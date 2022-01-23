package com.sucfyp.attendgo.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeViewModel extends ViewModel {

    private String userName;
    private MutableLiveData<String> mText;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;

    public HomeViewModel() {
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userName = mAuth.getCurrentUser().getDisplayName();

        mText = new MutableLiveData<>();
        mText.setValue("Welcome " + userName + "!\nLet's take your attendance!");
    }

    public LiveData<String> getText() {
        return mText;
    }
}