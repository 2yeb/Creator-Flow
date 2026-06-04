package com.example.creator_flow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SimulationConfirmFragment extends Fragment {

    // 앞 화면들로부터 전달받은 모델 타입을 보관할 변수
    private String modelType = "Business";

    // [변경 확인] 비즈니스 모델일 때만 제어할 두 개의 레이아웃 변수
    private View linearProfitContainer;
    private View linearMarginContainer;

    public SimulationConfirmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 번들에서 담아온 데이터 꺼내기
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_confirm, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. 좌상단 뒤로가기 버튼 설정
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 3. [변경 확인] 요청하신 두 가지 내용이 담겨있는 레이아웃 두 개 찾아오기
        linearProfitContainer = view.findViewById(R.id.profit_container_linear); // 희망 순이익 레이아웃
        linearMarginContainer = view.findViewById(R.id.margin_container_linear); // 희망 마진율 레이아웃

        // 4. [조건 분기] 모델 타입에 따라 두 레이아웃의 상태를 동시 제어
        if ("Business".equals(modelType)) {
            // 비즈니스 모델인 경우: 두 레이아웃을 모두 보여줍니다.
            if (linearProfitContainer != null) linearProfitContainer.setVisibility(View.VISIBLE);
            if (linearMarginContainer != null) linearMarginContainer.setVisibility(View.VISIBLE);
        } else {
            // 팬아터(or 그 외) 모델인 경우: 요청하신 두 가지 레이아웃을 모두 GONE 상태로 만듭니다.
            if (linearProfitContainer != null) linearProfitContainer.setVisibility(View.GONE);
            if (linearMarginContainer != null) linearMarginContainer.setVisibility(View.GONE);
        }

        // 5. 우하단 '확인' 버튼 클릭 이벤트
        View btnConfirm = view.findViewById(R.id.confirm_btn); // 혹은 XML 내 확인 버튼 ID
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                // TODO: 시뮬레이션 종료 후 새로 생성된 프로젝트 페이지로 연결되고, 데이터를 db에 연동하는 코드 작성
            });
        }

        // 데이터 연결부
    }
}