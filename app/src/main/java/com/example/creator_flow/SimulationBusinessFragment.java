package com.example.creator_flow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.creator_flow.model.ProjectFromSimulationDto;
import com.example.creator_flow.model.SimulationData;
import com.example.creator_flow.model.SimulationResultDto;
import com.example.creator_flow.network.RetrofitClient;

import com.example.creator_flow.model.SimulationDetailDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * 시뮬레이션 마지막 요약 화면.
 * SimulationData 정적 클래스에서 사용자가 선택/입력한 값 읽어와 표시.
 * Fan-art 모드에서는 희망 순이익/마진율 행을 숨김.
 *
 * TODO: 백엔드 API 완성 시
 *   - 진입 시점에 GET /simulations/{id} 또는 유사 엔드포인트 호출하여 응답으로 채우기
 *   - 확인 버튼: POST /simulations/{id}/project 호출 후 ProjectPageFragment 이동
 */
public class SimulationBusinessFragment extends Fragment {

    private static final String ARG_MODEL_TYPE = "model_type";

    /**
     * ⚠️ 시현/GitHub 푸시 토글용 플래그.
     *
     * - {@code true} (현재 — 시현 모드): 확인 클릭 후 새 프로젝트의 ProjectPageFragment로 직접 이동.
     *   ProjectFragment(프로젝트 목록) 미완성이라 우회.
     *
     * - {@code false} (정상 — GitHub 푸시용): 확인 후 ProjectFragment 목록으로 이동.
     *   사용자가 목록에서 프로젝트 클릭하여 ProjectPageFragment 진입.
     *
     * GitHub 푸시 전에 이 값만 false로 바꾸면 본래 A/B 흐름으로 복귀.
     * (차시 추가 모드 Flow B는 항상 ProjectPageFragment로 이동 — 플래그 무관)
     */
    private static final boolean DEMO_SKIP_PROJECT_LIST = false;

    /**
     * 시연용 로컬 프로젝트 모드.
     * true면 시뮬레이션 결과만 백엔드에서 받고, 프로젝트 생성/조회 API는 호출 안 함.
     * ProjectPageFragment를 projectId=null로 띄우고 SimulationData.lastResult 값으로 첫 차시 채움.
     * 백엔드 프로젝트 API가 불안정할 때 시연을 안전하게 함.
     */
    private static final boolean LOCAL_PROJECT_MODE = true;

    private boolean isFanart;

    public SimulationBusinessFragment() {}

    public static SimulationBusinessFragment newInstance() {
        return newInstance(SimulationDetail1Fragment.MODEL_BUSINESS);
    }

    public static SimulationBusinessFragment newInstance(String modelType) {
        SimulationBusinessFragment fragment = new SimulationBusinessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODEL_TYPE, modelType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_simulation_business, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 뒤로가기
        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // 모델 타입 확인
        String modelType = getArguments() != null
                ? getArguments().getString(ARG_MODEL_TYPE, SimulationDetail1Fragment.MODEL_BUSINESS)
                : SimulationDetail1Fragment.MODEL_BUSINESS;
        isFanart = SimulationDetail1Fragment.MODEL_FANART.equals(modelType);

        // 각 행 채우기 (SimulationData에서 읽음)
        bindRow(view, R.id.row_goods_type, "굿즈 유형", SimulationData.goodsType);
        bindRow(view, R.id.row_option, "옵션", SimulationData.optionSummary);
        bindRow(view, R.id.row_quantity, "수량",
                SimulationData.quantity != null ? SimulationData.quantity + "개" : null);
        bindRow(view, R.id.row_delivery_management, "배송 관리", "직접 관리");  // TODO: 실제 값

        bindRow(view, R.id.row_vendor, "제작 업체", SimulationData.vendorName);
        bindRow(view, R.id.row_platform, "판매 업체", SimulationData.platformName);

        bindRow(view, R.id.row_delivery_method, "택배", SimulationData.deliveryMethod);
        bindRow(view, R.id.row_shipping_fee, "배송비", SimulationData.shippingFeeType);
        bindRow(view, R.id.row_packaging, "포장재", SimulationData.packagingType);

        // 입력값 행 (Fan-art에서는 위 2개 숨김)
        View rowProfit = view.findViewById(R.id.row_profit);
        View rowMargin = view.findViewById(R.id.row_margin);
        if (isFanart) {
            rowProfit.setVisibility(View.GONE);
            rowMargin.setVisibility(View.GONE);
        } else {
            bindRow(view, R.id.row_profit, "희망 순이익",
                    SimulationData.profit != null ? formatWon(SimulationData.profit) : null);
            bindRow(view, R.id.row_margin, "희망 마진율",
                    SimulationData.marginRate != null ? SimulationData.marginRate + "%" : null);
        }
        bindRow(view, R.id.row_target_qty, "목표 판매량",
                SimulationData.targetQuantity != null ? SimulationData.targetQuantity + "개" : null);
        bindRow(view, R.id.row_min_stock, "최소 재고량",
                SimulationData.minStock != null ? SimulationData.minStock + "개" : null);

        // 예상 원가 행은 레이아웃에서 주석 처리됨 — findViewById가 null 반환 가능
        TextView tvCost = view.findViewById(R.id.tv_estimated_cost);
        if (tvCost != null) {
            if (SimulationData.estimatedCost != null) {
                tvCost.setText(formatWon(SimulationData.estimatedCost));
            } else {
                tvCost.setText("계산 대기");
            }
        }

        // 확인 버튼 → 프로젝트 페이지 이동
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> onConfirm());

