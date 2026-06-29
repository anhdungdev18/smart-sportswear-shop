# THIET KE SAN PHAM

## 1. Thong tin chung

- Ten san pham: Website kinh doanh trang phuc the thao thong minh tich hop AI
- Muc tieu: xay dung mot san pham thuong mai dien tu hoan chinh cho khach hang va bo phan van hanh
- Muc tieu tai lieu: chot pham vi, kien truc, du lieu, API, UI va lo trinh truoc khi bat dau code
- Trang thai tai lieu: ban thiet ke co so de review va chot

## 2. Dinh nghia san pham

### 2.1 Bai toan can giai quyet

San pham giai quyet bai toan ban hang truc tuyen cho linh vuc trang phuc the thao, bao gom:

- Hien thi va ban san pham online
- Quan ly san pham, bien the, ton kho, don hang
- Ho tro thanh toan online muc demo
- Ho tro tim kiem va tu van mua hang bang AI
- Cung cap dashboard bao cao cho admin

### 2.2 Nguoi dung muc tieu

- Khach hang: tim san pham, xem chi tiet, them gio, dat hang, thanh toan, theo doi don
- Admin: quan ly san pham, danh muc, ton kho, don hang, nguoi dung, bao cao
- Nhan vien ban hang: xu ly don, cap nhat trang thai
- Nhan vien kho: nhap kho, xuat kho, dieu chinh ton

### 2.3 Tieu chi de duoc coi la "hoan chinh"

San pham chi duoc xem la hoan chinh khi dap ung dong thoi:

- Co frontend va backend chay duoc end-to-end
- Co database that, khong dung du lieu mock cho luong chinh
- Co luong mua hang hoan chinh tu xem san pham den dat hang
- Co trang admin van hanh duoc
- Co tai lieu cai dat va tai lieu API
- Co xu ly loi co ban, phan quyen, logging, validation
- Co kha nang deploy local bang Docker hoac script ro rang

## 3. Pham vi san pham

### 3.1 Pham vi version 1

Version 1 tap trung vao luong kinh doanh cot loi:

- Dang ky, dang nhap, phan quyen
- Quan ly nguoi dung va dia chi giao hang
- Quan ly danh muc, thuong hieu, san pham, bien the, hinh anh
- Tim kiem va loc san pham
- Gio hang
- Dat hang
- Thanh toan online muc demo
- Quan ly ton kho co ban
- Quan ly don hang
- Dashboard doanh thu va ton kho co ban
- AI Chatbot tu van san pham
- AI Size Advisor

### 3.2 Ngoai pham vi version 1

Nhung hang muc duoi day nen de sang phase 2 neu khong co doi ngu lon:

- Image search bang AI
- Recommendation engine day du
- Demand forecasting nang cao
- Phan tich cam xuc review
- Notification event-driven quy mo lon
- Microservices day du cho tat ca module

### 3.3 Nguyen tac chot pham vi

- Moi tinh nang phai co user flow ro rang
- Moi tinh nang phai co API va du lieu tuong ung
- Moi tinh nang AI phai co nguon du lieu va cach danh gia ket qua
- Khong liet ke tinh nang chi de "cho dep tai lieu"

## 4. Chot kien truc ky thuat

### 4.1 Van de hien tai

Tai lieu cu dang mo ta hai huong kien truc khac nhau:

- Huong 1: Java Spring Boot + Microservices
- Huong 2: Node.js/Express

Can chot mot huong duy nhat truoc khi thiet ke chi tiet.

### 4.2 De xuat cho version 1

De xuat dung `modular monolith` thay vi microservices ngay tu dau.

Ly do:

- Giam do phuc tap van hanh
- De code nhanh hon
- De debug, test, deploy
- Phu hop hon voi doi ngu nho va san pham giai doan dau
- Sau nay van co the tach service theo module

### 4.3 Kien truc de xuat

