package com.example.creator_flow;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.creator_flow.model.VendorInfo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SimulationDetail1Fragment extends Fragment {

    // ===== 모델 타입 인자 =====
    private static final String ARG_MODEL_TYPE = "model_type";
    public static final String MODEL_BUSINESS = "Business";
    public static final String MODEL_FANART = "Fanart";

    private static final String CUSTOM_GOODS_TYPE = "직접입력";
    private static final int[] HAPTIC_POINTS = {10, 30, 50, 100, 150, 200, 250, 300};

    /** 굿즈 유형 → 세부 유형 목록 */
    private static final Map<String, List<String>> GOODS_TYPE_TO_SUBTYPES = new LinkedHashMap<>();
    /** 세부 유형 → 옵션 카테고리 → 값들 (순서 유지) */
    private static final Map<String, LinkedHashMap<String, List<String>>> SUBTYPE_OPTIONS = new LinkedHashMap<>();
    /** 전체 업체 목록 */
    private static final List<VendorInfo> ALL_VENDORS;

    static {
        // ============= 굿즈 유형 → 세부 유형 =============
        GOODS_TYPE_TO_SUBTYPES.put("스티커", Arrays.asList("띠부띠부 스티커", "완칼 스티커", "반칼 스티커", "조각 스티커"));
        GOODS_TYPE_TO_SUBTYPES.put("포스터", Arrays.asList("A4", "B4", "A3", "A2", "A1"));
        GOODS_TYPE_TO_SUBTYPES.put("엽서", Arrays.asList("정사각", "직사각", "미니"));
        GOODS_TYPE_TO_SUBTYPES.put("키링", Arrays.asList("아크릴", "가죽", "우드", "도자기"));
        GOODS_TYPE_TO_SUBTYPES.put("쿠션", Arrays.asList("정사각", "직사각", "캐릭터"));
        GOODS_TYPE_TO_SUBTYPES.put("가방", Arrays.asList("에코백", "크로스백", "백팩", "파우치"));
        GOODS_TYPE_TO_SUBTYPES.put("티셔츠", Arrays.asList("반팔", "긴팔", "후드", "맨투맨"));
        GOODS_TYPE_TO_SUBTYPES.put("인형", Arrays.asList("봉제 인형", "피규어", "키링 인형"));
        GOODS_TYPE_TO_SUBTYPES.put("뱃지", Arrays.asList("캔뱃지", "거울뱃지", "핀뱃지"));
        GOODS_TYPE_TO_SUBTYPES.put("마우스패드", Arrays.asList("라운드", "사각", "캐릭터"));
        GOODS_TYPE_TO_SUBTYPES.put("컵", Arrays.asList("머그컵", "텀블러", "유리잔"));
        GOODS_TYPE_TO_SUBTYPES.put(CUSTOM_GOODS_TYPE, Collections.emptyList());

        // ============= 세부 유형 → 옵션 =============
        // 스티커
        SUBTYPE_OPTIONS.put("띠부띠부 스티커", opts()
                .put("용지", "유광", "무광", "펄")
                .put("사이즈", "소", "중", "대")
                .build());
        SUBTYPE_OPTIONS.put("완칼 스티커", opts()
                .put("용지", "유광", "무광", "펄")
                .put("코팅", "라미", "없음")
                .put("사이즈", "소", "중", "대")
                .build());
        SUBTYPE_OPTIONS.put("반칼 스티커", opts()
                .put("용지", "유광", "무광", "펄")
                .put("코팅", "라미", "없음")
                .put("반칼 형태", "원형", "사각", "자유형")
                .build());
        SUBTYPE_OPTIONS.put("조각 스티커", opts()
                .put("용지", "유광", "무광")
                .put("사이즈", "소", "중", "대")
                .build());
        // 포스터 (A4~A1: 동일 옵션 패턴)
        for (String size : Arrays.asList("A4", "B4", "A3", "A2", "A1")) {
            SUBTYPE_OPTIONS.put(size, opts()
                    .put("용지", "유광", "무광")
                    .put("두께", "일반", "두꺼움")
                    .build());
        }
        // 엽서
        SUBTYPE_OPTIONS.put("정사각", opts()
                .put("용지", "유광", "무광")
                .put("두께", "일반", "두꺼움")
                .build());
        SUBTYPE_OPTIONS.put("직사각", opts()
                .put("용지", "유광", "무광")
                .put("두께", "일반", "두꺼움")
                .build());
        SUBTYPE_OPTIONS.put("미니", opts()
                .put("용지", "유광", "무광")
                .build());
        // 키링
        SUBTYPE_OPTIONS.put("아크릴", opts()
                .put("두께", "3T", "5T")
                .put("크기", "소", "중", "대")
                .build());
        SUBTYPE_OPTIONS.put("가죽", opts()
                .put("재질", "천연", "인조")
                .put("크기", "소", "중")
                .build());
        SUBTYPE_OPTIONS.put("우드", opts()
                .put("크기", "소", "중", "대")
                .build());
        SUBTYPE_OPTIONS.put("도자기", opts()
                .put("크기", "소", "중")
                .build());
        // 쿠션 (정사각/직사각/캐릭터)
        SUBTYPE_OPTIONS.put("캐릭터", opts()
                .put("크기", "소", "중", "대")
                .build());
        // 가방
        SUBTYPE_OPTIONS.put("에코백", opts()
                .put("원단", "면", "캔버스")
                .put("크기", "소", "중", "대")
                .build());
        SUBTYPE_OPTIONS.put("크로스백", opts()
                .put("원단", "면", "폴리에스터")
                .build());
        SUBTYPE_OPTIONS.put("백팩", opts()
                .put("원단", "캔버스", "폴리에스터")
                .build());
        SUBTYPE_OPTIONS.put("파우치", opts()
                .put("원단", "면", "캔버스")
                .build());
        // 티셔츠
        SUBTYPE_OPTIONS.put("반팔", opts()
                .put("원단", "면", "혼방")
                .put("사이즈", "S", "M", "L", "XL")
                .build());
        SUBTYPE_OPTIONS.put("긴팔", opts()
                .put("원단", "면", "혼방")
                .put("사이즈", "S", "M", "L", "XL")
                .build());
        SUBTYPE_OPTIONS.put("후드", opts()
                .put("원단", "면", "기모")
                .put("사이즈", "S", "M", "L", "XL")
                .build());
        SUBTYPE_OPTIONS.put("맨투맨", opts()
                .put("원단", "면", "기모")
                .put("사이즈", "S", "M", "L", "XL")
                .build());
        // 인형
        SUBTYPE_OPTIONS.put("봉제 인형", opts()
                .put("크기", "15cm", "25cm", "40cm")
                .build());
        SUBTYPE_OPTIONS.put("피규어", opts()
                .put("크기", "소", "중", "대")
                .build());
        SUBTYPE_OPTIONS.put("키링 인형", opts()
                .put("크기", "소", "중")
                .build());
        // 뱃지
        SUBTYPE_OPTIONS.put("캔뱃지", opts()
                .put("사이즈", "25mm", "32mm", "44mm", "58mm")
                .build());
        SUBTYPE_OPTIONS.put("거울뱃지", opts()
                .put("사이즈", "44mm", "58mm")
                .build());
        SUBTYPE_OPTIONS.put("핀뱃지", opts()
                .put("재질", "금속", "에나멜")
                .build());
        // 마우스패드
        SUBTYPE_OPTIONS.put("라운드", opts()
                .put("크기", "소", "중")
                .build());
        SUBTYPE_OPTIONS.put("사각", opts()
                .put("크기", "소", "중", "대")
                .build());
        // 컵
        SUBTYPE_OPTIONS.put("머그컵", opts()
                .put("재질", "도자기", "스테인리스")
                .build());
        SUBTYPE_OPTIONS.put("텀블러", opts()
                .put("재질", "스테인리스")
                .put("용량", "350ml", "500ml")
                .build());
        SUBTYPE_OPTIONS.put("유리잔", opts()
                .put("용량", "240ml", "350ml")
                .build());

        // ============= 업체 =============
        ALL_VENDORS = Arrays.asList(
                // 퍼블로그 — 지류 (스티커, 포스터, 엽서) 위주
                new VendorInfo("퍼블로그", Color.parseColor("#1A237E"), Color.parseColor("#F5FAE0"),
                        51000, 3000, false,
                        cap()
                                .add("완칼 스티커", "용지", "유광", "무광")
                                .add("완칼 스티커", "코팅", "라미", "없음")
                                .add("완칼 스티커", "사이즈", "소", "중", "대")
                                .add("반칼 스티커", "용지", "유광", "무광")
                                .add("반칼 스티커", "코팅", "라미", "없음")
                                .add("반칼 스티커", "반칼 형태", "원형", "사각")
                                .add("A4", "용지", "유광", "무광").add("A4", "두께", "일반", "두꺼움")
                                .add("B4", "용지", "유광", "무광").add("B4", "두께", "일반")
                                .add("A3", "용지", "유광", "무광").add("A3", "두께", "일반", "두꺼움")
                                .add("정사각", "용지", "유광", "무광").add("정사각", "두께", "일반")
                                .add("직사각", "용지", "유광", "무광").add("직사각", "두께", "일반")
                                .build()),
                // 레드프린팅 — 거의 모든 종이류 + 뱃지
                new VendorInfo("레드프린팅", Color.parseColor("#E53935"), Color.parseColor("#EEF6CC"),
                        44500, 2500, false,
                        cap()
                                .add("띠부띠부 스티커", "용지", "유광", "무광", "펄").add("띠부띠부 스티커", "사이즈", "소", "중", "대")
                                .add("완칼 스티커", "용지", "유광", "무광", "펄").add("완칼 스티커", "코팅", "라미", "없음").add("완칼 스티커", "사이즈", "소", "중", "대")
                                .add("반칼 스티커", "용지", "유광", "무광", "펄").add("반칼 스티커", "코팅", "라미", "없음").add("반칼 스티커", "반칼 형태", "원형", "사각", "자유형")
                                .add("조각 스티커", "용지", "유광", "무광").add("조각 스티커", "사이즈", "소", "중", "대")
                                .add("A4", "용지", "유광", "무광").add("A4", "두께", "일반", "두꺼움")
                                .add("B4", "용지", "유광", "무광").add("B4", "두께", "일반", "두꺼움")
                                .add("A3", "용지", "유광", "무광").add("A3", "두께", "일반", "두꺼움")
                                .add("A2", "용지", "유광", "무광").add("A2", "두께", "일반", "두꺼움")
                                .add("A1", "용지", "유광", "무광").add("A1", "두께", "두꺼움")
                                .add("정사각", "용지", "유광", "무광").add("정사각", "두께", "일반", "두꺼움")
                                .add("직사각", "용지", "유광", "무광").add("직사각", "두께", "일반", "두꺼움")
                                .add("미니", "용지", "유광", "무광")
                                .add("캔뱃지", "사이즈", "25mm", "32mm", "44mm")
                                .build()),
                // 마플 — 패브릭/잡화 위주
                new VendorInfo("마플", Color.parseColor("#1565C0"), Color.parseColor("#EEF6CC"),
                        54000, 0, true,
                        cap()
                                .add("완칼 스티커", "용지", "유광").add("완칼 스티커", "코팅", "없음").add("완칼 스티커", "사이즈", "소", "중")
                                .add("반칼 스티커", "용지", "유광").add("반칼 스티커", "코팅", "없음").add("반칼 스티커", "반칼 형태", "원형", "사각")
                                .add("아크릴", "두께", "3T", "5T").add("아크릴", "크기", "소", "중", "대")
                                .add("우드", "크기", "소", "중")
                                .add("반팔", "원단", "면", "혼방").add("반팔", "사이즈", "S", "M", "L", "XL")
                                .add("긴팔", "원단", "면", "혼방").add("긴팔", "사이즈", "S", "M", "L", "XL")
                                .add("후드", "원단", "면", "기모").add("후드", "사이즈", "S", "M", "L", "XL")
                                .add("맨투맨", "원단", "면", "기모").add("맨투맨", "사이즈", "S", "M", "L", "XL")
                                .add("에코백", "원단", "면", "캔버스").add("에코백", "크기", "소", "중", "대")
                                .add("크로스백", "원단", "면", "폴리에스터")
                                .add("파우치", "원단", "면", "캔버스")
                                .add("정사각", "용지", "유광", "무광").add("정사각", "두께", "일반")
                                .add("직사각", "용지", "유광", "무광").add("직사각", "두께", "일반")
                                .add("머그컵", "재질", "도자기", "스테인리스")
                                .add("텀블러", "재질", "스테인리스").add("텀블러", "용량", "350ml", "500ml")
                                .add("라운드", "크기", "소", "중")
                                .add("사각", "크기", "소", "중", "대")
                                .build()),
                // 스냅스 — 지류 일부
                new VendorInfo("스냅스", Color.parseColor("#212121"), Color.parseColor("#EEF6CC"),
                        39000, 2500, false,
                        cap()
                                .add("완칼 스티커", "용지", "유광", "무광").add("완칼 스티커", "코팅", "라미").add("완칼 스티커", "사이즈", "소", "중")
                                .add("반칼 스티커", "용지", "유광", "무광").add("반칼 스티커", "코팅", "라미").add("반칼 스티커", "반칼 형태", "원형", "사각")
                                .add("A4", "용지", "유광", "무광").add("A4", "두께", "일반")
                                .add("A3", "용지", "유광", "무광").add("A3", "두께", "일반")
                                .add("정사각", "용지", "유광", "무광").add("정사각", "두께", "일반")
                                .add("직사각", "용지", "유광", "무광").add("직사각", "두께", "일반")
                                .build()),
                // 프린팅박스 — 종이/아크릴 풀라인
                new VendorInfo("프린팅박스", Color.parseColor("#FF6F00"), Color.parseColor("#EEF6CC"),
                        50000, 2000, false,
                        cap()
                                .add("띠부띠부 스티커", "용지", "유광", "무광", "펄").add("띠부띠부 스티커", "사이즈", "소", "중", "대")
                                .add("완칼 스티커", "용지", "유광", "무광", "펄").add("완칼 스티커", "코팅", "라미", "없음").add("완칼 스티커", "사이즈", "소", "중", "대")
                                .add("반칼 스티커", "용지", "유광", "무광", "펄").add("반칼 스티커", "코팅", "라미", "없음").add("반칼 스티커", "반칼 형태", "원형", "사각", "자유형")
                                .add("조각 스티커", "용지", "유광", "무광").add("조각 스티커", "사이즈", "소", "중", "대")
                                .add("A4", "용지", "유광", "무광").add("A4", "두께", "일반", "두꺼움")
                                .add("B4", "용지", "유광", "무광").add("B4", "두께", "일반", "두꺼움")
                                .add("A3", "용지", "유광", "무광").add("A3", "두께", "일반", "두꺼움")
                                .add("A2", "용지", "유광", "무광").add("A2", "두께", "일반", "두꺼움")
                                .add("A1", "용지", "유광", "무광").add("A1", "두께", "일반", "두꺼움")
                                .add("정사각", "용지", "유광", "무광").add("정사각", "두께", "일반", "두꺼움")
                                .add("직사각", "용지", "유광", "무광").add("직사각", "두께", "일반", "두꺼움")
                                .add("아크릴", "두께", "3T", "5T").add("아크릴", "크기", "소", "중", "대")
                                .build()),
                // 오프린트미 — 스티커/포스터/뱃지/엽서
                new VendorInfo("오프린트미", Color.parseColor("#2E7D32"), Color.parseColor("#EEF6CC"),
                        47000, 3500, false,
                        cap()
                                .add("완칼 스티커", "용지", "유광", "무광").add("완칼 스티커", "코팅", "없음").add("완칼 스티커", "사이즈", "소", "중")
                                .add("반칼 스티커", "용지", "유광", "무광").add("반칼 스티커", "코팅", "없음").add("반칼 스티커", "반칼 형태", "원형", "사각")
                                .add("A4", "용지", "유광", "무광").add("A4", "두께", "일반")
                                .add("A3", "용지", "유광", "무광").add("A3", "두께", "일반")
                                .add("정사각", "용지", "유광", "무광").add("정사각", "두께", "일반")
                                .add("캔뱃지", "사이즈", "25mm", "32mm", "44mm", "58mm")
                                .add("거울뱃지", "사이즈", "44mm", "58mm")
                                .build())
        );
    }

    // ----- 옵션/케이프 빌더 헬퍼 -----
    private static OptsBuilder opts() { return new OptsBuilder(); }
    private static CapBuilder cap() { return new CapBuilder(); }

    private static class OptsBuilder {
        final LinkedHashMap<String, List<String>> data = new LinkedHashMap<>();
        OptsBuilder put(String category, String... values) {
            data.put(category, Arrays.asList(values));
            return this;
        }
        LinkedHashMap<String, List<String>> build() { return data; }
    }

    private static class CapBuilder {
        final Map<String, Map<String, Set<String>>> data = new HashMap<>();
        CapBuilder add(String subType, String category, String... values) {
            Map<String, Set<String>> sub = data.computeIfAbsent(subType, k -> new HashMap<>());
            Set<String> vs = sub.computeIfAbsent(category, k -> new HashSet<>());
            vs.addAll(Arrays.asList(values));
            return this;
        }
        Map<String, Map<String, Set<String>>> build() { return data; }
    }

    // ----- 상태 -----
    private String currentGoodsType;
    private String currentSubType;
    private final Map<String, String> selectedOptions = new HashMap<>();

    public SimulationDetail1Fragment() {}

    public static SimulationDetail1Fragment newInstance() {
        return newInstance(MODEL_BUSINESS);
    }

    public static SimulationDetail1Fragment newInstance(String modelType) {
        SimulationDetail1Fragment fragment = new SimulationDetail1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODEL_TYPE, modelType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_detail1, container, false);
    }

    /** 바텀시트에서 확정된 vendor 키 (null = 아직 선택 전) */
    private String confirmedVendorKey;
    private View nextButton;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        nextButton = view.findViewById(R.id.btn_next);
        nextButton.setOnClickListener(v -> goToPlatform());

        // 모델 헤더: Business / Fan-art 분기
        TextView tvModelHeader = view.findViewById(R.id.tv_model_header);
        if (tvModelHeader != null) {
            String modelType = getArguments() != null
                    ? getArguments().getString(ARG_MODEL_TYPE, MODEL_BUSINESS)
                    : MODEL_BUSINESS;
            tvModelHeader.setText(MODEL_FANART.equals(modelType) ? "Fan-art Model" : "Business Model");
        }

        setupGoodsTypeSpinner(view);
        setupSubTypeSpinner(view);
        setupQuantitySlider(view);
    }

    private void goToPlatform() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, new SimulationPlatformFragment())
                .addToBackStack(null)
                .commit();
    }

    // ============= 굿즈 유형 Spinner =============
    private void setupGoodsTypeSpinner(View root) {
        Spinner sp = root.findViewById(R.id.spinner_goods_type);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                String selected = String.valueOf(parent.getItemAtPosition(position));
                onGoodsTypeChanged(root, selected);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void onGoodsTypeChanged(View root, String goodsType) {
        currentGoodsType = goodsType;

        Spinner subSp = root.findViewById(R.id.spinner_sub_type);
        View subCard = root.findViewById(R.id.card_sub_type);

        if (CUSTOM_GOODS_TYPE.equals(goodsType)) {
            // 직접입력: 세부 유형 카드 숨김, 옵션/업체 비움
            subCard.setVisibility(View.GONE);
            currentSubType = null;
            selectedOptions.clear();
            renderOptionChipsEmpty(root, "직접입력 모드 — 옵션 미지원");
            renderVendorCardsEmpty(root, "직접입력 시에는 업체를 직접 협의해야 합니다.");
            return;
        }

        subCard.setVisibility(View.VISIBLE);
        List<String> subTypes = GOODS_TYPE_TO_SUBTYPES.getOrDefault(goodsType, Collections.emptyList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>(subTypes));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subSp.setAdapter(adapter);

        if (!subTypes.isEmpty()) {
            subSp.setSelection(0, false);
            // setSelection(false)는 콜백을 발생시키지 않으므로 수동 트리거
            onSubTypeChanged(root, subTypes.get(0));
        }
    }

    // ============= 세부 유형 Spinner =============
    private void setupSubTypeSpinner(View root) {
        Spinner sp = root.findViewById(R.id.spinner_sub_type);
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                String sub = String.valueOf(parent.getItemAtPosition(position));
                onSubTypeChanged(root, sub);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void onSubTypeChanged(View root, String subType) {
        currentSubType = subType;
        selectedOptions.clear();
        LinkedHashMap<String, List<String>> opts = SUBTYPE_OPTIONS.get(subType);
        if (opts != null) {
            for (Map.Entry<String, List<String>> e : opts.entrySet()) {
                if (!e.getValue().isEmpty()) {
                    selectedOptions.put(e.getKey(), e.getValue().get(0));
                }
            }
        }
        renderOptionChips(root);
        renderVendorCards(root);
    }

    // ============= 옵션 칩 =============
    private void renderOptionChips(View root) {
        LinearLayout container = root.findViewById(R.id.options_chip_container);
        container.removeAllViews();
        LinkedHashMap<String, List<String>> opts = SUBTYPE_OPTIONS.get(currentSubType);
        if (opts == null || opts.isEmpty()) {
            renderOptionChipsEmpty(root, "선택할 옵션이 없습니다.");
            return;
        }
        for (Map.Entry<String, List<String>> entry : opts.entrySet()) {
            String category = entry.getKey();
            List<String> values = entry.getValue();

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(8);
            row.setLayoutParams(rowLp);

            TextView label = new TextView(getContext());
            label.setText(category);
            label.setTextColor(Color.parseColor("#7A7A7A"));
            label.setTextSize(13f);
            label.setLayoutParams(new LinearLayout.LayoutParams(dp(64), LinearLayout.LayoutParams.WRAP_CONTENT));
            row.addView(label);

            for (String value : values) {
                TextView chip = new TextView(getContext());
                chip.setText(value);
                chip.setTextSize(13f);
                chip.setPadding(dp(14), dp(6), dp(14), dp(6));
                LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                chipLp.rightMargin = dp(6);
                chip.setLayoutParams(chipLp);
                applyChipStyle(chip, value.equals(selectedOptions.get(category)));
                chip.setOnClickListener(v -> {
                    selectedOptions.put(category, value);
                    renderOptionChips(root);
                    renderVendorCards(root);
                });
                row.addView(chip);
            }
            container.addView(row);
        }
    }

    private void renderOptionChipsEmpty(View root, String message) {
        LinearLayout container = root.findViewById(R.id.options_chip_container);
        container.removeAllViews();
        TextView empty = new TextView(getContext());
        empty.setText(message);
        empty.setTextColor(Color.parseColor("#9A9A9A"));
        empty.setTextSize(13f);
        container.addView(empty);
    }

    private void applyChipStyle(TextView chip, boolean selected) {
        if (selected) {
            chip.setBackgroundResource(R.drawable.chip_selected);
            chip.setTextColor(Color.parseColor("#313131"));
        } else {
            chip.setBackgroundResource(R.drawable.chip_unselected);
            chip.setTextColor(Color.parseColor("#9A9A9A"));
        }
    }

    // ============= 업체 카드 =============
    private void renderVendorCards(View root) {
        LinearLayout container = root.findViewById(R.id.vendor_container);
        container.removeAllViews();

        if (currentSubType == null) {
            renderVendorCardsEmpty(root, "굿즈 유형과 세부 유형을 선택해주세요.");
            return;
        }

        List<VendorInfo> matching = new ArrayList<>();
        for (VendorInfo v : ALL_VENDORS) {
            if (!v.supports(currentSubType)) continue;
            boolean ok = true;
            for (Map.Entry<String, String> sel : selectedOptions.entrySet()) {
                if (!v.supportsOption(currentSubType, sel.getKey(), sel.getValue())) {
                    ok = false;
                    break;
                }
            }
            if (ok) matching.add(v);
        }

        if (matching.isEmpty()) {
            renderVendorCardsEmpty(root, "조건에 맞는 업체가 없습니다.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < matching.size(); i += 2) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(8);
            row.setLayoutParams(rowLp);

            row.addView(buildCard(inflater, row, matching.get(i), true));
            if (i + 1 < matching.size()) {
                row.addView(buildCard(inflater, row, matching.get(i + 1), false));
            } else {
                View spacer = new View(getContext());
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 0, 1f);
                sp.setMarginStart(dp(6));
                spacer.setLayoutParams(sp);
                row.addView(spacer);
            }
            container.addView(row);
        }
    }

    private void renderVendorCardsEmpty(View root, String message) {
        LinearLayout container = root.findViewById(R.id.vendor_container);
        container.removeAllViews();
        TextView empty = new TextView(getContext());
        empty.setText(message);
        empty.setTextColor(Color.parseColor("#9A9A9A"));
        empty.setTextSize(13f);
        container.addView(empty);
    }

    private View buildCard(LayoutInflater inflater, ViewGroup parent, VendorInfo vendor, boolean isLeft) {
        View card = inflater.inflate(R.layout.item_vendor_summary_card, parent, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (isLeft) lp.setMarginEnd(dp(6)); else lp.setMarginStart(dp(6));
        card.setLayoutParams(lp);

        card.setBackgroundTintList(android.content.res.ColorStateList.valueOf(vendor.cardBgColor));

        View colorBox = card.findViewById(R.id.vendor_color_box);
        colorBox.setBackgroundTintList(android.content.res.ColorStateList.valueOf(vendor.brandColor));

        ((TextView) card.findViewById(R.id.tv_vendor_name)).setText(vendor.name);
        ((TextView) card.findViewById(R.id.tv_vendor_price)).setText(formatPrice(vendor.basePrice));

        StringBuilder spec = new StringBuilder();
        for (Map.Entry<String, String> e : selectedOptions.entrySet()) {
            if (spec.length() > 0) spec.append(" · ");
            spec.append(e.getValue());
        }
        ((TextView) card.findViewById(R.id.tv_vendor_spec)).setText(spec.toString());

        String shipText = vendor.freeShipping ? "무료 배송" : "배송 " + formatPrice(vendor.shippingFee);
        ((TextView) card.findViewById(R.id.tv_vendor_ship)).setText(shipText);

        card.setOnClickListener(v -> openVendorBottomSheet());
        return card;
    }

    private static String formatPrice(int amount) {
        return "₩ " + NumberFormat.getNumberInstance(Locale.KOREA).format(amount);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    // ============= 수량 슬라이더 =============
    private void setupQuantitySlider(View view) {
        SeekBar seekBar = view.findViewById(R.id.seekbar_quantity);
        EditText etValue = view.findViewById(R.id.tv_quantity_value);
        if (seekBar == null || etValue == null) return;
        final boolean[] syncing = {false};
        final int[] lastProgress = {seekBar.getProgress()};

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser) return;
                syncing[0] = true;
                etValue.setText(String.valueOf(progress));
                syncing[0] = false;
                int prev = lastProgress[0];
                for (int point : HAPTIC_POINTS) {
                    if ((prev < point && progress >= point) || (prev > point && progress <= point)) {
                        sb.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        break;
                    }
                }
                lastProgress[0] = progress;
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        etValue.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (syncing[0]) return;
                String text = s.toString().trim();
                if (text.isEmpty()) return;
                try {
                    int value = Integer.parseInt(text);
                    if (value < 0) return;
                    int seekValue = Math.min(value, seekBar.getMax());
                    syncing[0] = true;
                    seekBar.setProgress(seekValue);
                    syncing[0] = false;
                    lastProgress[0] = seekValue;
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    // ============= 바텀시트 =============
    private void openVendorBottomSheet() {
        VendorSelectBottomSheet sheet = new VendorSelectBottomSheet();
        sheet.setOnVendorSelectedListener(vendorKey -> {
            // "선택" 버튼 누르면 콜백 → 다음 화살표 노출
            confirmedVendorKey = vendorKey;
            if (nextButton != null) {
                nextButton.setVisibility(View.VISIBLE);
            }
            Toast.makeText(getContext(), "선택된 업체: " + vendorKey, Toast.LENGTH_SHORT).show();
        });
        sheet.show(getParentFragmentManager(), VendorSelectBottomSheet.TAG);
    }
}
