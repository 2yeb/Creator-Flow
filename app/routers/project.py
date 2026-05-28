from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import Project
from app.models.simulation import Simulation
from app.core.security import get_current_user_id
import uuid

router = APIRouter(prefix="/projects", tags=["프로젝트"])

# 전체 프로젝트 목록 조회
@router.get("")
def get_projects(db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    projects = db.query(Project).filter(Project.user_id == user_id).all()
    return projects

# 프로젝트 상세 조회
@router.get("/{project_id}")
def get_project(project_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    
    simulations = db.query(Simulation).filter(Simulation.project_id == project_id).all()

    return {
        "id": project.id,
        "name": project.name,
        "status": project.status,
        "created_at": project.created_at,
        "simulations": [
            {
                "id": s.id,
                "quantity": s.quantity,
                "selling_price": s.selling_price,
                "model_type": s.model_type,
                "shipping_type": s.shipping_type,
                "target_quantity": s.target_quantity,
                "actual_quantity": s.actual_quantity,
                "created_at": s.created_at,
            } for s in simulations
        ]
    }

# 프로젝트 수정
@router.put("/{project_id}")
def update_project(project_id: str, name: str, status: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    project.name = name
    project.status = status
    db.commit()
    db.refresh(project)
    return project

# 프로젝트 삭제
@router.delete("/{project_id}")
def delete_project(project_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    db.delete(project)
    db.commit()
    return {"message": "삭제 완료"}