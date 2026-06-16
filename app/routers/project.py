from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from pydantic import BaseModel
from app.database import get_db
from app.models.project import Project, ProjectImage, ProjectGroupMap
from app.models.simulation import Simulation, SimulationOption
from app.models.crawling import VendorProduct, Vendor, PlatformPlan, PriceByQuantity
from app.core.security import get_current_user_id

router = APIRouter(prefix="/projects", tags=["프로젝트"])

class UpdateProjectRequest(BaseModel):
    name: str
    status: str

@router.get("")
def get_projects(db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    projects = db.query(Project).filter(Project.user_id == user_id).all()
    return projects

@router.get("/{project_id}")
def get_project(project_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    
    simulations = db.query(Simulation).filter(Simulation.project_id == project_id).all()

    simulation_list = []
    for s in simulations:
        vendor_product = db.query(VendorProduct).filter(VendorProduct.id == s.vendor_product_id).first()
        vendor = db.query(Vendor).filter(Vendor.id == vendor_product.vendor_id).first() if vendor_product else None
        plan = db.query(PlatformPlan).filter(PlatformPlan.id == s.platform_plan_id).first()
        price_row = db.query(PriceByQuantity).filter(
            PriceByQuantity.vendor_product_id == s.vendor_product_id,
            PriceByQuantity.quantity <= s.quantity
        ).order_by(PriceByQuantity.quantity.desc()).first()

        unit_cost = price_row.unit_price if price_row else 0
        total_cost = unit_cost * s.quantity
        total_revenue = s.selling_price * s.quantity
        total_fee = total_revenue * (plan.fee_rate / 100) if plan else 0
        net_profit = total_revenue - total_cost - total_fee - (vendor_product.shipping_fee if vendor_product else 0)

        simulation_list.append({
            "id": s.id,
            "quantity": s.quantity,
            "selling_price": s.selling_price,
            "model_type": s.model_type,
            "shipping_type": s.shipping_type,
            "target_quantity": s.target_quantity,
            "actual_quantity": s.actual_quantity,
            "created_at": s.created_at,
            "vendor_name": vendor.name if vendor else None,
            "platform_plan": plan.plan_name if plan else None,
            "fee_rate": plan.fee_rate if plan else None,
            "unit_cost": unit_cost,
            "total_cost": total_cost,
            "net_profit": round(net_profit),
        })

    return {
        "id": project.id,
        "name": project.name,
        "status": project.status,
        "created_at": project.created_at,
        "simulations": simulation_list
    }

@router.put("/{project_id}")
def update_project(project_id: str, request: UpdateProjectRequest, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    project.name = request.name
    project.status = request.status
    db.commit()
    db.refresh(project)
    return project

@router.delete("/{project_id}")
def delete_project(project_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    
    simulations = db.query(Simulation).filter(Simulation.project_id == project_id).all()
    for s in simulations:
        db.query(SimulationOption).filter(SimulationOption.simulation_id == s.id).delete()
    db.query(Simulation).filter(Simulation.project_id == project_id).delete()
    
    db.query(ProjectGroupMap).filter(ProjectGroupMap.project_id == project_id).delete()
    db.query(ProjectImage).filter(ProjectImage.project_id == project_id).delete()
    
    db.delete(project)
    db.commit()
    return {"message": "삭제 완료"}