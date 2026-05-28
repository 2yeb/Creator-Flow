from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
from typing import Optional
from fastapi import Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer

# 비밀번호 해시 설정
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# JWT 설정
SECRET_KEY = "creator-flow-secret-key-change-in-production"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7  # 7일

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

def hash_password(password: str) -> str:
    # 평문 비밀번호를 해시로 변환
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    # 입력한 비밀번호와 해시 비교
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(user_id: str) -> str:
    # JWT 토큰 생성
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    data = {"sub": user_id, "exp": expire}
    return jwt.encode(data, SECRET_KEY, algorithm=ALGORITHM)

def decode_access_token(token: str) -> Optional[str]:
    # JWT 토큰 검증 후 user_id 반환
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload.get("sub")
    except JWTError:
        return None
    
def get_current_user_id(token: str = Depends(oauth2_scheme)) -> str:
    user_id = decode_access_token(token)
    if not user_id:
        raise HTTPException(status_code=401, detail="유효하지 않은 토큰입니다")
    return user_id