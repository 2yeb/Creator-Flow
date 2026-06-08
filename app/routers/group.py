from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import ProjectGroup, ProjectGroupMap
from app.core.security import get_current_user_id
import uuid

router = APIRouter(tags=["그룹"])

# 내 그룹 목록 조회
@router.get("/groups")
def get_groups(db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    groups = db.query(ProjectGroup).filter(ProjectGroup.user_id == user_id).all()
    return groups

# 그룹 생성
@router.post("/groups")
def create_group(keyword: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    group = ProjectGroup(
        id=str(uuid.uuid4()),
        user_id=user_id,
        keyword=keyword
    )
    db.add(group)
    db.commit()
    db.refresh(group)
    return group

# 그룹 삭제
@router.delete("/groups/{group_id}")
def delete_group(group_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    group = db.query(ProjectGroup).filter(ProjectGroup.id == group_id).first()
    if not group:
        raise HTTPException(status_code=404, detail="그룹을 찾을 수 없습니다")
    db.query(ProjectGroupMap).filter(ProjectGroupMap.group_id == group_id).delete()
    db.delete(group)
    db.commit()
    return {"message": "삭제 완료"}

# 프로젝트에 그룹 태그
@router.post("/projects/{project_id}/groups")
def tag_group(project_id: str, group_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    map = ProjectGroupMap(project_id=project_id, group_id=group_id)
    db.add(map)
    db.commit()
    return {"message": "태그 완료"}

# 프로젝트 그룹 태그 조회
@router.get("/projects/{project_id}/groups")
def get_project_groups(project_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    maps = db.query(ProjectGroupMap).filter(ProjectGroupMap.project_id == project_id).all()
    group_ids = [m.group_id for m in maps]
    groups = db.query(ProjectGroup).filter(ProjectGroup.id.in_(group_ids)).all()
    return groups

# 프로젝트 그룹 태그 해제
@router.delete("/projects/{project_id}/groups/{group_id}")
def untag_group(project_id: str, group_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    map = db.query(ProjectGroupMap).filter(
        ProjectGroupMap.project_id == project_id,
        ProjectGroupMap.group_id == group_id
    ).first()
    if not map:
        raise HTTPException(status_code=404, detail="태그를 찾을 수 없습니다")
    db.delete(map)
    db.commit()
    return {"message": "태그 해제 완료"}