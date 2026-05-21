from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.crawling import GoodsType, GoodsDetailType, Vendor, VendorProduct, PriceByQuantity, ProductOption, Platform, PlatformPlan
from app.models.simulation import Simulation, SimulationOption
from app.models.project import Project
import uuid

router = APIRouter(tags=["제작 시뮬레이터"])

TEMP_USER_ID = "test-user-id-1234"

# 굿즈 유형 목록 조회
@router.get("/goods-types")
def get_goods_types(db: Session = Depends(get_db)):
    return db.query(GoodsType).all()

# 굿즈 유형별 세부 유형 조회
@router.get("/goods-types/{goods_type_id}/details")
def get_goods_detail_types(goods_type_id: str, db: Session = Depends(get_db)):
    details = db.query(GoodsDetailType).filter(GoodsDetailType.goods_type_id == goods_type_id).all()
    return details

# 업체별 상품 목록 조회 (세부 유형 필터)
@router.get("/vendors/{vendor_id}/products")
def get_vendor_products(vendor_id: str, goods_detail_type_id: str = None, db: Session = Depends(get_db)):
    query = db.query(VendorProduct).filter(VendorProduct.vendor_id == vendor_id)
    if goods_detail_type_id:
        query = query.filter(VendorProduct.goods_detail_type_id == goods_detail_type_id)
    return query.all()

# 상품별 옵션 목록 조회
@router.get("/vendor-products/{vendor_product_id}/options")
def get_vendor_product_options(vendor_product_id: str, db: Session = Depends(get_db)):
    options = db.query(ProductOption).filter(ProductOption.vendor_product_id == vendor_product_id).all()
    return options

# 업체 목록 조회 (굿즈 유형 필터)
@router.get("/vendors")
def get_vendors(goods_type_id: str = None, db: Session = Depends(get_db)):
    if goods_type_id:
        vendor_product_ids = db.query(VendorProduct).filter(
            VendorProduct.goods_type_id == goods_type_id
        ).all()
        vendor_ids = [vp.vendor_id for vp in vendor_product_ids]
        return db.query(Vendor).filter(Vendor.id.in_(vendor_ids)).all()
    return db.query(Vendor).all()

# 판매 플랫폼 목록 조회
@router.get("/platforms")
def get_platforms(supports_fulfillment: bool = None, supports_pod: bool = None, db: Session = Depends(get_db)):
    query = db.query(Platform)
    if supports_fulfillment is not None:
        query = query.filter(Platform.supports_fulfillment == supports_fulfillment)
    if supports_pod is not None:
        query = query.filter(Platform.supports_pod == supports_pod)
    return query.all()

# 플랫폼별 요금제 조회
@router.get("/platforms/{platform_id}/plans")
def get_platform_plans(platform_id: str, db: Session = Depends(get_db)):
    plans = db.query(PlatformPlan).filter(PlatformPlan.platform_id == platform_id).all()
    if not plans:
        raise HTTPException(status_code=404, detail="요금제를 찾을 수 없습니다")
    return plans

# 시뮬레이션 실행 및 저장
@router.post("/simulations")
def run_simulation(
    model_type: str,
    vendor_product_id: str,
    quantity: int,
    platform_plan_id: str,
    selling_price: int,
    shipping_fee_buyer: int,
    shipping_type: str,
    project_id: str = None,
    actual_quantity: int = None,
    selected_option_ids: str = "",
    target_quantity: int = None,
    db: Session = Depends(get_db)
):
    # 업체 상품 조회
    vendor_product = db.query(VendorProduct).filter(VendorProduct.id == vendor_product_id).first()
    if not vendor_product:
        raise HTTPException(status_code=404, detail="업체 상품을 찾을 수 없습니다")

    # 수량별 단가 조회 (입력 수량 이하 중 가장 큰 구간)
    price_row = db.query(PriceByQuantity).filter(
        PriceByQuantity.vendor_product_id == vendor_product_id,
        PriceByQuantity.quantity <= quantity
    ).order_by(PriceByQuantity.quantity.desc()).first()

    if not price_row:
        raise HTTPException(status_code=400, detail="해당 수량에 맞는 단가가 없습니다")

    unit_cost = price_row.unit_price

    # 옵션 추가금 계산
    option_extra = 0
    option_id_list = [o for o in selected_option_ids.split(",") if o]
    for option_id in option_id_list:
        option = db.query(ProductOption).filter(ProductOption.id == option_id).first()
        if option:
            option_extra += option.extra_price

    unit_cost += option_extra

    # 플랫폼 요금제 조회
    plan = db.query(PlatformPlan).filter(PlatformPlan.id == platform_plan_id).first()
    if not plan:
        raise HTTPException(status_code=404, detail="플랫폼 요금제를 찾을 수 없습니다")

    # 계산
    total_cost = unit_cost * quantity
    total_revenue = selling_price * quantity
    total_fee = total_revenue * (plan.fee_rate / 100)
    total_shipping = vendor_product.shipping_fee
    net_profit = total_revenue - total_cost - total_fee - total_shipping
    revenue_rate = round((net_profit / total_revenue) * 100, 1)
    recommended_price = round((unit_cost + (net_profit / quantity)) / (1 - plan.fee_rate / 100))
    break_even_quantity = round(total_shipping / (selling_price - unit_cost - (selling_price * plan.fee_rate / 100)))

    # 시뮬레이션 저장
    simulation = Simulation(
        id=str(uuid.uuid4()),
        user_id=TEMP_USER_ID,
        vendor_product_id=vendor_product_id,
        platform_plan_id=platform_plan_id,
        model_type=model_type,
        quantity=quantity,
        selling_price=selling_price,
        shipping_fee_buyer=shipping_fee_buyer,
        shipping_type=shipping_type,
        target_quantity=target_quantity,
        project_id=project_id,
        actual_quantity=actual_quantity
    )
    db.add(simulation)
    db.commit()
    db.refresh(simulation)

    # 선택 옵션 저장
    for option_id in option_id_list:
        sim_option = SimulationOption(
            id=str(uuid.uuid4()),
            simulation_id=simulation.id,
            product_option_id=option_id
        )
        db.add(sim_option)
    db.commit()

    return {
        "simulation_id": simulation.id,
        "unit_cost": unit_cost,
        "total_cost": total_cost,
        "recommended_price": recommended_price,
        "expected_revenue": int(total_revenue),
        "revenue_rate": revenue_rate,
        "break_even_quantity": break_even_quantity
    }

