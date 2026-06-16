import uuid
from sqlalchemy import Column, String, DateTime
from sqlalchemy.dialects.mysql import CHAR
from datetime import datetime
from app.database import Base

class User(Base):
    __tablename__ = "user"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    email = Column(String(255), nullable=False, unique=True)
    password_hash = Column(String(255), nullable=False)
    name = Column(String(50), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)