- Frontend: Next.js
- Backend: Node.js + NestJS hoac Express
- Database: PostgreSQL
- ORM: Prisma
- Cache: Redis
- Object storage anh: Cloudinary
- Auth: JWT + refresh token
- AI: OpenAI API hoac Gemini API
- Search nang cao: PostgreSQL + pgvector cho phase sau, version 1 co the dung full-text + filter co cau truc
- Deploy local: Docker Compose

Neu doi muon theo Java, van duoc, nhung khong nen vua viet tai lieu theo Java vua code theo Node.js.

## 5. Cau truc module

### 5.1 Module khach hang

- Trang chu
- Danh sach san pham
- Chi tiet san pham
- Tim kiem va loc
- Gio hang
- Checkout
- Thanh toan
- Tai khoan ca nhan
- Lich su don hang
- Theo doi don hang
- AI chatbot
- AI size advisor

### 5.2 Module admin

- Dashboard
- Quan ly san pham
- Quan ly danh muc, thuong hieu
- Quan ly ton kho
- Quan ly don hang
- Quan ly nguoi dung
- Quan ly noi dung co ban
- Cau hinh chatbot
- Bao cao

### 5.3 Module he thong

- Xac thuc va phan quyen
- Upload anh
- Audit log
- Thong bao email co ban
- Logging va monitoring co ban

## 6. User flow cot loi

### 6.1 Luong mua hang

1. Khach truy cap trang chu
2. Tim kiem hoac loc san pham
3. Xem chi tiet san pham
4. Chon size, mau, so luong
5. Them vao gio hang
6. Dang nhap hoac dang ky
7. Nhap dia chi giao hang
8. Chon phuong thuc thanh toan
9. Tao don hang
10. Thanh toan demo
11. Nhan trang thai dat hang thanh cong
12. Theo doi lich su don hang

### 6.2 Luong admin xu ly don

1. Admin dang nhap
2. Xem danh sach don
3. Mo chi tiet don
4. Xac nhan don
5. Dong goi
6. Cap nhat giao hang
7. Hoan tat hoac huy don
8. He thong cap nhat ton kho va bao cao

### 6.3 Luong AI size advisor

1. Khach nhap chieu cao, can nang, gioi tinh
2. Chon loai san pham va form mac
3. He thong tra goi y size
4. He thong hien thi ly do de xuat
5. Khach ap dung size vao san pham dang xem

## 7. Yeu cau chuc nang

### 7.1 Auth

- Dang ky
- Dang nhap
- Dang xuat
- Refresh token
- Quen mat khau
- Doi mat khau
- Phan quyen theo role

### 7.2 Product

- CRUD san pham
- CRUD bien the
- CRUD danh muc
- CRUD thuong hieu
- Quan ly hinh anh
- Trang thai ban/hide/out of stock

### 7.3 Search

- Tim theo tu khoa
- Loc theo gia, size, mau, danh muc, thuong hieu
- Sap xep theo moi nhat, gia, ban chay

### 7.4 Cart

- Them vao gio
- Cap nhat so luong
- Xoa khoi gio
- Tinh tam tinh

### 7.5 Order

- Tao don hang
- Xem lich su don
- Xem chi tiet don
- Huy don trong dieu kien hop le
- Admin cap nhat trang thai

### 7.6 Payment

- Tao payment session
- Callback thanh toan
- Cap nhat trang thai thanh toan

### 7.7 Inventory

- Theo doi ton theo bien the
- Nhap kho
- Xuat kho
- Dieu chinh ton
- Canh bao sap het hang

### 7.8 Report

- Doanh thu theo ngay/thang
- Don hang theo trang thai
- San pham ban chay
- Ton kho hien tai

### 7.9 AI Chatbot

- Tra loi cau hoi ve san pham
- Goi y san pham theo nhu cau va ngan sach
- Tra loi dua tren du lieu that cua he thong
- Khong duoc tu y "boc phep" san pham khong ton tai

### 7.10 AI Size Advisor

- Nhan input co cau truc
- Tra size de xuat
- Giai thich ly do
- Canh bao khi nam giua 2 size

## 8. Yeu cau phi chuc nang