# 시뮬레이션 결과로 프로젝트 생성
@router.post("/simulations/{simulation_id}/project")
def create_project_from_simulation(simulation_id: str, name: str, db: Session = Depends(get_db)):
    simulation = db.query(Simulation).filter(Simulation.id == simulation_id).first()
    if not simulation:
        raise HTTPException(status_code=404, detail="시뮬레이션을 찾을 수 없습니다")

    project = Project(
        id=str(uuid.uuid4()),
        user_id=TEMP_USER_ID,
        name=name,
        status="active"
    )
    db.add(project)
    db.commit()
    db.refresh(project)

    # 생성된 프로젝트 id를 시뮬레이션에 연결
    simulation.project_id = project.id
    db.commit()

    return {
        "project_id": project.id,
        "name": project.name,
        "simulation_id": simulation_id
    }

# 시뮬레이션 결과 조회
@router.get("/simulations/{simulation_id}")
def get_simulation(simulation_id: str, db: Session = Depends(get_db)):
    simulation = db.query(Simulation).filter(Simulation.id == simulation_id).first()
    if not simulation:
        raise HTTPException(status_code=404, detail="시뮬레이션을 찾을 수 없습니다")
    
    # 수량별 단가 조회
    price_row = db.query(PriceByQuantity).filter(
        PriceByQuantity.vendor_product_id == simulation.vendor_product_id,
        PriceByQuantity.quantity <= simulation.quantity
    ).order_by(PriceByQuantity.quantity.desc()).first()

    # 플랫폼 요금제 조회
    plan = db.query(PlatformPlan).filter(PlatformPlan.id == simulation.platform_plan_id).first()

    # 업체 조회
    vendor_product = db.query(VendorProduct).filter(VendorProduct.id == simulation.vendor_product_id).first()
    vendor = db.query(Vendor).filter(Vendor.id == vendor_product.vendor_id).first()

    unit_cost = price_row.unit_price if price_row else 0
    total_cost = unit_cost * simulation.quantity
    total_revenue = simulation.selling_price * simulation.quantity
    total_fee = total_revenue * (plan.fee_rate / 100)
    net_profit = total_revenue - total_cost - total_fee - vendor_product.shipping_fee
    revenue_rate = round((net_profit / total_revenue) * 100, 1)
    break_even_quantity = round(vendor_product.shipping_fee / (simulation.selling_price - unit_cost - (simulation.selling_price * plan.fee_rate / 100)))  # ← 여기 추가

    return {
        "simulation_id": simulation.id,
        "created_at": simulation.created_at,
        "vendor_name": vendor.name,
        "platform_plan": plan.plan_name,
        "fee_rate": plan.fee_rate,
        "model_type": simulation.model_type,
        "quantity": simulation.quantity,
        "selling_price": simulation.selling_price,
        "shipping_type": simulation.shipping_type,
        "target_quantity": simulation.target_quantity,
        "actual_quantity": simulation.actual_quantity,  # ← 여기 추가
        "unit_cost": unit_cost,
        "total_cost": total_cost,
        "expected_revenue": int(total_revenue),
        "total_fee": round(total_fee),
        "net_profit": round(net_profit),
        "revenue_rate": revenue_rate,
        "break_even_quantity": break_even_quantity  # ← 여기 추가
    }