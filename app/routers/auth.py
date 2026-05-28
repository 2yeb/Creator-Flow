from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User
from app.core.security import hash_password, verify_password, create_access_token, get_current_user_id
from pydantic import BaseModel
import uuid

router = APIRouter(prefix="/auth", tags=["인증"])

class SignupRequest(BaseModel):
    email: str
    password: str
    name: str

class LoginRequest(BaseModel):
    email: str
    password: str

# 회원가입
@router.post("/signup")
def signup(request: SignupRequest, db: Session = Depends(get_db)):
    existing_user = db.query(User).filter(User.email == request.email).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="이미 사용 중인 이메일입니다")
    
    user = User(
        id=str(uuid.uuid4()),
        email=request.email,
        password_hash=hash_password(request.password),
        name=request.name
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return {"message": "회원가입 완료", "user_id": user.id}

# 로그인
@router.post("/login")
def login(request: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == request.email).first()
    if not user:
        raise HTTPException(status_code=401, detail="이메일 또는 비밀번호가 틀렸습니다")
    
    if not verify_password(request.password, user.password_hash):
        raise HTTPException(status_code=401, detail="이메일 또는 비밀번호가 틀렸습니다")
    
    token = create_access_token(user.id)
    return {"access_token": token, "token_type": "bearer"}

# 내 정보 조회
@router.get("/me")
def get_me(db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="유저를 찾을 수 없습니다")
    return {"user_id": user.id, "email": user.email, "name": user.name}

# 내 정보 수정
@router.put("/me")
def update_me(name: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="유저를 찾을 수 없습니다")
    user.name = name
    db.commit()
    db.refresh(user)
    return {"user_id": user.id, "name": user.name}