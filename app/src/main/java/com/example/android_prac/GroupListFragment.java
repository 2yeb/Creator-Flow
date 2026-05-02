package com.example.android_prac;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android_prac.databinding.FragmentGroupListBinding;
import com.example.android_prac.databinding.FragmentSimulationBinding;
import com.example.android_prac.databinding.FragmentStatisticsBinding;

import org.jetbrains.annotations.Nullable;

public class GroupListFragment extends Fragment {
    FragmentGroupListBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}