package com.example.cloudchat_user.ui.interactive_lecture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cloudchat_user.databinding.FragmentLectureBinding;

public class LectureFragment extends Fragment {

    private FragmentLectureBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LectureViewModel lectureViewModel =
                new ViewModelProvider(this).get(LectureViewModel.class);

        binding = FragmentLectureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLecture;
        lectureViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}