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

import com.example.creator_flow.model.PlatformDto;
import com.example.creator_flow.model.PlatformPlanDto;
import com.example.creator_flow.model.SimulationData;
import com.example.creator_flow.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

public class SimulationPlatformFragment extends Fragment {

    /** 판매 플랫폼 정의. defaultFee는 API plan 응답으로 나중에 업데이트될 수 있어 mutable. */
    private static class Platform {
        final String name;
        final String logoLetter;
        /** 수수료율(%) — 소수점 유지를 위해 double */
        double defaultFee;
        final boolean isDirectInput;
        /** drawable 리소스 ID — 0이면 logoLetter(텍스트) fallback */
        final int logoResId;
        /** 로고 ImageView 내부 padding (dp) — 로고별 크기 균형 조정용. 기본 6dp. */
        final int logoPaddingDp;

        Platform(String name, String logoLetter, double defaultFee, boolean isDirectInput) {
            this(name, logoLetter, defaultFee, isDirectInput, 0, 6);
        }

        Platform(String name, String logoLetter, double defaultFee, boolean isDirectInput,
                 int logoResId) {
            this(name, logoLetter, defaultFee, isDirectInput, logoResId, 6);
        }

        Platform(String name, String logoLetter, double defaultFee, boolean isDirectInput,
                 int logoResId, int logoPaddingDp) {
            this.name = name;
            this.logoLetter = logoLetter;
            this.defaultFee = defaultFee;
            this.isDirectInput = isDirectInput;
            this.logoResId = logoResId;
            this.logoPaddingDp = logoPaddingDp;
        }
    }

    /** 수수료를 "8" / "4.68" 형식으로 포맷 (정수면 .0 안 붙임) */
    private static String formatFee(double fee) {
        if (fee == (long) fee) return String.valueOf((long) fee);
        return String.valueOf(fee);
    }

    // ===== 시연용 하드코딩 (2026-06) =====
    // ============= 이름/수수료는 백엔드, 로고만 클라이언트 매핑 =============
    // GET /platforms 로 이름·수수료·plan_id 받음. 로고는 백엔드 응답 platform name 기준 매핑.
    private static final List<Platform> PLATFORMS = java.util.Collections.emptyList();

    /** platform name → 로고 drawable resource id */
    private static final java.util.Map<String, Integer> PLATFORM_LOGO_RES =
            new java.util.HashMap<String, Integer>() {{
                put("윗치폼",            R.drawable.logo_witchform);
                put("TMM",              R.drawable.logo_tmm);
                put("텀블벅",            R.drawable.logo_tumblbug);
                put("네이버 스마트스토어", R.drawable.logo_naverstore);
                put("개인폼(무통장)",    R.drawable.logo_bank);
                put("직접 입력",         R.drawable.logo_pencil);
            }};

    /** platform name → 로고 padding (시각적 크기 균형) */
    private static final java.util.Map<String, Integer> PLATFORM_LOGO_PADDING_DP =
            new java.util.HashMap<String, Integer>() {{
                put("윗치폼",            0);
                put("TMM",              0);
                put("텀블벅",            0);
                put("네이버 스마트스토어", 0);
                put("개인폼(무통장)",    8);
                put("직접 입력",         14);
            }};

    // ===== 모델 타입 인자 (Business / Fan-art) =====
    private static final String ARG_MODEL_TYPE = "model_type";

    private final List<View> cardViews = new ArrayList<>();
    private int selectedIndex = -1;
    private View nextButton;
    /** 현재 화면이 표현 중인 모델 타입 (다음 화면으로 전달) */
    private String currentModelType = SimulationDetail1Fragment.MODEL_BUSINESS;

    /** GET /platforms 응답: 플랫폼 이름 → ID */
    private final java.util.Map<String, String> platformNameToId = new HashMap<>();
    /** GET /platforms/{id}/plans 응답: 플랫폼 이름 → 첫 플랜의 plan_id */
    private final java.util.Map<String, String> platformNameToPlanId = new HashMap<>();
    /** API 응답으로 빌드된 플랫폼 리스트 (비어있으면 mock 사용) */
    private final List<Platform> apiPlatforms = new ArrayList<>();

    /** 화면이 view를 갖고 있을 때만 grid 다시 그림 — fragment 보관용 */
    private View rootView;

    public SimulationPlatformFragment() {}

    public static SimulationPlatformFragment newInstance() {
        return newInstance(SimulationDetail1Fragment.MODEL_BUSINESS);
    }

    public static SimulationPlatformFragment newInstance(String modelType) {
        SimulationPlatformFragment fragment = new SimulationPlatformFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODEL_TYPE, modelType);
        fragment.setArguments(args);
        return fragment;
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

