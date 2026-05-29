package com.example.creator_flow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.creator_flow.model.SimulationData;

public class SimulationDeliveryFragment extends Fragment {

    // ===== 모델 타입 인자 (Business / Fan-art) =====
    private static final String ARG_MODEL_TYPE = "model_type";

    // 라디오 그룹별 현재 선택된 View
    private View selectedDelivery;
    private View selectedFee;
    private View selectedPackaging;
    private View nextButton;
    /** 현재 화면이 표현 중인 모델 타입 (다음 화면으로 전달) */
    private String currentModelType = SimulationDetail1Fragment.MODEL_BUSINESS;

    public SimulationDeliveryFragment() {}

    public static SimulationDeliveryFragment newInstance() {
        return newInstance(SimulationDetail1Fragment.MODEL_BUSINESS);
    }

    public static SimulationDeliveryFragment newInstance(String modelType) {
        SimulationDeliveryFragment fragment = new SimulationDeliveryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODEL_TYPE, modelType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_delivery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뒤로가기
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 다음 버튼 (3개 선택 완료 시 표시) → SimulationDetail2Fragment 이동
        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(v -> goToDetail2());

        // 모델 헤더: Business / Fan-art 분기
        currentModelType = getArguments() != null
                ? getArguments().getString(ARG_MODEL_TYPE, SimulationDetail1Fragment.MODEL_BUSINESS)
                : SimulationDetail1Fragment.MODEL_BUSINESS;
        TextView tvModelHeader = view.findViewById(R.id.tv_model_header);
        if (tvModelHeader != null) {
            tvModelHeader.setText(
                    SimulationDetail1Fragment.MODEL_FANART.equals(currentModelType)
                            ? "Fan-art Model" : "Business Model");
        }

        setupDeliveryRadio(view);
        setupFeeRadio(view);
        setupPackagingRadio(view);
    }

    // ===== 택배 라디오 =====
    private void setupDeliveryRadio(View root) {
        int[] ids = {
                R.id.opt_post,
                R.id.opt_cvs,
                R.id.opt_cvs_half,
                R.id.opt_visit,
                R.id.opt_post_mail
        };
        for (int id : ids) {
            View row = root.findViewById(id);
            row.setOnClickListener(v -> {
                if (selectedDelivery != null) selectedDelivery.setSelected(false);
                v.setSelected(true);
                selectedDelivery = v;
                updateNextButton();
            });
        }
    }