- Bao mat: JWT, hash password, phan quyen route
- Hieu nang: trang danh sach san pham phai phan trang
- Kha dung: giao dien mobile va desktop
- Do tin cay: validate input, transaction cho tao don
- Kha nang bao tri: code theo module, naming nhat quan
- Logging: log auth, log dat hang, log loi he thong
- Backup: backup database dinh ky khi dua vao van hanh that

## 9. Mo hinh du lieu

### 9.1 Thuc the chinh

- users
- roles
- addresses
- categories
- brands
- products
- product_variants
- product_images
- carts
- cart_items
- orders
- order_items
- payments
- warehouses
- inventory_transactions
- reviews
- wishlists
- chat_logs

### 9.2 Quan he chinh

- user co nhieu addresses
- product thuoc mot category va mot brand
- product co nhieu product_variants
- product co nhieu product_images
- cart thuoc mot user
- cart co nhieu cart_items
- order thuoc mot user
- order co nhieu order_items
- order co the co nhieu ban ghi payment
- variant co nhieu bien dong inventory_transactions

### 9.3 Nguyen tac du lieu

- Ton kho nen quan ly o cap `product_variant`
- Gia ban va ton kho khong nen dat o bang `products`
- Trang thai don hang va trang thai thanh toan tach rieng
- Moi thay doi ton kho can co lich su

## 10. Thiet ke API

### 10.1 Nguyen tac API

- Prefix: `/api/v1`
- JSON response thong nhat
- Phan trang cho danh sach
- Ma loi va thong diep ro rang

### 10.2 Nhom API chinh

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `GET /api/v1/products`
- `GET /api/v1/products/:slugOrId`
- `POST /api/v1/admin/products`
- `PUT /api/v1/admin/products/:id`
- `GET /api/v1/cart`
- `POST /api/v1/cart/items`
- `PATCH /api/v1/cart/items/:id`
- `DELETE /api/v1/cart/items/:id`
- `POST /api/v1/orders`
- `GET /api/v1/orders/me`
- `GET /api/v1/orders/:id`
- `PATCH /api/v1/admin/orders/:id/status`
- `POST /api/v1/payments/create`
- `POST /api/v1/payments/callback`
- `GET /api/v1/admin/reports/overview`
- `POST /api/v1/ai/chat`
- `POST /api/v1/ai/size-advisor`

### 10.3 Mau response

```json
{
  "success": true,
  "message": "OK",
  "data": {},
  "meta": {}
}
```

### 10.4 Mau loi

```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    {
      "field": "email",
      "message": "Email is invalid"
    }
  ]
}
```

## 11. Quy tac nghiep vu quan trong

### 11.1 San pham va ton kho

- Khong cho them vao gio neu variant khong ton tai
- Khong cho dat hang vuot ton kha dung
- San pham het hang van co the hien thi, nhung khong mua duoc

### 11.2 Don hang

- Mỗi don hang co ma duy nhat
- Tao don hang phai chot snapshot gia tai thoi diem mua
- Huy don chi hop le truoc mot moc trang thai xac dinh

### 11.3 Thanh toan

- Callback phai kiem tra checksum
- Khong duoc cap nhat thanh cong neu callback trung lap
- Payment va order phai co doi soat trang thai ro rang

## 12. Thiet ke UI/UX can co

### 12.1 Trang khach hang

- Home
- Product listing
- Product detail
- Cart
- Checkout
- Order success
- Profile
- Order history

### 12.2 Trang admin

- Dashboard
- Product management
- Inventory management
- Order management
- User management
- Reports

### 12.3 Nguyen tac UI

- Mobile first
- Luong mua hang it buoc
- Trang thai, loi, empty state ro rang
- CTA mua hang noi bat

## 13. AI design

### 13.1 Chatbot

Input:

- Cau hoi nguoi dung
- Context san pham va ton kho

Output:

- Cau tra loi ngan gon
- Goi y san pham co that
- Link san pham lien quan

Rang buoc:

- Neu khong du thong tin thi hoi lai
- Neu he thong khong co san pham phu hop thi phai noi ro

