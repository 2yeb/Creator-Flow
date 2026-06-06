package com.example.creator_flow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.creator_flow.databinding.FragmentHomeBinding;
import com.example.creator_flow.databinding.FragmentSimulationBinding;

import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    // 항목 변수
    private final int TYPE_PRICE = 0;   // 단가
    private final int TYPE_QUANTITY = 1;   //수량
    private final int TYPE_SUB_MATERIAL = 2;   // 부자재값
    private final int TYPE_FEE = 3;     // 수수료
    private final int TYPE_TARGET_PROFIT = 4;   // 목표 순이익

    // 계산용 변수
    private long price = 0, subMaterial = 0, targetProfit = 0;
    private int quantity = 0;
    private double fee = 0;

    // 뷰 선언
    private TextView InputLabel;
    private EditText InputEdit;
    private LinearLayout linearInputContainer;  // inputedit
    private Spinner feeSpinner;     // 수수료 스피너
    private TextView tvPrice, tvQuantity, tvSubMaterial, tvFee, tvTarget;
    private View cardDescription;
    private TextView titleDescription, bodyDescription;

    private int currentSelectedType = TYPE_PRICE; // 기본 선택을 단가로
    private DecimalFormat decimalFormat = new DecimalFormat("#,###"); // 콤마 포맷터

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뷰 초기화
        InputLabel = view.findViewById(R.id.input_label);
        InputEdit = view.findViewById(R.id.input_edit);
        linearInputContainer = view.findViewById(R.id.input_container_linear);
        feeSpinner = view.findViewById(R.id.fee_spinner);

        tvPrice = view.findViewById(R.id.price_tv);
        tvQuantity = view.findViewById(R.id.quantity_tv);

        tvSubMaterial = view.findViewById(R.id.sub_material_tv);
        tvFee = view.findViewById(R.id.fee_tv);
        tvTarget = view.findViewById(R.id.target_tv);

        cardDescription = view.findViewById(R.id.description_card);
        titleDescription = view.findViewById(R.id.description_title);
        bodyDescription = view.findViewById(R.id.description_body);

        // 클릭 리스너 설정
        View.OnClickListener rowClickListener = v -> {
            int id = v.getId();

            // 기본 상태 -> inputcontainer visible
            linearInputContainer.setVisibility(View.VISIBLE);
            feeSpinner.setVisibility(View.INVISIBLE);

            if (id == R.id.price_btn) {
                currentSelectedType = TYPE_PRICE; // TYPE_PRICE
                InputLabel.setText("단가");
            } else if (id == R.id.quantity_btn) {
                currentSelectedType = TYPE_QUANTITY; // TYPE_QUANTITY
                InputLabel.setText("수량");
            } else if (id == R.id.sub_material_btn) {
                currentSelectedType = TYPE_SUB_MATERIAL; // TYPE_SUB_MATERIAL
                InputLabel.setText("부자재비");
            } else if (id == R.id.fee_btn) {
                currentSelectedType = TYPE_FEE; // TYPE_FEE
                InputLabel.setText("수수료율");
                // 수수료율 클릭시 spinner visible 상태로 변경
                linearInputContainer.setVisibility(View.INVISIBLE);
                feeSpinner.setVisibility(View.VISIBLE);
            } else if (id == R.id.target_btn) {
                currentSelectedType = TYPE_TARGET_PROFIT; // TYPE_TARGET_PROFIT
                InputLabel.setText("목표순이익");
            }

            InputEdit.setText("");
            InputEdit.requestFocus();
        };

        // 리스너 연결
        view.findViewById(R.id.price_btn).setOnClickListener(rowClickListener);
        view.findViewById(R.id.quantity_btn).setOnClickListener(rowClickListener);
        view.findViewById(R.id.sub_material_btn).setOnClickListener(rowClickListener);
        view.findViewById(R.id.fee_btn).setOnClickListener(rowClickListener);
        view.findViewById(R.id.target_btn).setOnClickListener(rowClickListener);

        // 스피너 아이템 선택 이벤트 처리
        feeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (currentSelectedType != TYPE_FEE) return;

                String selectedItem = parent.getItemAtPosition(position).toString();

                if (selectedItem.equals("직접 입력")) {
                    // '직접 입력' 누르면 스피너 숨기고 에딧텍스트 표시
                    feeSpinner.setVisibility(View.INVISIBLE);
                    linearInputContainer.setVisibility(View.VISIBLE);
                    InputEdit.setText("");
                    InputEdit.requestFocus();
                } else {
                    // 숫자만 추출 (ex: "15%" -> "15")
                    String cleanString = selectedItem.replaceAll("[^0-9]", "");
                    saveValue(cleanString);
                    updateRowText(cleanString);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // TextWatcher 설정
        InputEdit.addTextChangedListener(new TextWatcher() {
            String result = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s.toString()) && !s.toString().equals(result)) {
                    String cleanString = s.toString().replaceAll("[^0-9]", "");
                    saveValue(cleanString);

                    double parsed = Double.parseDouble(cleanString);
                    result = decimalFormat.format(parsed);
                    InputEdit.setText(result);
                    InputEdit.setSelection(result.length());

                    updateRowText(result);
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // 계산 버튼
        binding.calculateBtn.setOnClickListener(v -> {
            calculateFinalResult();
        });

        // 물음표 클릭 이벤트(부자재비)
        view.findViewById(R.id.help_sub_material_btn).setOnClickListener(v -> {
            showDescription("부자재비", "포장지, 테이프, 택배 박스 등 소모품 비용입니다.");
        });

        // 물음표 클릭 이벤트(수수료율)
        view.findViewById(R.id.help_fee_btn).setOnClickListener(v -> {
            showDescription("수수료율", "판매 플랫폼의 수수료입니다.");
        });

        // 초기화 버튼 찾기 및 리스너 연결
        view.findViewById(R.id.reset_btn).setOnClickListener(v -> {
            ResetData();
            binding.priceReceipt.setVisibility(View.INVISIBLE);

            InputEdit.requestFocus();
        });

    }

    // private void updateInput(String ) 만들지 고민 중

    // 화면 업데이트
    private void updateRowText(String value) {
        switch (currentSelectedType) {
            case 0: tvPrice.setText(value); break;
            case 1: tvQuantity.setText(value); break;
            case 2: tvSubMaterial.setText(value); break;
            case 3: tvFee.setText(value + " %"); break;
            case 4: tvTarget.setText(value); break;
        }
    }

    // 값 저장 로직
    private void saveValue(String cleanString) {
        long val = cleanString.isEmpty() ? 0 : Long.parseLong(cleanString);
        switch (currentSelectedType) {
            case TYPE_PRICE: price = val; break;
            case TYPE_QUANTITY: quantity = (int) val; break;
            case TYPE_SUB_MATERIAL: subMaterial = val; break;
            case TYPE_FEE: fee = (double) val; break;
            case TYPE_TARGET_PROFIT: targetProfit = val; break;
        }
    }

    // 최종 계산 및 영수증 표시(db 연결로 구현 변경 예정)
    private void calculateFinalResult() {
        // 부자재비 제외 필수값 체크
        if (price == 0 || quantity == 0 || fee == 0 || targetProfit == 0) {
            return;
        }
        // 수수료가 100% 이상일 때 분모가 0이 되거나 음수가 되는 현상 방지
        if (fee >= 100.0) {
            return;
        }

        // 공식 적용 (역산: 정가 = (원가합계) / (1 - 수수료율))
        double totalRequired = (double)(price * quantity) + subMaterial + targetProfit;
        double finalPrice = (totalRequired / (1 - (fee / 100.0))) / quantity;

        // 결과 반영
        binding.pullPriceTv.setText(decimalFormat.format(Math.round(finalPrice)) + "원");
        binding.priceReceipt.setVisibility(View.VISIBLE); // 영수증 보이게 하기
    }

    // 설명칸 클릭 이벤트
    private void showDescription(String title, String body) {
        // 이미 같은 설명이 보이고 있다면 닫기 (토글 기능)
        if (cardDescription.getVisibility() == View.VISIBLE && titleDescription.getText().equals(title)) {
            cardDescription.setVisibility(View.GONE);
        } else {
            // 내용 셋팅 후 보여주기
            titleDescription.setText(title);
            bodyDescription.setText(body);
            cardDescription.setVisibility(View.VISIBLE);
        }
    }

    // home 화면 내 계산기 내용 모두 리셋
    private void ResetData() {
        // 모든 변수, 텍스트 초기화
        InputEdit.setText("");
        InputLabel.setText("단가");
        currentSelectedType = 0;
        price = 0;
        quantity = 0;
        subMaterial = 0;
        fee = 0;
        targetProfit = 0;

        // 모든 텍스트뷰 초기화
        tvPrice.setText("");
        tvQuantity.setText("");
        tvSubMaterial.setText("");
        tvFee.setText("");
        tvTarget.setText("");
        binding.pullPriceTv.setText("");
    }

    @Override
    // 메모리 누수 방지
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}