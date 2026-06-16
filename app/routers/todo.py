from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import Todo
from app.core.security import get_current_user_id
import uuid

router = APIRouter(tags=["To-Do"])

# 전체 To-Do 조회
@router.get("/todos")
def get_todos(db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    todos = db.query(Todo).filter(Todo.user_id == user_id).all()
    return todos

# To-Do 생성
@router.post("/todos")
def create_todo(content: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    todo = Todo(
        id=str(uuid.uuid4()),
        user_id=user_id,
        content=content,
        is_done=False
    )
    db.add(todo)
    db.commit()
    db.refresh(todo)
    return todo

# To-Do 수정 / 완료 처리
@router.put("/todos/{todo_id}")
def update_todo(todo_id: str, content: str = None, is_done: bool = None, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    todo = db.query(Todo).filter(Todo.id == todo_id).first()
    if not todo:
        raise HTTPException(status_code=404, detail="To-Do를 찾을 수 없습니다")
    if content is not None:
        todo.content = content
    if is_done is not None:
        todo.is_done = is_done
    db.commit()
    db.refresh(todo)
    return todo

# To-Do 삭제
@router.delete("/todos/{todo_id}")
def delete_todo(todo_id: str, db: Session = Depends(get_db), user_id: str = Depends(get_current_user_id)):
    todo = db.query(Todo).filter(Todo.id == todo_id).first()
    if not todo:
        raise HTTPException(status_code=404, detail="To-Do를 찾을 수 없습니다")
    db.delete(todo)
    db.commit()
    return {"message": "삭제 완료"}