### 13.2 Size advisor

Input:

- Chieu cao
- Can nang
- Gioi tinh
- Loai san pham
- Form mac

Output:

- Size de xuat
- Ly do de xuat
- Canh bao giua 2 size neu co

### 13.3 Nguyen tac AI version 1

- AI phai dua tren du lieu san pham that
- Khong lam qua nhieu bai toan ML nang
- Uu tien `rule-based + LLM support` truoc

## 14. Bao mat va phan quyen

### 14.1 Roles

- CUSTOMER
- SALES_STAFF
- WAREHOUSE_STAFF
- ADMIN

### 14.2 Nhom quyen

- Customer: thao tac tai khoan, gio hang, don cua minh
- Sales staff: xem va cap nhat don hang
- Warehouse staff: thao tac ton kho
- Admin: toan quyen he thong

## 15. Logging, monitoring, audit

- Log dang nhap that bai
- Log tao don
- Log callback payment
- Log thay doi ton kho
- Log hanh dong admin quan trong

## 16. Moi truong va trien khai

### 16.1 Moi truong

- local
- staging
- production

### 16.2 Bien moi truong du kien

- DATABASE_URL
- REDIS_URL
- JWT_ACCESS_SECRET
- JWT_REFRESH_SECRET
- CLOUDINARY_URL
- OPENAI_API_KEY hoac GEMINI_API_KEY
- PAYMENT_RETURN_URL
- PAYMENT_CALLBACK_URL

### 16.3 Docker Compose version 1

- frontend
- backend
- postgres
- redis

## 17. Ke hoach implementation

### Phase 1

- Chot stack
- Chot schema du lieu
- Chot API contract
- Chot wireframe

### Phase 2

- Auth
- Product
- Cart
- Order
- Inventory co ban

### Phase 3

- Payment demo
- Admin dashboard
- Reports co ban

### Phase 4

- AI chatbot
- AI size advisor
- Hardening va test

## 18. Tieu chi nghiem thu

San pham duoc xem la san sang code khi da co:

- Tai lieu scope da chot
- Stack da chot
- ERD da chot
- Danh sach API da chot
- User flow da chot
- Wireframe da chot
- Quy tac nghiep vu da chot
- Danh sach ngoai pham vi da chot

San pham duoc xem la hoan thanh version 1 khi:

- Luong mua hang chay thong
- Admin thao tac duoc san pham, don hang, ton kho
- Payment demo hoat dong
- AI chatbot va size advisor co ban hoat dong
- Co tai lieu cai dat
- Co du lieu demo

## 19. Nhung diem can chot ngay

Truoc khi code, doi can chot 7 diem nay:

1. Dung Java hay Node.js
2. Dung microservices hay modular monolith
3. AI nao lam that trong version 1
4. Thanh toan dung VNPay demo hay cach khac
5. Muc ton kho quan ly theo san pham hay theo bien the
6. Frontend dung Next.js hay React thuong
7. Muc tieu version 1 va phase 2 phan tach the nao

## 20. Danh gia tai lieu hien tai

Tai lieu ban dau co diem manh:

- Liet ke duoc nhieu module va huong phat trien
- Biet ro bai toan kinh doanh
- Co dinh huong AI ro

Nhung chua du de bat dau code vi:

- Chua chot kien truc duy nhat
- Chua cat pham vi version 1
- Chua chot ERD chi tiet
- Chua chot API contract day du
- Chua co quy tac nghiep vu chi tiet
- Chua co wireframe/man hinh

## 21. De xuat cach lam tiep

Thu tu lam dung:

1. Chot stack va kien truc
2. Cat pham vi version 1
3. Ve ERD
4. Chot API
5. Ve wireframe
6. Viet task breakdown
7. Moi bat dau code

Neu muon, tai lieu nay co the tach tiep thanh 4 file rieng:

- `01_SCOPE.md`
- `02_ERD.md`
- `03_API_SPEC.md`
- `04_IMPLEMENTATION_PLAN.md`
