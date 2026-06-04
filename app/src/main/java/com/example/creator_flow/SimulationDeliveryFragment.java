package com.example.creator_flow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SimulationDeliveryFragment extends Fragment {

    private String modelType = "Business";

    // 각 카드뷰 그룹별 선택된 인덱스 (-1은 미선택)
    private int selectedDeliveryIndex = -1; // 1. 택배 그룹 (총 5개 박스)
    private int selectedFeeIndex = -1;      // 2. 배송비 그룹 (총 2개 박스)
    private int selectedPackingIndex = -1;  // 3. 포장재 그룹 (총 4개 박스)

    // 실제 클릭을 감지하고 배경이 바뀔 박스(View) 레이아웃 배열
    private LinearLayout[] deliveryBoxes;
    private LinearLayout[] feeBoxes;
    private TextView[] packingBoxes;

    private ImageView nextButton;

    public SimulationDeliveryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_delivery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            modelType = getArguments().getString("model_type", "Business");
        }

        // 뒤로가기 및 다음 버튼 초기화
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        nextButton = view.findViewById(R.id.btn_next);
        if (nextButton != null) {
            nextButton.setVisibility(View.GONE);
            nextButton.setOnClickListener(v -> goToNextStep());
        }

        // ==========================================
        // 그룹 1. 택배 박스 리스트 (실제 감싸고 있는 레이아웃 ID 매핑)
        // ==========================================
        deliveryBoxes = new LinearLayout[]{
                view.findViewById(R.id.post_office),    // 우체국 택배
                view.findViewById(R.id.convenious_store),   // 편의점 택배
                view.findViewById(R.id.convenious_half),     // 편의점 반값 택배
                view.findViewById(R.id.post_basic),     // 방문 택배
                view.findViewById(R.id.postal)     // 준등기 / 우편
        };
        setupGroupClickListeners(deliveryBoxes, 1);

        // ==========================================
        // 그룹 2. 배송비 박스 리스트 (실제 감싸고 있는 레이아웃 ID 매핑)
        // ==========================================
        feeBoxes = new LinearLayout[]{
                view.findViewById(R.id.fee_include),    // 구매자 부담
                view.findViewById(R.id.fee_exclude)     // 판매자 택배
        };
        setupGroupClickListeners(feeBoxes, 2);

        // ==========================================
        // 그룹 3. 포장재 박스 리스트 (실제 감싸고 있는 레이아웃 ID 매핑)
        // ==========================================
        packingBoxes = new TextView[]{
                view.findViewById(R.id.pack_opp_tv),       // OPP 봉투 레이아웃
                view.findViewById(R.id.pack_zipper_tv),    // 지퍼백 레이아웃
                view.findViewById(R.id.pack_none_tv),      // 없음 레이아웃
                view.findViewById(R.id.pack_custom_tv)     // 직접 입력 레이아웃
        };
        setupGroupClickListeners(packingBoxes, 3);
    }

    /**
     * 박스(레이아웃) 배열에 클릭 리스너를 달아주는 함수
     */
    private void setupGroupClickListeners(View[] boxes, int groupType) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] == null) continue;

            final int index = i;
            boxes[i].setOnClickListener(v -> {
                if (groupType == 1) {
                    selectedDeliveryIndex = index;
                    updateGroupUI(boxes, selectedDeliveryIndex);
                } else if (groupType == 2) {
                    selectedFeeIndex = index;
                    updateGroupUI(boxes, selectedFeeIndex);
                } else if (groupType == 3) {
                    selectedPackingIndex = index;
                    updateGroupUI(boxes, selectedPackingIndex);
                }

                // 세 카드뷰가 모두 선택되었는지 매번 검사해서 다음 버튼 노출
                checkAllCategoriesSelected();
            });
        }
    }

    /**
     * 클릭된 박스 영역만 selected 상태를 true로 켜고 나머지는 끄는 함수
     */
    private void updateGroupUI(View[] boxes, int selectedIndex) {
        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null) {
                boxes[i].setSelected(i == selectedIndex);
            }
        }
    }

    private void checkAllCategoriesSelected() {
        if (selectedDeliveryIndex != -1 && selectedFeeIndex != -1 && selectedPackingIndex != -1) {
            if (nextButton != null) nextButton.setVisibility(View.VISIBLE);
        } else {
            if (nextButton != null) nextButton.setVisibility(View.GONE);
        }
    }

    private void goToNextStep() {
        if (selectedDeliveryIndex == -1 || selectedFeeIndex == -1 || selectedPackingIndex == -1) return;

        Fragment nextFragment;
        if ("Business".equals(modelType)) {
            nextFragment = new SimulationBusinessFragment();
        } else {
            nextFragment = new SimulationConfirmFragment();
        }

        Bundle args = new Bundle();
        args.putString("model_type", modelType);
        nextFragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, nextFragment)
                .addToBackStack(null)
                .commit();
    }
}