        // (선택) 화면 진입 시 시뮬레이션 결과 미리 계산해서 예상 원가 표시 시도
        // 실제 데이터(vendor_product_id, platform_plan_id 등)가 없으면 호출 안 함
        tryPrefetchSimulationCost(view);
    }

    /**
     * 화면 진입 시 POST /simulations 호출 시도하여 예상 원가 미리 표시.
     * 필수 ID(vendor_product_id, platform_plan_id)가 SimulationData에 없으면 skip.
     */
    private void tryPrefetchSimulationCost(View root) {
        // 현재 SimulationData에 vendor_product_id, platform_plan_id 저장 안 됨
        // → 백엔드 데이터로 vendor/platform 선택 시 채워져야 함
        // 본격 동작은 onConfirm에서. 여기는 placeholder.
    }

    /** 행 하나 채우기. value가 null이면 "—" 표시 */
    private void bindRow(View root, @IdRes int rowId, String label, String value) {
        View row = root.findViewById(rowId);
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.tv_label);
        TextView tvValue = row.findViewById(R.id.tv_value);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value != null && !value.isEmpty() ? value : "—");
    }

    private static String formatWon(int amount) {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + "원";
    }

    private void onConfirm() {
        if (getContext() != null) {
            Toast.makeText(getContext().getApplicationContext(),
                    "시뮬레이션 저장 완료", Toast.LENGTH_SHORT).show();
        }
        // 백엔드 응답 후 navigate (project_id를 응답에서 받아야 ProjectPageFragment로 갈 수 있음)
        attemptRunSimulationAndChainNavigate();
    }

    /**
     * POST /simulations → (필요 시) POST /simulations/{id}/project 연쇄 호출 후
     * 응답으로 받은 project_id로 ProjectPageFragment 이동 (또는 ProjectFragment 목록).
     *
     * mock 모드 (vendor_product_id 또는 platform_plan_id 가 null)면 API 호출 skip하고
     * 즉시 fallback navigate.
     */
    private void attemptRunSimulationAndChainNavigate() {
        if (getContext() == null) return;

        String vendorProductId = SimulationData.vendorProductId;
        String platformPlanId = SimulationData.platformPlanId;
        final String capturedTargetProjectId = SimulationData.targetProjectId;  // null = 새 프로젝트 모드

        if (vendorProductId == null || platformPlanId == null) {
            android.util.Log.w("SimulationConfirm",
                    "❌ MOCK 모드 진입 — 백엔드 호출 SKIP (mock fallback navigate)");
            finishAndNavigate(capturedTargetProjectId);
            return;
        }

        int quantity = SimulationData.quantity != null ? SimulationData.quantity : 0;
        int sellingPrice = 0;
        int shippingFeeBuyer = SimulationData.shippingFeeBuyer != null
                ? SimulationData.shippingFeeBuyer : 0;
        String shippingType = SimulationData.shippingFeeType != null
                ? SimulationData.shippingFeeType : "buyer";
        String selectedOptionIds = (SimulationData.selectedOptionIds != null
                && !SimulationData.selectedOptionIds.isEmpty())
                ? android.text.TextUtils.join(",", SimulationData.selectedOptionIds)
                : "";
        Integer targetQty = SimulationData.targetQuantity;

        android.util.Log.d("SimulationConfirm",
                "→ POST /simulations 호출 시작 (targetProjectId=" + capturedTargetProjectId
                + " " + (capturedTargetProjectId != null ? "[차시 추가]" : "[새 프로젝트]") + ")");

        RetrofitClient.getApi(requireContext()).runSimulation(
                SimulationData.modelType != null ? SimulationData.modelType : "Business",
                vendorProductId, quantity, platformPlanId,
                sellingPrice, shippingFeeBuyer, shippingType,
                selectedOptionIds, targetQty,
                capturedTargetProjectId
        ).enqueue(new Callback<SimulationResultDto>() {
            @Override
            public void onResponse(@NonNull Call<SimulationResultDto> call,
                                   @NonNull Response<SimulationResultDto> resp) {
                if (!isAdded()) return;
                if (!resp.isSuccessful() || resp.body() == null || resp.body().simulationId == null) {
                    android.util.Log.e("SimulationConfirm",
                            "❌ POST /simulations 실패: HTTP " + resp.code() + " → fallback navigate");
                    finishAndNavigate(capturedTargetProjectId);
                    return;
                }
                SimulationResultDto r = resp.body();
                SimulationData.estimatedCost = r.totalCost;
                // 시연용 로컬 모드: 결과를 보관 → ProjectPageFragment 첫 차시 자동 채움용
                SimulationData.lastResult = r;
                android.util.Log.d("SimulationConfirm",
                        "✅ 시뮬레이션 OK: simulation_id=" + r.simulationId);

                if (LOCAL_PROJECT_MODE) {
                    // 시연용 로컬 모드 — 백엔드 프로젝트 API 호출 스킵
                    android.util.Log.d("SimulationConfirm",
                            "🔄 LOCAL_PROJECT_MODE — 프로젝트 생성 API 스킵, ProjectPageFragment(null)로 이동");
                    finishAndNavigate(null);
                    return;
                }

                if (capturedTargetProjectId != null) {
                    // Flow B: 기존 프로젝트에 차시 attach 완료 — 그 프로젝트로 이동
                    finishAndNavigate(capturedTargetProjectId);
                } else {
                    // Flow A: 새 프로젝트 생성 후 그 프로젝트로 이동
                    createProjectAndNavigate(r.simulationId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimulationResultDto> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                android.util.Log.e("SimulationConfirm",
                        "← POST /simulations FAILED: " + t.getMessage() + " → fallback navigate");
                finishAndNavigate(capturedTargetProjectId);
            }
        });
    }

    /** POST /simulations/{id}/project 호출 후 응답의 project_id로 navigate. */
    private void createProjectAndNavigate(String simulationId) {
        if (getContext() == null) return;
        String name = buildProjectName();
        android.util.Log.d("SimulationConfirm",
                "→ POST /simulations/" + simulationId + "/project?name=\"" + name + "\"");
        RetrofitClient.getApi(requireContext())
                .createProjectFromSimulation(simulationId, name)
                .enqueue(new Callback<ProjectFromSimulationDto>() {
                    @Override
                    public void onResponse(@NonNull Call<ProjectFromSimulationDto> call,
                                           @NonNull Response<ProjectFromSimulationDto> resp) {
                        if (!isAdded()) return;
                        String newProjectId = (resp.isSuccessful() && resp.body() != null)
                                ? resp.body().projectId : null;
                        android.util.Log.d("SimulationConfirm",
                                "← POST .../project 응답: HTTP " + resp.code()
                                + ", project_id=" + newProjectId);
                        finishAndNavigate(newProjectId);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProjectFromSimulationDto> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        android.util.Log.e("SimulationConfirm",
                                "← POST .../project FAILED: " + t.getMessage());
                        finishAndNavigate(null);
                    }
                });
    }

    /**
     * 시뮬레이션 흐름 종료 후 화면 이동.
     * - DEMO_SKIP_PROJECT_LIST 가 true: projectId 있으면 ProjectPageFragment로 직접
     * - DEMO_SKIP_PROJECT_LIST 가 false: 차시 추가 모드면 ProjectPageFragment, 새 프로젝트면 ProjectFragment 목록으로
     */
    private void finishAndNavigate(String projectId) {
        boolean wasAddingChasi = SimulationData.targetProjectId != null;
        SimulationData.reset();

        if (!isAdded()) return;
        try {
            FragmentManager fm = getParentFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fm.executePendingTransactions();

            androidx.fragment.app.Fragment dest;
            if (LOCAL_PROJECT_MODE) {
                // 시연용 로컬 모드 — projectId 무시하고 항상 ProjectPageFragment(null) (메모리만)
                dest = new ProjectPageFragment();
            } else if (DEMO_SKIP_PROJECT_LIST) {
                // 시현 모드 — 항상 ProjectPageFragment로 (projectId 없으면 fallback)
                dest = (projectId != null)
                        ? ProjectPageFragment.newInstance(projectId)
                        : new ProjectFragment();
            } else {
                // 정상 모드 — 차시 추가는 ProjectPageFragment, 새 프로젝트는 ProjectFragment 목록
                dest = wasAddingChasi && projectId != null
                        ? ProjectPageFragment.newInstance(projectId)
                        : new ProjectFragment();
            }

            fm.beginTransaction()
                    .replace(R.id.main_fragment, dest)
                    .commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            // state-already-saved 등 — 무시
        }
    }

    /**
     * SimulationData 값들로 프로젝트 이름 자동 생성.
     * 예: "스티커 100개 · 레드프린팅 · 텀블벅 (5%)"
     */
    private String buildProjectName() {
        StringBuilder sb = new StringBuilder();
        if (SimulationData.goodsType != null) {
            sb.append(SimulationData.goodsType);
        }
        if (SimulationData.quantity != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(SimulationData.quantity).append("개");
        }
        if (SimulationData.vendorName != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(SimulationData.vendorName);
        }
        if (SimulationData.platformName != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(SimulationData.platformName);
            if (SimulationData.platformFee != null) {
                sb.append(" (").append(SimulationData.platformFee).append("%)");
            }
        }
        if (sb.length() == 0) {
            sb.append("Simulation - ").append(System.currentTimeMillis());
        }
        return sb.toString();
    }
}
