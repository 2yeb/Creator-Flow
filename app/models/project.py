import uuid
from sqlalchemy import Column, String, DateTime, Integer, Boolean, ForeignKey
from sqlalchemy.dialects.mysql import CHAR
from datetime import datetime
from app.database import Base

class Project(Base):
    __tablename__ = "project"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(CHAR(36), ForeignKey("user.id"), nullable=False)
    name = Column(String(255), nullable=False)
    status = Column(String(50), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)


class ProjectImage(Base):
    __tablename__ = "project_image"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    project_id = Column(CHAR(36), ForeignKey("project.id"), nullable=False)
    image_url = Column(String(500), nullable=False)
    order = Column(Integer, nullable=False)


class ProjectGroup(Base):
    __tablename__ = "project_group"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(CHAR(36), ForeignKey("user.id"), nullable=False)
    keyword = Column(String(100), nullable=False)


class ProjectGroupMap(Base):
    __tablename__ = "project_group_map"

    project_id = Column(CHAR(36), ForeignKey("project.id"), primary_key=True)
    group_id = Column(CHAR(36), ForeignKey("project_group.id"), primary_key=True)


class Todo(Base):
    __tablename__ = "todo"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(CHAR(36), ForeignKey("user.id"), nullable=False)
    content = Column(String(500), nullable=False)
    is_done = Column(Boolean, default=False, nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)