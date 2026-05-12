from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import Project
import uuid

router = APIRouter(prefix="/projects", tags=["프로젝트"])

# 임시 user_id (나중에 JWT 인증으로 교체 예정)
TEMP_USER_ID = "test-user-id-1234"

# 전체 프로젝트 목록 조회
@router.get("")
def get_projects(db: Session = Depends(get_db)):
    projects = db.query(Project).filter(Project.user_id == TEMP_USER_ID).all()
    return projects

# 프로젝트 상세 조회
@router.get("/{project_id}")
def get_project(project_id: str, db: Session = Depends(get_db)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    return project

# 프로젝트 수정
@router.put("/{project_id}")
def update_project(project_id: str, name: str, status: str, db: Session = Depends(get_db)):
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
def delete_project(project_id: str, db: Session = Depends(get_db)):
    project = db.query(Project).filter(Project.id == project_id).first()
    if not project:
        raise HTTPException(status_code=404, detail="프로젝트를 찾을 수 없습니다")
    db.delete(project)
    db.commit()
    return {"message": "삭제 완료"}