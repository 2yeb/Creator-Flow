package com.example.creator_flow;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.creator_flow.model.SimulationData;

public class SimulationDetail2Fragment extends Fragment {

    // ===== 모델 타입 인자 (Business / Fan-art) =====
    private static final String ARG_MODEL_TYPE = "model_type";

    private boolean isFanart;
    private View nextButton;
    private EditText edtProfit;     // Business 전용
    private EditText edtMargin;     // Business 전용
    private EditText edtTarget;     // 공통
    private EditText edtMinStock;   // 공통

    public SimulationDetail2Fragment() {}

    public static SimulationDetail2Fragment newInstance() {
        return newInstance(SimulationDetail1Fragment.MODEL_BUSINESS);
    }

    public static SimulationDetail2Fragment newInstance(String modelType) {
        SimulationDetail2Fragment fragment = new SimulationDetail2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODEL_TYPE, modelType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_detail2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뒤로가기
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 다음 버튼 → SimulationBusinessFragment 이동
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(v -> goToBusiness());

        // 모델 타입 확인
        String modelType = getArguments() != null
                ? getArguments().getString(ARG_MODEL_TYPE, SimulationDetail1Fragment.MODEL_BUSINESS)
                : SimulationDetail1Fragment.MODEL_BUSINESS;
        isFanart = SimulationDetail1Fragment.MODEL_FANART.equals(modelType);

        // 헤더 텍스트
        TextView header = view.findViewById(R.id.tv_model_header);
        header.setText(isFanart ? "Fan-art Model" : "Business Model");

        // Business 전용 카드는 Fan-art일 때 숨김
        view.findViewById(R.id.card_profit).setVisibility(isFanart ? View.GONE : View.VISIBLE);
        view.findViewById(R.id.card_margin).setVisibility(isFanart ? View.GONE : View.VISIBLE);

        // 입력 필드 참조 + TextWatcher (다음 버튼 표시 조건 갱신)
        edtProfit = view.findViewById(R.id.edt_profit);
        edtMargin = view.findViewById(R.id.edt_margin);
        edtTarget = view.findViewById(R.id.edt_target);
        edtMinStock = view.findViewById(R.id.edt_min_stock);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { updateNextButton(); }
        };
        edtProfit.addTextChangedListener(watcher);
        edtMargin.addTextChangedListener(watcher);
        edtTarget.addTextChangedListener(watcher);
        edtMinStock.addTextChangedListener(watcher);
    }

    private void goToBusiness() {
        // 입력값을 SimulationData에 저장
        SimulationData.targetQuantity = parseIntOrNull(edtTarget.getText().toString());
        SimulationData.minStock = parseIntOrNull(edtMinStock.getText().toString());
        if (!isFanart) {
            SimulationData.profit = parseIntOrNull(edtProfit.getText().toString());
            SimulationData.marginRate = parseIntOrNull(edtMargin.getText().toString());
        } else {
            SimulationData.profit = null;
            SimulationData.marginRate = null;
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment,
                        SimulationBusinessFragment.newInstance(isFanart
                                ? SimulationDetail1Fragment.MODEL_FANART
                                : SimulationDetail1Fragment.MODEL_BUSINESS))
                .addToBackStack(null)
                .commit();
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }

    /** 모델에 따라 다른 필드 조합을 검사해서 다음 버튼 노출 */
    private void updateNextButton() {
        if (nextButton == null) return;
        boolean allFilled;
        if (isFanart) {
            // Fan-art: 목표 판매량 + 최소 재고량만
            allFilled = !TextUtils.isEmpty(edtTarget.getText())
                    && !TextUtils.isEmpty(edtMinStock.getText());
        } else {
            // Business: 4개 모두
            allFilled = !TextUtils.isEmpty(edtProfit.getText())
                    && !TextUtils.isEmpty(edtMargin.getText())
                    && !TextUtils.isEmpty(edtTarget.getText())
                    && !TextUtils.isEmpty(edtMinStock.getText());
        }
        nextButton.setVisibility(allFilled ? View.VISIBLE : View.GONE);
    }
}