        rootView = view;
        populateGrid(view);
        // 이름·수수료·plan_id 모두 백엔드 API에서 받음. 로고만 클라이언트 매핑.
        loadPlatformsFromApi();
    }

    /**
     * 시연용 하드코딩 모드 변종: 백엔드 GET /platforms + plans 호출하지만
     * 응답으로 화면을 다시 그리지 않고 platformNameToPlanId 매핑만 채움.
     * (apiPlatforms는 비워둠 → populateGrid는 항상 PLATFORMS 하드코딩 사용)
     */
    private void loadPlatformIdsOnly() {
        if (getContext() == null) return;
        RetrofitClient.getApi(requireContext()).getPlatforms(null, null)
                .enqueue(new Callback<List<PlatformDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PlatformDto>> call,
                                           @NonNull Response<List<PlatformDto>> resp) {
                        if (!isAdded() || !resp.isSuccessful() || resp.body() == null) return;
                        for (PlatformDto p : resp.body()) {
                            if (p.name == null || p.id == null) continue;
                            platformNameToId.put(p.name, p.id);
                            // 각 플랫폼의 첫 plan_id 받아옴 (화면 갱신 X — apiPlatforms 비워둠)
                            loadPlatformPlansForId(p.name, p.id);
                        }
                        android.util.Log.d("PlatformIds",
                                "plan_id 매핑 시작 — " + resp.body().size() + " platforms");
                    }
                    @Override
                    public void onFailure(@NonNull Call<List<PlatformDto>> call,
                                          @NonNull Throwable t) {
                        android.util.Log.w("PlatformIds", "GET /platforms 실패: " + t.getMessage());
                    }
                });
    }

    /**
     * GET /platforms 호출.
     * 성공 시 apiPlatforms 빌드 + 그리드 다시 채움 (mock 대체).
     * 그 다음 각 플랫폼의 plan도 fetch하여 fee 업데이트.
     */
    private void loadPlatformsFromApi() {
        if (getContext() == null) return;
        RetrofitClient.getApi(requireContext()).getPlatforms(null, null)
                .enqueue(new Callback<List<PlatformDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PlatformDto>> call,
                                           @NonNull Response<List<PlatformDto>> resp) {
                        if (!isAdded()) return;
                        android.util.Log.d("PlatformApi",
                                "GET /platforms → HTTP " + resp.code()
                                + ", body=" + (resp.body() != null ? resp.body().size() : "null") + " items");
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            platformNameToId.clear();
                            apiPlatforms.clear();

                            for (PlatformDto p : resp.body()) {
                                if (p.name == null || p.id == null) continue;
                                platformNameToId.put(p.name, p.id);

                                // 로고 매핑 (클라이언트 보유 drawable)
                                Integer logoRes = PLATFORM_LOGO_RES.get(p.name);
                                Integer paddingDp = PLATFORM_LOGO_PADDING_DP.get(p.name);
                                boolean isDirectInput = "직접 입력".equals(p.name);

                                Platform platform = new Platform(
                                        p.name,                       // 이름: 백엔드
                                        p.name.substring(0, 1),       // 텍스트 fallback (로고 없을 때)
                                        0,                            // 수수료: plan 응답 기다림
                                        isDirectInput,                // 직접 입력 카드 인식
                                        logoRes != null ? logoRes : 0,
                                        paddingDp != null ? paddingDp : 6
                                );
                                apiPlatforms.add(platform);
                                loadPlatformPlansForId(p.name, p.id);
                            }
                            // 그리드 다시 그리기 (API 플랫폼으로)
                            if (rootView != null) {
                                populateGrid(rootView);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PlatformDto>> call, @NonNull Throwable t) {
                        android.util.Log.e("PlatformApi",
                                "GET /platforms FAILED: " + t.getMessage());
                    }
                });
    }

    /**
     * GET /platforms/{id}/plans 호출.
     * 첫 플랜의 ID + fee_rate를 저장하고, 화면의 해당 카드 fee 텍스트도 업데이트.
     */
    private void loadPlatformPlansForId(String platformName, String platformId) {
        if (getContext() == null) return;
        RetrofitClient.getApi(requireContext()).getPlatformPlans(platformId)
                .enqueue(new Callback<List<PlatformPlanDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PlatformPlanDto>> call,
                                           @NonNull Response<List<PlatformPlanDto>> resp) {
                        if (!isAdded()) return;
                        android.util.Log.d("PlatformApi",
                                "GET /platforms/" + platformId + "/plans → HTTP " + resp.code()
                                + ", body=" + (resp.body() != null ? resp.body().size() : "null") + " plans"
                                + ", platform=" + platformName);
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            PlatformPlanDto plan = resp.body().get(0);
                            if (plan.id != null) {
                                platformNameToPlanId.put(platformName, plan.id);
                                android.util.Log.d("PlatformApi",
                                        "  ★ planId 저장: " + platformName + " → " + plan.id);
                            }
                            // 소수점 유지 (4.68% 같은 값 보존)
                            double feePercent = plan.feeRate != null ? plan.feeRate : 0;
                            // apiPlatforms에서 매칭되는 플랫폼 찾아 fee 업데이트
                            for (int i = 0; i < apiPlatforms.size(); i++) {
                                Platform p = apiPlatforms.get(i);
                                if (platformName.equals(p.name)) {
                                    p.defaultFee = feePercent;
                                    updateCardFee(i, feePercent);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PlatformPlanDto>> call,
                                          @NonNull Throwable t) {
                        android.util.Log.e("PlatformApi",
                                "GET /platforms/" + platformId + "/plans FAILED: " + t.getMessage());
                    }
                });
    }

    /** apiPlatforms의 i번째 카드의 fee 텍스트 in-place 업데이트 */
    private void updateCardFee(int index, double feePercent) {
        if (index < 0 || index >= cardViews.size()) return;
        View card = cardViews.get(index);
        if (card == null) return;
        TextView tvFee = card.findViewById(R.id.tv_fee);
        if (tvFee != null && tvFee.getVisibility() == View.VISIBLE) {
            tvFee.setText(formatFee(feePercent) + "%");
        }
    }

    // ============= 그리드 생성 =============
    private void populateGrid(View root) {
        LinearLayout rowsContainer = root.findViewById(R.id.platform_rows);
        rowsContainer.removeAllViews();
        cardViews.clear();
        // 그리드를 다시 그릴 때 선택 상태 초기화 (apiPlatforms 인덱스가 mock과 다를 수 있어 안전)
        selectedIndex = -1;
        if (nextButton != null) nextButton.setVisibility(View.GONE);

        // API 모드: apiPlatforms 사용. 비어있으면 mock 사용.
        List<Platform> source = apiPlatforms.isEmpty() ? PLATFORMS : apiPlatforms;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // 2개씩 묶어서 행 배치
        for (int i = 0; i < source.size(); i += 2) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) rowLp.topMargin = dp(12);
            row.setLayoutParams(rowLp);

            for (int j = 0; j < 2 && (i + j) < source.size(); j++) {
                View card = inflater.inflate(R.layout.item_platform_card, row, false);
                LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(0, dp(135), 1f);
                if (j == 1) cardLp.leftMargin = dp(12);
                card.setLayoutParams(cardLp);

                bindCard(card, source.get(i + j), i + j);
                cardViews.add(card);
                row.addView(card);
            }
            rowsContainer.addView(row);
        }
    }

    private void bindCard(View card, Platform p, int index) {
        // 로고: drawable 있으면 이미지 표시, 없으면 한 글자 텍스트 fallback
        ImageView ivLogo = card.findViewById(R.id.iv_logo);
        TextView tvLetter = card.findViewById(R.id.tv_logo_letter);
        if (p.logoResId != 0) {
            ivLogo.setImageResource(p.logoResId);
            // 로고별 padding으로 시각적 크기 균형 맞춤
            int pad = dp(p.logoPaddingDp);
            ivLogo.setPadding(pad, pad, pad, pad);
            ivLogo.setVisibility(View.VISIBLE);
            tvLetter.setVisibility(View.GONE);
        } else {
            ivLogo.setVisibility(View.GONE);
            tvLetter.setVisibility(View.VISIBLE);
            tvLetter.setText(p.logoLetter);
        }
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
            tvFee.setText(formatFee(p.defaultFee) + "%");
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
        // 선택된 플랫폼 정보 저장 (현재 보이는 source 기준)
        List<Platform> source = apiPlatforms.isEmpty() ? PLATFORMS : apiPlatforms;
        if (selectedIndex >= 0 && selectedIndex < source.size()) {
            Platform p = source.get(selectedIndex);
            SimulationData.platformName = p.name;
            // ✅ POST /simulations에 보낼 plan ID commit
            // - 직접 입력: null
            // - 이름 매칭되면 그 plan_id
            // - 매칭 실패 시: 백엔드의 첫 plan_id를 fallback (시뮬레이션 계산 보장)
            String planId = null;
            if (!p.isDirectInput) {
                planId = platformNameToPlanId.get(p.name);
                if (planId == null && !platformNameToPlanId.isEmpty()) {
                    planId = platformNameToPlanId.values().iterator().next();   // fallback
                    android.util.Log.d("PlatformApi",
                            "plan_id 이름 매칭 실패 → fallback 첫 plan_id 사용: " + planId);
                }
            }
            SimulationData.platformPlanId = planId;
            android.util.Log.d("PlatformApi",
                    "goToDelivery 시점 - platformName=" + p.name
                    + ", isDirectInput=" + p.isDirectInput
                    + ", apiPlatforms.size=" + apiPlatforms.size()
                    + ", platformNameToPlanId.size=" + platformNameToPlanId.size()
                    + ", committedPlanId=" + SimulationData.platformPlanId);
            if (p.isDirectInput) {
                View root = getView();
                if (root != null) {
                    EditText etFee = root.findViewById(R.id.et_fee);
                    if (etFee != null) {
                        try {
                            SimulationData.platformFee = Double.parseDouble(
                                    etFee.getText().toString().trim());
                        } catch (NumberFormatException ignored) {
                            SimulationData.platformFee = 0.0;
                        }
                    }
                }
            } else {
                SimulationData.platformFee = p.defaultFee;
            }
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, SimulationDeliveryFragment.newInstance(currentModelType))
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
