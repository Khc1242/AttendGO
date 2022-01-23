package com.sucfyp.attendgo.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.sucfyp.attendgo.ChangePasswordActivity;
import com.sucfyp.attendgo.EditProfileActivity;
import com.sucfyp.attendgo.LoginActivity;
import com.sucfyp.attendgo.databinding.FragmentSettingBinding;

public class SettingFragment extends Fragment {

    private SettingViewModel settingViewModel;
    private FragmentSettingBinding binding;
    private TextView EditProfile;
    private TextView ChangePassword;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        settingViewModel = new ViewModelProvider(this).get(SettingViewModel.class);
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize and final the view(s)
        EditProfile = binding.editProfileTV;
        EditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        ChangePassword = binding.changePasswordTV;
        ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        // logoutBtn
        final Button signOutBtn = binding.logoutBtn;
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                onDestroy();
            }
        });
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}