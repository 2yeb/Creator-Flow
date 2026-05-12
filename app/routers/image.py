from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import ProjectImage
import uuid
import os
import shutil

router = APIRouter(tags=["프로젝트 이미지"])

# 이미지 저장 폴더
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

# 이미지 업로드
@router.post("/projects/{project_id}/images")
def upload_image(project_id: str, image: UploadFile = File(...), db: Session = Depends(get_db)):
    # 파일 확장자 확인
    ext = image.filename.split(".")[-1].lower()
    if ext not in ["jpg", "jpeg", "png", "gif", "webp"]:
        raise HTTPException(status_code=400, detail="이미지 파일만 업로드 가능합니다")

    # 파일 저장
    image_id = str(uuid.uuid4())
    filename = f"{image_id}.{ext}"
    file_path = os.path.join(UPLOAD_DIR, filename)

    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(image.file, buffer)

    # 현재 프로젝트 이미지 개수 (순서 지정용)
    count = db.query(ProjectImage).filter(ProjectImage.project_id == project_id).count()

    # DB 저장
    project_image = ProjectImage(
        id=image_id,
        project_id=project_id,
        image_url=f"/uploads/{filename}",
        order=count + 1
    )
    db.add(project_image)
    db.commit()
    db.refresh(project_image)

    return {
        "image_id": project_image.id,
        "image_url": project_image.image_url
    }

# 이미지 삭제
@router.delete("/projects/{project_id}/images/{image_id}")
def delete_image(project_id: str, image_id: str, db: Session = Depends(get_db)):
    project_image = db.query(ProjectImage).filter(
        ProjectImage.id == image_id,
        ProjectImage.project_id == project_id
    ).first()
    if not project_image:
        raise HTTPException(status_code=404, detail="이미지를 찾을 수 없습니다")

    # 파일 삭제
    file_path = project_image.image_url.replace("/uploads/", "uploads/")
    if os.path.exists(file_path):
        os.remove(file_path)

    db.delete(project_image)
    db.commit()
    return {"message": "삭제 완료"}