package com.example.creator_flow;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.creator_flow.databinding.FragmentGroupListBinding;
import com.example.creator_flow.databinding.FragmentProjectBinding;

import org.jetbrains.annotations.Nullable;

public class ProjectFragment extends Fragment {

    private FrameLayout toggleContainer;
    private View selectedBg;
    private TextView btnStatistics;
    private TextView btnGroupList;
    FragmentProjectBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProjectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 초기화
        toggleContainer = view.findViewById(R.id.toggle_container);
        selectedBg = view.findViewById(R.id.selected_bg);
        btnStatistics = view.findViewById(R.id.statistics_btn);
        btnGroupList = view.findViewById(R.id.grouplist_btn);

        // 기본으로 statistics fragment 실행(추후 변경 예정)
        if (savedInstanceState == null) {
            replaceChildFragment(new StatisticsFragment());
        }

        // Statistics 클릭 이벤트
        btnStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAnimation(Gravity.START);
                btnStatistics.setTypeface(null, Typeface.BOLD);
                btnGroupList.setTypeface(null, Typeface.BOLD);

                replaceChildFragment(new StatisticsFragment());
            }
        });

        // Group List 클릭 이벤트
        btnGroupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAnimation(Gravity.END);
                btnGroupList.setTypeface(null, Typeface.BOLD);
                btnStatistics.setTypeface(null, Typeface.BOLD);

                replaceChildFragment(new GroupListFragment());
            }
        });

        // 프로젝트 페이지 이동 버튼
        view.findViewById(R.id.btn_open_project_page).setOnClickListener(v -> {
            // TODO: 프로젝트 목록에서 선택한 실제 project ID를 전달하세요
            String selectedProjectId = null; // 예: "uuid-from-project-list"
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment,
                            ProjectPageFragment.newInstance(selectedProjectId))
                    .addToBackStack(null)
                    .commit();
        });

    }

    // 애니메이션 처리를 위한 별도 메소드
    private void playAnimation(int gravity) {
        // 애니메이션 설정
        ChangeBounds transition = new ChangeBounds();
        transition.setDuration(250);

        // Transition 실행
        TransitionManager.beginDelayedTransition(toggleContainer, transition);

        // 위치(Gravity) 변경
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) selectedBg.getLayoutParams();
        params.gravity = gravity;
        selectedBg.setLayoutParams(params);
    }

    private void replaceChildFragment(Fragment fragment) {
        // fragment 안 fragment 변경
        getChildFragmentManager().beginTransaction()
                .replace(R.id.project_fragment, fragment)
                .setTransition(androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 메모리 누수 방지
    }
}