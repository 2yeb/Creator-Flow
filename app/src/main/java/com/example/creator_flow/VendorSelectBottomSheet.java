package com.example.creator_flow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

public class VendorSelectBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "VendorSelectBottomSheet";

    public interface OnVendorSelectedListener {
        /** 사용자가 "선택" 버튼을 눌러 확정한 vendor 키 */
        void onVendorSelected(String vendorKey);
    }

    private OnVendorSelectedListener listener;

    /** 현재 선택된 카드 키 (null = 아무것도 선택 안 됨) */
    private String selectedKey = null;

    private MaterialCardView cardRedprinting;
    private MaterialCardView cardPrintbyme;
    private MaterialCardView cardGrafolio;

    public void setOnVendorSelectedListener(OnVendorSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_vendor_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_close_vendor_sheet).setOnClickListener(v -> dismiss());

        cardRedprinting = view.findViewById(R.id.card_redprinting);
        cardPrintbyme = view.findViewById(R.id.card_printbyme);
        cardGrafolio = view.findViewById(R.id.card_grafolio);

        // 카드 클릭 = 선택 상태만 변경 (바텀시트는 안 닫힘)
        cardRedprinting.setOnClickListener(v -> selectCard("redprinting"));
        cardPrintbyme.setOnClickListener(v -> selectCard("printbyme"));
        cardGrafolio.setOnClickListener(v -> selectCard("grafolio"));

        // "선택" 버튼 = 해당 카드가 이미 선택된 상태일 때만 확정 + dismiss
        view.findViewById(R.id.btn_select_redprinting)
                .setOnClickListener(v -> confirmIfSelected("redprinting"));
        view.findViewById(R.id.btn_select_printbyme)
                .setOnClickListener(v -> confirmIfSelected("printbyme"));
        view.findViewById(R.id.btn_select_grafolio)
                .setOnClickListener(v -> confirmIfSelected("grafolio"));
    }

    private void selectCard(String key) {
        selectedKey = key;
        // setSelected는 자식 View에도 자동 전파됨 (선택 버튼 색·텍스트도 같이 변함)
        cardRedprinting.setSelected("redprinting".equals(key));
        cardPrintbyme.setSelected("printbyme".equals(key));
        cardGrafolio.setSelected("grafolio".equals(key));
    }

    private void confirmIfSelected(String key) {
        if (key.equals(selectedKey)) {
            if (listener != null) listener.onVendorSelected(selectedKey);
            dismiss();
        }
        // 선택 안 된 카드의 "선택" 버튼은 아무 반응 없음
    }
}
