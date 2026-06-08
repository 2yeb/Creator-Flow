import uuid
from sqlalchemy import Column, String, DateTime, Integer, Boolean, Float, ForeignKey
from sqlalchemy.dialects.mysql import CHAR
from datetime import datetime
from app.database import Base

class GoodsType(Base):
    __tablename__ = "goods_type"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(100), nullable=False)

class GoodsDetailType(Base):
    __tablename__ = "goods_detail_type"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    goods_type_id = Column(CHAR(36), ForeignKey("goods_type.id"), nullable=False)
    name = Column(String(100), nullable=False)

class Vendor(Base):
    __tablename__ = "vendor"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(100), nullable=False)
    site_url = Column(String(500), nullable=False)
    logo_url = Column(String(500), nullable=True)
    crawled_at = Column(DateTime, default=datetime.utcnow, nullable=False)


class VendorProduct(Base):
    __tablename__ = "vendor_product"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    vendor_id = Column(CHAR(36), ForeignKey("vendor.id"), nullable=False)
    name = Column(String(100), nullable=False)  # 추가
    goods_type_id = Column(CHAR(36), ForeignKey("goods_type.id"), nullable=False)
    goods_detail_type_id = Column(CHAR(36), ForeignKey("goods_detail_type.id"), nullable=True)
    min_quantity = Column(Integer, nullable=False)
    shipping_fee = Column(Integer, nullable=False)
    free_shipping_min = Column(Integer, nullable=True)


class PriceByQuantity(Base):
    __tablename__ = "price_by_quantity"


    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    vendor_product_id = Column(CHAR(36), ForeignKey("vendor_product.id"), nullable=False)
    quantity = Column(Integer, nullable=False)
    unit_price = Column(Integer, nullable=False)


class ProductOption(Base):
    __tablename__ = "product_option"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    vendor_product_id = Column(CHAR(36), ForeignKey("vendor_product.id"), nullable=False)
    option_name = Column(String(100), nullable=False)
    option_value = Column(String(100), nullable=False)
    extra_price = Column(Integer, nullable=False)


class Platform(Base):
    __tablename__ = "platform"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(100), nullable=False)
    supports_fulfillment = Column(Boolean, nullable=False)
    supports_pod = Column(Boolean, nullable=False)


class PlatformPlan(Base):
    __tablename__ = "platform_plan"

    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    platform_id = Column(CHAR(36), ForeignKey("platform.id"), nullable=False)
    plan_name = Column(String(100), nullable=False)
    fee_rate = Column(Float, nullable=False)
    description = Column(String(500), nullable=True)