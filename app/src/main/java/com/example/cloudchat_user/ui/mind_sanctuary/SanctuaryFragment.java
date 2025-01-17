package com.example.cloudchat_user.ui.mind_sanctuary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cloudchat_user.databinding.FragmentSanctuaryBinding;


public class SanctuaryFragment extends Fragment {

    private FragmentSanctuaryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SanctuaryViewModel sanctuaryViewModel =
                new ViewModelProvider(this).get(SanctuaryViewModel.class);

        binding = FragmentSanctuaryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSanctuary;
        sanctuaryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}