package com.example.creator_flow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimulationPlatformFragment extends Fragment {

    /** 판매 플랫폼 정의 */
    private static class Platform {
        final String name;
        final String logoLetter;
        final int defaultFee;
        final boolean isDirectInput;

        Platform(String name, String logoLetter, int defaultFee, boolean isDirectInput) {
            this.name = name;
            this.logoLetter = logoLetter;
            this.defaultFee = defaultFee;
            this.isDirectInput = isDirectInput;
        }
    }

    private static final List<Platform> PLATFORMS = Arrays.asList(
            new Platform("윗치폼", "W", 5, false),
            new Platform("텀블벅", "T", 5, false),
            new Platform("TMM", "T", 5, false),
            new Platform("스마트 스토어", "S", 5, false),
            new Platform("무통장", "M", 5, false),
            new Platform("직접 입력", "직", 0, true)
    );

    private final List<View> cardViews = new ArrayList<>();
    private int selectedIndex = -1;
    private View nextButton;

    public SimulationPlatformFragment() {}

    public static SimulationPlatformFragment newInstance() {
        return new SimulationPlatformFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_platform, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(v -> goToDelivery());

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        populateGrid(view);
    }

    // ============= 그리드 생성 =============
    private void populateGrid(View root) {
        LinearLayout rowsContainer = root.findViewById(R.id.platform_rows);
        rowsContainer.removeAllViews();
        cardViews.clear();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // 2개씩 묶어서 3행으로 배치
        for (int i = 0; i < PLATFORMS.size(); i += 2) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) rowLp.topMargin = dp(12);
            row.setLayoutParams(rowLp);

            for (int j = 0; j < 2 && (i + j) < PLATFORMS.size(); j++) {
                View card = inflater.inflate(R.layout.item_platform_card, row, false);
                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(0, dp(135), 1f);
                if (j == 1) cardLp.leftMargin = dp(12);
                card.setLayoutParams(cardLp);

                bindCard(card, PLATFORMS.get(i + j), i + j);
                cardViews.add(card);
                row.addView(card);
            }
            rowsContainer.addView(row);
        }
    }

    private void bindCard(View card, Platform p, int index) {
        ((TextView) card.findViewById(R.id.tv_logo_letter)).setText(p.logoLetter);
        ((TextView) card.findViewById(R.id.tv_platform_name)).setText(p.name);

        TextView tvFee = card.findViewById(R.id.tv_fee);
        View feeInputRow = card.findViewById(R.id.fee_input_row);
        EditText etFee = card.findViewById(R.id.et_fee);

        if (p.isDirectInput) {
            tvFee.setVisibility(View.GONE);
            feeInputRow.setVisibility(View.VISIBLE);
            // 초기 표시: 비어 있어서 hint "0" 보임 → "0%"로 렌더링됨
            etFee.setText("");

            // 사용자가 EditText를 직접 탭해도 카드 선택 처리
            etFee.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) selectIndex(index);
            });
        } else {
            tvFee.setVisibility(View.VISIBLE);
            tvFee.setText(p.defaultFee + "%");
            feeInputRow.setVisibility(View.GONE);
        }

        card.setOnClickListener(v -> {
            selectIndex(index);
            if (p.isDirectInput) {
                etFee.requestFocus();
                showKeyboard(etFee);
            } else {
                hideKeyboard(v);
            }
        });
    }

    private void selectIndex(int index) {
        if (selectedIndex == index) return;
        selectedIndex = index;
        for (int i = 0; i < cardViews.size(); i++) {
            cardViews.get(i).setSelected(i == index);
        }
        nextButton.setVisibility(View.VISIBLE);
    }

    // ============= 다음 화면 이동 =============
    private void goToDelivery() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, new SimulationDeliveryFragment())
                .addToBackStack(null)
                .commit();
    }

    // ============= 키보드 =============
    private void showKeyboard(View target) {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View anyView) {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(anyView.getWindowToken(), 0);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
