from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.crawling import GoodsType, Vendor, VendorProduct, PriceByQuantity, ProductOption, Platform, PlatformPlan
from app.models.simulation import Simulation, SimulationOption
from app.models.project import Project
import uuid

router = APIRouter(tags=["제작 시뮬레이터"])

TEMP_USER_ID = "test-user-id-1234"

# 굿즈 유형 목록 조회
@router.get("/goods-types")
def get_goods_types(db: Session = Depends(get_db)):
    return db.query(GoodsType).all()

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
    selected_option_ids: str = "",  # 쉼표로 구분된 option id 목록
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
        shipping_type=shipping_type
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
        simulation_id=simulation_id,
        name=name,
        status="active"
    )
    db.add(project)
    db.commit()
    db.refresh(project)

    return {
        "project_id": project.id,
        "name": project.name,
        "simulation_id": simulation_id
    }