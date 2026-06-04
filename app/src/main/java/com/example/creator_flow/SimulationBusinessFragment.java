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
        // 백엔드 호출 시도 → 성공/실패 무관하게 SimulationFragment로 복귀
        attemptRunSimulationAndCreateProject();
        navigateBackToSimulation();
    }

    /**
     * POST /simulations 호출 (예상 원가 계산 + 시뮬레이션 저장).
     * 성공 시 POST /simulations/{id}/project 연쇄 호출하여 프로젝트 자동 생성.
     * 모든 필수 데이터가 백엔드와 매핑되지 않은 상태라 placeholder 값 사용 — TODO 표시.
     */
    private void attemptRunSimulationAndCreateProject() {
        if (getContext() == null) return;

        // 사용자 선택 시점에 SimulationData에 commit된 실제 ID 사용
        String vendorProductId = SimulationData.vendorProductId;
        String platformPlanId = SimulationData.platformPlanId;

        if (vendorProductId == null || platformPlanId == null) {
            // mock 모드 또는 직접입력 → API 호출 skip
            Toast.makeText(getContext(), "시뮬레이션 저장 완료 (로컬)", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = SimulationData.quantity != null ? SimulationData.quantity : 0;
        int sellingPrice = 0; // 판매가는 프로젝트 페이지에서 입력받음 — 시뮬레이션 시점엔 0
        int shippingFeeBuyer = SimulationData.shippingFeeBuyer != null
                ? SimulationData.shippingFeeBuyer : 0;
        String shippingType = SimulationData.shippingFeeType != null
                ? SimulationData.shippingFeeType : "buyer";
        // 옵션 ID들을 쉼표 join (백엔드는 "uuid1,uuid2" 포맷 기대)
        String selectedOptionIds = (SimulationData.selectedOptionIds != null
                && !SimulationData.selectedOptionIds.isEmpty())
                ? android.text.TextUtils.join(",", SimulationData.selectedOptionIds)
                : "";
        Integer targetQty = SimulationData.targetQuantity;

        RetrofitClient.getApi(requireContext()).runSimulation(
                SimulationData.modelType != null ? SimulationData.modelType : "Business",
                vendorProductId, quantity, platformPlanId,
                sellingPrice, shippingFeeBuyer, shippingType,
                selectedOptionIds, targetQty
        ).enqueue(new Callback<SimulationResultDto>() {
            @Override
            public void onResponse(@NonNull Call<SimulationResultDto> call,
                                   @NonNull Response<SimulationResultDto> resp) {
                if (!isAdded()) return;
                if (resp.isSuccessful() && resp.body() != null) {
                    SimulationResultDto r = resp.body();
                    SimulationData.estimatedCost = r.totalCost;
                    if (r.simulationId != null) {
                        // 연쇄: 프로젝트 생성
                        createProjectFromSimulation(r.simulationId);
                        // 연쇄: 결과 조회 (옵션)
                        fetchSimulationDetail(r.simulationId);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimulationResultDto> call, @NonNull Throwable t) {
                // 실패: 로컬 데이터로만 동작
            }
        });
    }

    /** POST /simulations/{id}/project — 프로젝트 자동 생성 */
    private void createProjectFromSimulation(String simulationId) {
        if (getContext() == null) return;
        String name = buildProjectName();
        RetrofitClient.getApi(requireContext())
                .createProjectFromSimulation(simulationId, name)
                .enqueue(new Callback<ProjectFromSimulationDto>() {
                    @Override
                    public void onResponse(@NonNull Call<ProjectFromSimulationDto> call,
                                           @NonNull Response<ProjectFromSimulationDto> resp) {
                        // 응답 받음 — 프로젝트 ID 저장하거나 알림 정도. 화면은 이미 복귀 중.
                    }

                    @Override
                    public void onFailure(@NonNull Call<ProjectFromSimulationDto> call,
                                          @NonNull Throwable t) {
                        // 무시
                    }
                });
    }

    /**
     * 사용자가 요약 페이지에서 선택한 핵심 값들을 프로젝트 이름에 인코딩.
     * 예: "스티커 100개 · 레드프린팅 · 텀블벅 (5%)"
     * 백엔드 project 엔티티엔 quantity/vendor/platform 필드가 없어서, 이렇게 name으로 우회.
     */
    private String buildProjectName() {
        StringBuilder sb = new StringBuilder();

        // 굿즈 유형 + 수량
        if (SimulationData.goodsType != null) {
            sb.append(SimulationData.goodsType);
        }
        if (SimulationData.quantity != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(SimulationData.quantity).append("개");
        }

        // 제작 업체
        if (SimulationData.vendorName != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(SimulationData.vendorName);
        }

        // 판매 업체 + 수수료
        if (SimulationData.platformName != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(SimulationData.platformName);
            if (SimulationData.platformFee != null) {
                sb.append(" (").append(SimulationData.platformFee).append("%)");
            }
        }

        // 아무것도 없으면 timestamp fallback
        if (sb.length() == 0) {
            sb.append("Simulation - ").append(System.currentTimeMillis());
        }

        return sb.toString();
    }

    /** GET /simulations/{id} — 저장된 시뮬레이션 결과 조회 (현재는 호출만, 데이터 활용 미구현) */
    private void fetchSimulationDetail(String simulationId) {
        if (getContext() == null) return;
        RetrofitClient.getApi(requireContext())
                .getSimulation(simulationId)
                .enqueue(new Callback<SimulationDetailDto>() {
                    @Override
                    public void onResponse(@NonNull Call<SimulationDetailDto> call,
                                           @NonNull Response<SimulationDetailDto> resp) {
                        // 응답 OK. 향후 UI 갱신 시 사용 가능.
                    }

                    @Override
                    public void onFailure(@NonNull Call<SimulationDetailDto> call, @NonNull Throwable t) {
                        // 무시
                    }
                });
    }

    /** 시뮬레이션 흐름 종료 및 홈으로 복귀 */
    private void navigateBackToSimulation() {
        Toast.makeText(getContext(), "시뮬레이션 저장 완료", Toast.LENGTH_SHORT).show();
        SimulationData.reset();

        FragmentManager fm = getParentFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction()
                .replace(R.id.main_fragment, new SimulationFragment())
                .commit();
    }
}
