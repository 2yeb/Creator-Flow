from fastapi import FastAPI
from app.database import Base, engine
from app import models
from app.routers import project, group, todo, simulation
from app.routers import image as image_router
from app.routers import auth
from sqlalchemy import text

app = FastAPI(
    title="Creator-Flow API",
    description="굿즈 창작자를 위한 제작 가이드 앱",
    version="0.1.0"
)

from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], #프론트 주소
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

Base.metadata.create_all(bind=engine)

app.include_router(project.router)
app.include_router(group.router)
app.include_router(todo.router)
app.include_router(simulation.router)
app.include_router(image_router.router)
app.include_router(auth.router)

@app.get("/")
def root():
    return {"message": "Creator-Flow API 서버 실행 중"}

@app.post("/calculate/price")
def calculate_price(cost: float, target_profit: float, fee_rate: float):
    price = (cost + target_profit) / (1 - fee_rate)
    return {
        "recommended_price": round(price),
        "profit_rate": round((target_profit / price) * 100, 1)
    }

@app.post("/simulate")
def simulate(
    quantity: int,
    production_cost: float,
    selling_price: float,
    shipping_cost: float,
    fee_rate: float
):
    total_production = production_cost * quantity
    total_revenue = selling_price * quantity
    total_shipping = shipping_cost * quantity
    total_fee = total_revenue * fee_rate
    net_profit = total_revenue - total_production - total_shipping - total_fee
    profit_rate = round((net_profit / total_revenue) * 100, 1)

    return {
        "total_production_cost": total_production,
        "total_revenue": total_revenue,
        "total_fee": round(total_fee),
        "net_profit": round(net_profit),
        "profit_rate": profit_rate
    }

@app.get("/db-test")
def db_test():
    with engine.connect() as conn:
        result = conn.execute(text("SELECT 1"))
        return {"db": "연결 성공!"}