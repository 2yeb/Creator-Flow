package com.example.creator_flow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.creator_flow.databinding.FragmentSimulationBinding;

public class SimulationFragment extends Fragment {
    FragmentSimulationBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSimulationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.businessBtn.setOnClickListener(v ->
                navigateToDetail1(SimulationDetail1Fragment.MODEL_BUSINESS));

        binding.fanartBtn.setOnClickListener(v ->
                navigateToDetail1(SimulationDetail1Fragment.MODEL_FANART));
    }

    private void navigateToDetail1(String modelType) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, SimulationDetail1Fragment.newInstance(modelType))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
