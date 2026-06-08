package com.example.creator_flow;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.creator_flow.model.VendorInfo;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 업체 선택 바텀시트. SimulationDetail1Fragment에서 필터링된 vendor 리스트를
 * setVendors()로 전달받아 카드를 동적으로 생성한다.
 */
public class VendorSelectBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "VendorSelectBottomSheet";

    public interface OnVendorSelectedListener {
        /** "선택" 버튼을 눌러 확정한 vendor 이름 */
        void onVendorSelected(String vendorName);
    }

    /** 바텀시트에서 옵션 chip이 바뀌었음을 detail1에 알리는 콜백 (dismiss 시 호출). */
    public interface OnOptionsChangedListener {
        void onOptionsChanged();
    }

    private OnVendorSelectedListener listener;
    private OnOptionsChangedListener optionsChangedListener;
    /** 바텀시트가 열려있는 동안 옵션이 변경됐는지 추적 — dismiss 시 콜백 발사 여부 결정 */
    private boolean optionsModified = false;
    private List<VendorInfo> vendors = Collections.emptyList();
    private String subType;
    private Map<String, String> selectedOptions = new LinkedHashMap<>();
    /** 상단 필터 칩에 표시할 텍스트들 (예: "판스티커", "100개") */
    private List<String> filterChips = new ArrayList<>();

    /** 현재 선택된 카드의 vendor 이름 (null = 아무것도 선택 안 됨) */
    private String selectedVendorName = null;
    private final List<MaterialCardView> cardViews = new ArrayList<>();
    /** cardViews와 인덱스가 일치하는 vendor 리스트 — chip 클릭 후 옵션 행 재렌더링용 */
    private final List<VendorInfo> cardVendors = new ArrayList<>();
    /**
     * 각 카드별 독립 옵션 state (cardViews와 인덱스 매칭).
     * 초기값은 detail1의 selectedOptions를 복제. 카드별로 chip을 따로 바꿔도 다른 카드에 영향 X.
     * "선택" 버튼 클릭 시에만 해당 카드의 state를 detail1의 selectedOptions로 commit.
     */
    private final List<Map<String, String>> cardOptionMaps = new ArrayList<>();

    public void setOnVendorSelectedListener(OnVendorSelectedListener listener) {
        this.listener = listener;
    }

    /** detail1이 바텀시트의 옵션 변경을 감지해서 자기 화면을 다시 그릴 수 있게 한다. */
    public void setOnOptionsChangedListener(OnOptionsChangedListener l) {
        this.optionsChangedListener = l;
    }

    /** detail1에서 필터링된 vendor 리스트 전달 */
    public void setVendors(List<VendorInfo> vendors) {
        this.vendors = vendors != null ? vendors : Collections.emptyList();
    }

    /** 현재 sub_type (옵션 chip 렌더링 시 capabilities[subType] 조회) */
    public void setSubType(String subType) {
        this.subType = subType;
    }

    /** 사용자가 detail1에서 선택한 옵션 (chip 선택 상태 반영용) */
    public void setSelectedOptions(Map<String, String> selectedOptions) {
        this.selectedOptions = selectedOptions != null
                ? selectedOptions : new LinkedHashMap<>();
    }

    /** 상단에 보일 필터 칩 텍스트들 (예: ["판스티커", "100개"]) */
    public void setFilterChips(List<String> chips) {
        this.filterChips = chips != null ? chips : new ArrayList<>();
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

        renderFilterChips(view);
        renderVendorCards(view);
    }

    // ============= 상단 필터 칩 =============
    private void renderFilterChips(View root) {
        LinearLayout container = root.findViewById(R.id.filter_chips_container);
        container.removeAllViews();
        for (int i = 0; i < filterChips.size(); i++) {
            String text = filterChips.get(i);
            TextView chip = new TextView(getContext());
            chip.setText(text);
            chip.setTextSize(13f);
            chip.setTextColor(Color.parseColor("#313131"));
            chip.setBackgroundResource(R.drawable.chip_selected);
            chip.setPadding(dp(14), dp(6), dp(14), dp(6));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) lp.leftMargin = dp(8);
            chip.setLayoutParams(lp);
            container.addView(chip);
        }
    }

    // ============= 업체 카드 동적 생성 =============
    private void renderVendorCards(View root) {
        LinearLayout container = root.findViewById(R.id.vendor_cards_container);
        container.removeAllViews();
        cardViews.clear();
        cardVendors.clear();
        cardOptionMaps.clear();

        if (vendors.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("조건에 맞는 업체가 없습니다.");
            empty.setTextColor(Color.parseColor("#9A9A9A"));
            empty.setTextSize(13f);
            empty.setPadding(0, dp(20), 0, dp(20));
            empty.setGravity(Gravity.CENTER);
            container.addView(empty);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < vendors.size(); i++) {
            VendorInfo vendor = vendors.get(i);
            View card = inflater.inflate(R.layout.item_vendor_bottom_sheet_card, container, false);
            // 카드별 옵션 state — detail1의 selectedOptions 복제 (이후엔 독립적으로 변경됨)
            cardOptionMaps.add(new LinkedHashMap<>(selectedOptions));
            cardViews.add((MaterialCardView) card);
            cardVendors.add(vendor);
            bindCard(card, vendor, i);
            container.addView(card);
        }
    }

    private void bindCard(View card, VendorInfo vendor, int cardIndex) {
        ((TextView) card.findViewById(R.id.tv_vendor_name)).setText(vendor.name);

        // 상품명 (백엔드 VendorProduct.name) — 값 있을 때만 표시
        TextView tvProduct = card.findViewById(R.id.tv_vendor_product);
        if (tvProduct != null) {
            if (vendor.productName != null && !vendor.productName.isEmpty()) {
                tvProduct.setText(vendor.productName);
                tvProduct.setVisibility(View.VISIBLE);
            } else {
                tvProduct.setVisibility(View.GONE);
            }
        }

        // 로고 박스: brandColor를 fallback, logoUrl 있으면 Glide로 로드
        android.widget.ImageView logo = card.findViewById(R.id.iv_vendor_logo);
        if (logo != null) {
            logo.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(vendor.brandColor));
            if (vendor.logoUrl != null && !vendor.logoUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                        .load(vendor.logoUrl)
                        .into(logo);
            } else {
                logo.setImageDrawable(null);
            }
        }

        // 가격 표시 — 옵션 칩 변경 시 다시 갱신할 수 있도록 헬퍼로 분리
        updateCardPriceText(card, vendor);

        // 옵션 행 동적 생성 — vendor가 지원하는 옵션을 카테고리별 chip으로 (카드별 state 사용)
        LinearLayout optionsContainer = card.findViewById(R.id.options_container);
        renderOptionRows(optionsContainer, vendor, cardIndex);

        // 카드 클릭 = 선택 상태만 변경 (바텀시트는 안 닫힘)
        card.setOnClickListener(v -> selectCard(vendor.name));

        // "선택" 버튼 = 이 카드가 선택 상태일 때만 확정 + dismiss
        // commit: 이 카드의 옵션 state를 detail1의 selectedOptions로 복사
        card.findViewById(R.id.btn_select).setOnClickListener(v -> {
            if (vendor.name.equals(selectedVendorName)) {
                Map<String, String> cardOpts = cardOptionMaps.get(cardIndex);
                if (!cardOpts.equals(selectedOptions)) {
                    selectedOptions.clear();
                    selectedOptions.putAll(cardOpts);
                    optionsModified = true;     // dismiss 시 detail1 재렌더 트리거
                }
                if (listener != null) listener.onVendorSelected(selectedVendorName);
                dismiss();
            }
        });
    }

    private void renderOptionRows(LinearLayout container, VendorInfo vendor, int cardIndex) {
        container.removeAllViews();
        if (subType == null) return;
        Map<String, Set<String>> categoryToValues = vendor.capabilities.get(subType);
        if (categoryToValues == null || categoryToValues.isEmpty()) return;

        Map<String, String> cardOpts = cardOptionMaps.get(cardIndex);

        for (Map.Entry<String, Set<String>> entry : categoryToValues.entrySet()) {
            String category = entry.getKey();
            Set<String> values = entry.getValue();

            // row를 match_parent + horizontal: 라벨 (좌측) + 칩 그룹 (남은 공간 + 자동 줄바꿈)
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            // 라벨이 상단 정렬 — 칩이 여러 줄로 펼쳐졌을 때 라벨이 첫 줄에 정렬되어 보임
            row.setGravity(Gravity.TOP);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(6);
            row.setLayoutParams(rowLp);

            // 카테고리 라벨 — 70dp로 키워서 "인쇄데이터", "제작방식" 등 4~5글자도 한 줄에 들어감
            // singleLine + maxLines=1 명시 — 라벨이 좁아도 줄바꿈 안 됨
            TextView label = new TextView(getContext());
            label.setText(category);
            label.setTextColor(Color.parseColor("#7A7A7A"));
            label.setTextSize(12f);
            label.setSingleLine(true);
            label.setMaxLines(1);
            label.setPadding(0, dp(8), dp(4), 0);
            LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(dp(70),
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            label.setLayoutParams(labelLp);
            row.addView(label);

            // 칩 그룹 — 화면 너비 초과 시 자동으로 다음 줄로 줄바꿈
            com.google.android.material.chip.ChipGroup chipGroup =
                    new com.google.android.material.chip.ChipGroup(getContext());
            chipGroup.setChipSpacingHorizontal(dp(4));
            chipGroup.setChipSpacingVertical(dp(4));
            // 0dp + weight=1 = 라벨 빼고 남은 공간 모두 차지 (줄바꿈 기준)
            LinearLayout.LayoutParams chipGroupLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            chipGroup.setLayoutParams(chipGroupLp);

            // 값 chip들 — 이 카드의 독립 state 기준
            String selectedValue = cardOpts.get(category);
            for (String value : values) {
                TextView chip = makeChip(category, value, value.equals(selectedValue), cardIndex);
                chipGroup.addView(chip);
            }
            row.addView(chipGroup);
            container.addView(row);
        }
    }

    private TextView makeChip(String category, String value, boolean selected, int cardIndex) {
        TextView chip = new TextView(getContext());
        chip.setText(value);
        chip.setTextSize(12f);
        // 한 줄 강제 — 텍스트 긴 칩이 세로로 깨지는 것 방지 ("컬러우 이어링" 같은 케이스)
        chip.setSingleLine(true);
        chip.setMaxLines(1);
        // 클릭 영역 확보 — 이전 dp(3)은 너무 작아서 MaterialCardView가 클릭 가로챔
        chip.setPadding(dp(12), dp(8), dp(12), dp(8));
        // 명시적으로 클릭 가능 — 부모 MaterialCardView의 clickable이 자식 클릭 흡수 방지
        chip.setClickable(true);
        chip.setFocusable(true);
        if (selected) {
            chip.setBackgroundResource(R.drawable.chip_selected);
            chip.setTextColor(Color.parseColor("#313131"));
        } else {
            chip.setBackgroundResource(R.drawable.chip_unselected);
            chip.setTextColor(Color.parseColor("#9A9A9A"));
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp(4);
        lp.topMargin = dp(2);
        lp.bottomMargin = dp(2);
        chip.setLayoutParams(lp);

        // 클릭 시 이 카드의 옵션 state만 갱신 + 이 카드의 옵션 행만 재렌더 (다른 카드 영향 X)
        chip.setOnClickListener(v -> {
            android.util.Log.d("VendorSheet",
                    "chip click: card=" + cardIndex + ", " + category + "=" + value);
            Map<String, String> cardOpts = cardOptionMaps.get(cardIndex);
            String prev = cardOpts.get(category);
            if (value.equals(prev)) return;
            cardOpts.put(category, value);
            rerenderCardOptionRows(cardIndex);
        });
        return chip;
    }

    /**
     * 한 카드의 옵션 chip 행 + 가격 표시를 다시 그림.
     * 가격은 cardOpts의 옵션들 × vendor.optionPrices로 단가 재계산 후
     * basePrice 업데이트 → 카드의 tv_base_price / tv_total_price 갱신.
     */
    private void rerenderCardOptionRows(int cardIndex) {
        MaterialCardView card = cardViews.get(cardIndex);
        VendorInfo vendor = cardVendors.get(cardIndex);

        // ① 단가 재계산 — cardOpts의 옵션들 × vendor.optionPrices 합산
        recalculateVendorPrice(vendor, cardOptionMaps.get(cardIndex));

        // ② 가격 텍스트 갱신
        updateCardPriceText(card, vendor);

        // ③ 옵션 칩 다시 그림
        LinearLayout optsContainer = card.findViewById(R.id.options_container);
        renderOptionRows(optsContainer, vendor, cardIndex);
    }

    /**
     * cardOpts의 선택된 옵션들의 extra_price 합산 → vendor.basePrice 업데이트 (× quantity).
     * vendor.optionPrices가 null이면 (mock 모드) skip.
     */
    private void recalculateVendorPrice(VendorInfo vendor, Map<String, String> cardOpts) {
        if (vendor.optionPrices == null || vendor.optionPrices.isEmpty()) return;
        int unitCost = 0;
        for (Map.Entry<String, String> e : cardOpts.entrySet()) {
            Map<String, Integer> valueToPrice = vendor.optionPrices.get(e.getKey());
            if (valueToPrice == null) continue;
            Integer price = valueToPrice.get(e.getValue());
            if (price != null) unitCost += price;
        }
        vendor.basePrice = unitCost * vendor.quantity;
    }

    /** 카드의 가격 텍스트 3개(base / shipping / total) 갱신 — bindCard + rerenderCardOptionRows 공통 사용 */
    private void updateCardPriceText(View card, VendorInfo vendor) {
        TextView tvBase = card.findViewById(R.id.tv_base_price);
        TextView tvShipping = card.findViewById(R.id.tv_shipping);
        TextView tvTotal = card.findViewById(R.id.tv_total_price);

        tvBase.setText(formatWon(vendor.basePrice));
        if (vendor.freeShipping) {
            tvShipping.setText("+ 무료 배송");
        } else {
            tvShipping.setText("+ 배송 " + formatWon(vendor.shippingFee));
        }
        int total = vendor.basePrice + (vendor.freeShipping ? 0 : vendor.shippingFee);
        tvTotal.setText(formatWon(total));
    }

    // ============= 선택 처리 =============
    private void selectCard(String vendorName) {
        selectedVendorName = vendorName;
        // 모든 카드의 selected 상태 갱신 (setSelected는 자식 view에 자동 전파)
        for (int i = 0; i < cardViews.size(); i++) {
            MaterialCardView card = cardViews.get(i);
            String name = ((TextView) card.findViewById(R.id.tv_vendor_name)).getText().toString();
            card.setSelected(vendorName.equals(name));
        }
    }

    // ============= 라이프사이클: dismiss 시 detail1에 옵션 변경 알림 =============
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (optionsModified && optionsChangedListener != null) {
            optionsChangedListener.onOptionsChanged();
        }
    }

    // ============= 헬퍼 =============
    private static String formatWon(int amount) {
        return "₩" + NumberFormat.getNumberInstance(Locale.KOREA).format(amount);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
