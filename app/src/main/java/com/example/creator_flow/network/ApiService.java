package com.example.creator_flow.network;

import com.example.creator_flow.model.GoodsDetailTypeDto;
import com.example.creator_flow.model.GoodsTypeDto;
import com.example.creator_flow.model.ImageUploadResponse;
import com.example.creator_flow.model.PlatformDto;
import com.example.creator_flow.model.PlatformPlanDto;
import com.example.creator_flow.model.ProductOptionDto;
import com.example.creator_flow.model.ProjectFromSimulationDto;
import com.example.creator_flow.model.ProjectImageDto;
import com.example.creator_flow.model.ProjectResponse;
import com.example.creator_flow.model.SimulationDetailDto;
import com.example.creator_flow.model.SimulationResultDto;
import com.example.creator_flow.model.UpdateProjectRequest;
import com.example.creator_flow.model.VendorDto;
import com.example.creator_flow.model.VendorProductDto;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ============================================================
    // 프로젝트
    // ============================================================

    @GET("projects/{id}")
    Call<ProjectResponse> getProject(@Path("id") String projectId);

    /**
     * 프로젝트 수정 — 백엔드는 Pydantic UpdateProjectRequest (JSON body) 받음.
     * {@code def update_project(project_id, request: UpdateProjectRequest, ...)}
     */
    @PUT("projects/{id}")
    Call<ProjectResponse> updateProject(
            @Path("id") String projectId,
            @Body UpdateProjectRequest body);

    /** 프로젝트 삭제 — 응답 바디: {"message": "삭제 완료"} */
    @DELETE("projects/{id}")
    Call<JsonObject> deleteProject(@Path("id") String projectId);

    // ============================================================
    // 시뮬레이션 (차시) - 수정/삭제 (2026-06 백엔드 업데이트)
    // ============================================================

    /**
     * 시뮬레이션(차시) 수정 — 사용자가 프로젝트 페이지 EditText에서 수정한 값 반영.
     * 백엔드: PUT /simulations/{id}?quantity=...&selling_price=...&...
     * 각 인자는 null 가능 (변경 안 할 필드는 보내지 말라는 뜻).
     */
    @PUT("simulations/{simulation_id}")
    Call<SimulationDetailDto> updateSimulation(
            @Path("simulation_id") String simulationId,
            @Query("quantity") Integer quantity,
            @Query("selling_price") Integer sellingPrice,
            @Query("actual_quantity") Integer actualQuantity,
            @Query("target_quantity") Integer targetQuantity);

    /** 시뮬레이션(차시) 삭제 — DELETE /simulations/{id} */
    @DELETE("simulations/{simulation_id}")
    Call<JsonObject> deleteSimulation(@Path("simulation_id") String simulationId);

    // ============================================================
    // 프로젝트 이미지
    // ============================================================

    /**
     * 이미지 업로드.
     * 2026-06 백엔드 업데이트로 simulation_id 옵션 파라미터 추가 — 현재 보고 있는
     * 차시의 simulation id를 함께 보내면 그 차시 소속 이미지로 저장됨.
     * null이면 어느 차시에도 속하지 않은 프로젝트 단위 이미지가 됨.
     */
    @Multipart
    @POST("projects/{id}/images")
    Call<ImageUploadResponse> uploadImage(
            @Path("id") String projectId,
            @Part MultipartBody.Part image,
            @Query("simulation_id") String simulationId);

    @DELETE("projects/{id}/images/{image_id}")
    Call<JsonObject> deleteImage(
            @Path("id") String projectId,
            @Path("image_id") String imageId);

    /**
     * 프로젝트 이미지 목록 조회 (2026-06-06 백엔드 추가).
     *
     * @param projectId    프로젝트 UUID
     * @param simulationId 옵션 — 특정 차시 이미지만 필터링. null이면 프로젝트 전체 이미지.
     * @return order 오름차순으로 정렬된 이미지 목록
     */
    @GET("projects/{project_id}/images")
    Call<List<ProjectImageDto>> getProjectImages(
            @Path("project_id") String projectId,
            @Query("simulation_id") String simulationId);

    // ============================================================
    // 시뮬레이션 - 조회
    // ============================================================

    /** 굿즈 유형 목록 (스티커/포스터/엽서 등) */
    @GET("goods-types")
    Call<List<GoodsTypeDto>> getGoodsTypes();

    /** 굿즈 유형별 세부 유형 (예: 스티커 → 띠부띠부/완칼/반칼/조각) */
    @GET("goods-types/{goods_type_id}/details")
    Call<List<GoodsDetailTypeDto>> getGoodsDetailTypes(
            @Path("goods_type_id") String goodsTypeId);

    /** 업체 목록 (옵션으로 굿즈 유형 필터) */
    @GET("vendors")
    Call<List<VendorDto>> getVendors(
            @Query("goods_type_id") String goodsTypeId);

    /** 업체의 상품 목록 (옵션으로 세부 유형 필터) */
    @GET("vendors/{vendor_id}/products")
    Call<List<VendorProductDto>> getVendorProducts(
            @Path("vendor_id") String vendorId,
            @Query("goods_detail_type_id") String goodsDetailTypeId);

    /** 상품의 옵션 목록 (예: 용지, 코팅, 재단) */
    @GET("vendor-products/{vendor_product_id}/options")
    Call<List<ProductOptionDto>> getVendorProductOptions(
            @Path("vendor_product_id") String vendorProductId);

    /** 판매 플랫폼 목록 */
    @GET("platforms")
    Call<List<PlatformDto>> getPlatforms(
            @Query("supports_fulfillment") Boolean supportsFulfillment,
            @Query("supports_pod") Boolean supportsPod);

    /** 플랫폼별 요금제 */
    @GET("platforms/{platform_id}/plans")
    Call<List<PlatformPlanDto>> getPlatformPlans(
            @Path("platform_id") String platformId);

    /** 시뮬레이션 결과 조회 — 차시별 상세 데이터 (vendor_name, plan_name, unit_cost 등) */
    @GET("simulations/{simulation_id}")
    Call<SimulationDetailDto> getSimulation(@Path("simulation_id") String simulationId);

    // ============================================================
    // 시뮬레이션 - 실행/저장
    // ============================================================

    /**
     * 시뮬레이션 계산 및 저장.
     * (FastAPI 기본 동작상 POST 본문이 아니라 쿼리 파라미터로 받음)
     *
     * @param modelType "Business" | "Fanart"
     * @param vendorProductId 선택한 업체 상품 ID
     * @param quantity 수량
     * @param platformPlanId 플랫폼 요금제 ID
     * @param sellingPrice 판매가 (원)
     * @param shippingFeeBuyer 구매자 부담 배송비
     * @param shippingType 배송 방식
     * @param selectedOptionIds 쉼표 구분 옵션 ID들 (예: "uuid1,uuid2")
     * @param targetQuantity 목표 판매량 (선택)
     */
    @POST("simulations")
    Call<SimulationResultDto> runSimulation(
            @Query("model_type") String modelType,
            @Query("vendor_product_id") String vendorProductId,
            @Query("quantity") int quantity,
            @Query("platform_plan_id") String platformPlanId,
            @Query("selling_price") int sellingPrice,
            @Query("shipping_fee_buyer") int shippingFeeBuyer,
            @Query("shipping_type") String shippingType,
            @Query("selected_option_ids") String selectedOptionIds,
            @Query("target_quantity") Integer targetQuantity,
            /**
             * 기존 프로젝트에 차시 추가 모드일 때 그 프로젝트 ID 전달.
             * null이면 별도 프로젝트 없는 simulation row만 생성됨 (이후
             * POST /simulations/{simulation_id}/project로 새 프로젝트 만들 수 있음).
             */
            @Query("project_id") String projectId);

    /** 시뮬레이션 결과로 프로젝트 생성 */
    @POST("simulations/{simulation_id}/project")
    Call<ProjectFromSimulationDto> createProjectFromSimulation(
            @Path("simulation_id") String simulationId,
            @Query("name") String projectName);
}