    // ===== 배송비 라디오 =====
    private void setupFeeRadio(View root) {
        View feeBuyer = root.findViewById(R.id.fee_buyer);
        View feeSeller = root.findViewById(R.id.fee_seller);
        View buyerInputRow = root.findViewById(R.id.fee_buyer_input_row);
        EditText buyerAmount = root.findViewById(R.id.et_fee_buyer_amount);

        feeBuyer.setOnClickListener(v -> {
            if (selectedFee != null) selectedFee.setSelected(false);
            v.setSelected(true);
            selectedFee = v;
            // 구매자 부담 → 금액 입력 칸 노출 + 포커스
            buyerInputRow.setVisibility(View.VISIBLE);
            buyerAmount.requestFocus();
            showKeyboard(buyerAmount);
            updateNextButton();
        });

        feeSeller.setOnClickListener(v -> {
            if (selectedFee != null) selectedFee.setSelected(false);
            v.setSelected(true);
            selectedFee = v;
            // 판매자 택배 → 금액 입력 칸 숨김
            buyerInputRow.setVisibility(View.GONE);
            hideKeyboard(v);
            updateNextButton();
        });

        // EditText 직접 탭해도 구매자 부담 행 선택 처리
        buyerAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && selectedFee != feeBuyer) {
                if (selectedFee != null) selectedFee.setSelected(false);
                feeBuyer.setSelected(true);
                selectedFee = feeBuyer;
                updateNextButton();
            }
        });
    }

    // ===== 포장재 라디오 + 직접 입력 =====
    private void setupPackagingRadio(View root) {
        TextView opp = root.findViewById(R.id.pkg_opp);
        TextView zipper = root.findViewById(R.id.pkg_zipper);
        TextView none = root.findViewById(R.id.pkg_none);
        TextView custom = root.findViewById(R.id.pkg_custom);
        EditText customInput = root.findViewById(R.id.pkg_custom_input);

        TextView[] chips = {opp, zipper, none, custom};
        for (TextView chip : chips) {
            chip.setOnClickListener(v -> {
                if (selectedPackaging != null) selectedPackaging.setSelected(false);
                v.setSelected(true);
                selectedPackaging = v;

                // 직접 입력 칩 → EditText 표시 + 포커스
                if (v.getId() == R.id.pkg_custom) {
                    customInput.setVisibility(View.VISIBLE);
                    customInput.requestFocus();
                    showKeyboard(customInput);
                } else {
                    customInput.setVisibility(View.GONE);
                    hideKeyboard(v);
                }
                updateNextButton();
            });
        }

        // EditText 직접 탭해도 직접 입력 칩 선택 처리
        customInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (selectedPackaging != null && selectedPackaging != custom) {
                    selectedPackaging.setSelected(false);
                }
                custom.setSelected(true);
                selectedPackaging = custom;
                updateNextButton();
            }
        });
    }

    /** 3개 섹션 모두 선택돼야 다음 버튼 노출 */
    private void updateNextButton() {
        if (nextButton == null) return;
        boolean allSelected = selectedDelivery != null
                && selectedFee != null
                && selectedPackaging != null;
        nextButton.setVisibility(allSelected ? View.VISIBLE : View.GONE);
    }

    private void goToDetail2() {
        // 선택값 저장
        if (selectedDelivery instanceof View && selectedDelivery != null) {
            TextView label = findOptionLabel((View) selectedDelivery);
            SimulationData.deliveryMethod = (label != null) ? label.getText().toString() : null;
        }
        if (selectedFee != null) {
            TextView label = findOptionLabel((View) selectedFee);
            SimulationData.shippingFeeType = (label != null) ? label.getText().toString() : null;

            // 구매자 부담이면 입력한 금액도 저장 (블랭크/invalid 시 0)
            if (selectedFee.getId() == R.id.fee_buyer) {
                View root = getView();
                if (root != null) {
                    EditText amount = root.findViewById(R.id.et_fee_buyer_amount);
                    String text = (amount != null) ? amount.getText().toString().trim() : "";
                    try {
                        SimulationData.shippingFeeBuyer = text.isEmpty() ? 0 : Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        SimulationData.shippingFeeBuyer = 0;
                    }
                }
            } else {
                // 판매자 택배 → 구매자는 0원 (판매자가 다 부담)
                SimulationData.shippingFeeBuyer = 0;
            }
        }
        if (selectedPackaging instanceof TextView) {
            // 직접 입력이면 EditText 값 사용, 아니면 칩 텍스트
            View root = getView();
            if (selectedPackaging.getId() == R.id.pkg_custom && root != null) {
                EditText input = root.findViewById(R.id.pkg_custom_input);
                String custom = (input != null) ? input.getText().toString().trim() : "";
                SimulationData.packagingType = custom.isEmpty() ? "직접 입력" : custom;
            } else {
                SimulationData.packagingType = ((TextView) selectedPackaging).getText().toString();
            }
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment,
                        SimulationDetail2Fragment.newInstance(currentModelType))
                .addToBackStack(null)
                .commit();
    }

    /** 택배/배송비 행 안의 옵션 명 TextView를 찾기 (DeliveryOptionText 스타일 사용) */
    private TextView findOptionLabel(View row) {
        if (row instanceof android.view.ViewGroup) {
            android.view.ViewGroup vg = (android.view.ViewGroup) row;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    // weight=1로 길게 늘어난 옵션 명 TextView 식별
                    android.view.ViewGroup.LayoutParams lp = tv.getLayoutParams();
                    if (lp instanceof android.widget.LinearLayout.LayoutParams
                            && ((android.widget.LinearLayout.LayoutParams) lp).weight > 0) {
                        return tv;
                    }
                }
            }
        }
        return null;
    }

    // ===== 키보드 =====
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
}
