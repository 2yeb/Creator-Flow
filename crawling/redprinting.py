from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import time
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.database import SessionLocal
from app.models.crawling import VendorProduct, PriceByQuantity
import uuid

def crawl_redprinting_acrylic_keyring():
    options = webdriver.ChromeOptions()
    driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()))
    wait = WebDriverWait(driver, 10)

    db = SessionLocal()

    try:
        driver.get("https://www.redprinting.co.kr/ko/product/item/AC/ACTHDKY")
        time.sleep(3)

        # 가격표 버튼 클릭
        price_btn = wait.until(EC.element_to_be_clickable((By.XPATH, '//*[contains(text(), "가격표")]')))
        price_btn.click()
        time.sleep(2)

        # 수량, 금액, 개당단가 읽기
        wait.until(EC.presence_of_element_located((By.ID, 'id_price_data')))
        uls = driver.find_elements(By.CSS_SELECTOR, '#id_price_data ul')

        quantities = [li.text.replace(',', '') for li in uls[0].find_elements(By.TAG_NAME, 'li')[1:]]
        unit_prices = [li.text.replace(',', '') for li in uls[2].find_elements(By.TAG_NAME, 'li')[1:]]

        print("수량:", quantities)
        print("단가:", unit_prices)

        # VENDOR_PRODUCT 저장
        vendor_product_id = str(uuid.uuid4())
        vendor_product = VendorProduct(
            id=vendor_product_id,
            vendor_id='vendor-redprinting',
            goods_type_id='gt-acrylic',
            goods_detail_type_id='gdt-acrylic-keyring',
            min_quantity=int(quantities[0]),
            shipping_fee=3000,
            free_shipping_min=None
        )
        db.add(vendor_product)
        db.commit()

        # PRICE_BY_QUANTITY 저장
        for qty, price in zip(quantities, unit_prices):
            price_row = PriceByQuantity(
                id=str(uuid.uuid4()),
                vendor_product_id=vendor_product_id,
                quantity=int(qty),
                unit_price=int(price)
            )
            db.add(price_row)
        db.commit()

        print("DB 저장 완료!")

        # 팝업 닫기
        close_btn = driver.find_element(By.ID, 'id_btn_cpt_close')
        close_btn.click()
        time.sleep(1)

        # 기본 청구금액 읽기
        base_price = int(driver.find_element(By.CSS_SELECTOR, '#RFtotalPrice b').text.replace(',', ''))
        print("기본 청구금액:", base_price)


        # 옵션 행들 읽기
        
        # 옵션 행들 읽기
        option_rows = driver.find_elements(By.CSS_SELECTOR, 'div.type-option')

        for row in option_rows:
            try:
               option_name = row.find_element(By.CSS_SELECTOR, 'legend.title').text
               buttons = row.find_elements(By.CSS_SELECTOR, 'button')
        
               for btn in buttons:
                    btn_text = btn.text.strip()
                    if not btn_text:
                        continue
                    btn.click()
                    time.sleep(0.5)
                    new_price = int(driver.find_element(By.CSS_SELECTOR, '#RFtotalPrice b').text.replace(',', ''))
                    extra = new_price - base_price
                    print(f"옵션: {option_name} / {btn_text} / 추가금: {extra}")
            except:
                continue

    finally:
        input("확인 후 엔터...")
        db.close()
        driver.quit()

if __name__ == "__main__":
    crawl_redprinting_acrylic_keyring()