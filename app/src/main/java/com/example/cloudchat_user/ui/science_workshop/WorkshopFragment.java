package com.example.cloudchat_user.ui.science_workshop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cloudchat_user.databinding.FragmentWorkshopBinding;

public class WorkshopFragment extends Fragment {

    private FragmentWorkshopBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        WorkshopViewModel workshopViewModel =
                new ViewModelProvider(this).get(WorkshopViewModel.class);

        binding = FragmentWorkshopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textWorkshop;
        workshopViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}