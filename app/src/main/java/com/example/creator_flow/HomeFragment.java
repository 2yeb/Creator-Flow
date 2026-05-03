package com.example.creator_flow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.creator_flow.databinding.FragmentHomeBinding;
import com.example.creator_flow.databinding.FragmentSimulationBinding;

import org.jetbrains.annotations.Nullable;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}