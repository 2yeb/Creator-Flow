from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import ProjectImage
from app.core.security import get_current_user_id
import uuid
import os
import shutil

router = APIRouter(tags=["프로젝트 이미지"])

UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/projects/{project_id}/images")
def upload_image(
    project_id: str,
    image: UploadFile = File(...),
    simulation_id: str = None,
    db: Session = Depends(get_db),
    user_id: str = Depends(get_current_user_id)
):
    ext = image.filename.split(".")[-1].lower()
    if ext not in ["jpg", "jpeg", "png", "gif", "webp"]:
        raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다")

    image_id = str(uuid.uuid4())
    filename = f"{image_id}.{ext}"
    file_path = os.path.join(UPLOAD_DIR, filename)

    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(image.file, buffer)

    count = db.query(ProjectImage).filter(ProjectImage.project_id == project_id).count()

    project_image = ProjectImage(
        id=image_id,
        project_id=project_id,
        simulation_id=simulation_id,
        image_url=f"/uploads/{filename}",
        order=count + 1
    )
    db.add(project_image)
    db.commit()
    db.refresh(project_image)

    return {
        "image_id": project_image.id,
        "image_url": project_image.image_url,
        "simulation_id": project_image.simulation_id
    }

@router.get("/projects/{project_id}/images")
def get_images(project_id: str, simulation_id: str = None, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    query = db.query(ProjectImage).filter(ProjectImage.project_id == project_id)
    if simulation_id:
        query = query.filter(ProjectImage.simulation_id == simulation_id)
    return query.order_by(ProjectImage.order).all()

@router.delete("/projects/{project_id}/images/{image_id}")
def delete_image(project_id: str, image_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    project_image = db.query(ProjectImage).filter(
        ProjectImage.id == image_id,
        ProjectImage.project_id == project_id
    ).first()
    if not project_image:
        raise HTTPException(status_code=404, detail="이미지를 찾을 수 없습니다")

    file_path = project_image.image_url.replace("/uploads/", "uploads/")
    if os.path.exists(file_path):
        os.remove(file_path)

    db.delete(project_image)
    db.commit()
    return {"message": "삭제 완료"}