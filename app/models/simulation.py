import uuid
from sqlalchemy import Column, String, DateTime, Integer, ForeignKey
from sqlalchemy.dialects.mysql import CHAR
from datetime import datetime
from app.database import Base

class Simulation(Base):
    __tablename__ = "simulation"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id = Column(CHAR(36), ForeignKey("user.id"), nullable=False)
    project_id = Column(CHAR(36), ForeignKey("project.id"), nullable=True)
    actual_quantity = Column(Integer, nullable=True)
    vendor_product_id = Column(CHAR(36), ForeignKey("vendor_product.id"), nullable=False)
    platform_plan_id = Column(CHAR(36), ForeignKey("platform_plan.id"), nullable=False)
    model_type = Column(String(50), nullable=False)
    quantity = Column(Integer, nullable=False)
    selling_price = Column(Integer, nullable=False)
    shipping_fee_buyer = Column(Integer, nullable=False)
    shipping_type = Column(String(50), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    target_quantity = Column(Integer, nullable=True)


class SimulationOption(Base):
    __tablename__ = "simulation_option"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    simulation_id = Column(CHAR(36), ForeignKey("simulation.id"), nullable=False)
    product_option_id = Column(CHAR(36), ForeignKey("product_option.id"), nullable=False)