. 🔥 Vấn đề hiệu năng (Performance & Scalability)

Thực tế:

Khi có hàng nghìn user cùng truy cập → query chậm, server quá tải
Trang product list / search rất dễ bị bottleneck

Bạn có thể nói trong CV:

Tối ưu query (JOIN, INDEX cho products, orders, reviews)
Phân trang (pagination) cho danh sách sản phẩm, đơn hàng
Caching (Redis nếu có, hoặc in-memory cache)

👉 Bonus xịn:

“Optimized product listing query reducing response time by X%”
2. 🛒 Giỏ hàng & đồng bộ dữ liệu (Cart consistency)

Thực tế:

User thêm sản phẩm → nhưng sản phẩm hết hàng
User mở nhiều tab → cart bị lệch

Bạn có thể xử lý:

Validate stock khi checkout (không chỉ khi add cart)
Transaction khi tạo order

👉 CV:

“Handled concurrent cart updates and ensured stock consistency using transactional processing”
3. 💸 Quản lý tồn kho (Inventory race condition)

Thực tế:

2 người mua cùng lúc → oversell (bán quá số lượng)

Cách giải:

Lock row / optimistic locking
Check stock trong transaction

👉 Đây là điểm rất đáng ghi vào CV:

“Prevented overselling with concurrency-safe inventory updates”
4. ⭐ Fake review & spam

Thực tế:

User spam review
Seller tự review sản phẩm mình

Bạn có thể làm:

Chỉ cho phép review nếu đã mua (order_items)
Limit số lần review

👉 CV:

“Implemented verified purchase review system to prevent spam and fake reviews”
5. 🚨 Report hệ thống (product_reports)

Thực tế:

User report sản phẩm vi phạm
Admin cần xử lý

Bạn có thể nâng cấp:

Status report (pending / resolved)
Admin dashboard xử lý report

👉 CV:

“Built moderation workflow for product reports with admin resolution tracking”
6. 🧾 Trạng thái đơn hàng (Order lifecycle)

Bạn có order_status_history → rất tốt 👍

Thực tế:

Đơn hàng có nhiều trạng thái (pending, shipped, delivered, cancelled)
Cần trace lịch sử

👉 CV:

“Designed order lifecycle tracking system with historical status logs”
7. 🎯 Tìm kiếm & lọc sản phẩm (Search & Filter)

Thực tế:

User tìm theo:
category
price range
attributes (size, color)

Bạn đã có:

product_attributes
variant_attribute_values

👉 rất giống Shopee/Lazada

👉 CV:

“Implemented dynamic product filtering based on attributes (size, color, etc.)”
8. 📦 Quản lý biến thể sản phẩm (Product Variants)

Bạn có:

product_variants
variant_attribute_values

Thực tế:

Áo có size + màu → mỗi biến thể có stock riêng

👉 CV:

“Built product variant system supporting multi-attribute combinations”
9. 💬 Notification system

Bạn có notifications

Thực tế:

Thông báo:
đơn hàng
khuyến mãi
report

👉 nâng cấp:

Real-time (WebSocket)

👉 CV:

“Developed notification system for real-time user updates”
10. 🏪 Multi-shop (Marketplace problem)

Bạn có shops

Thực tế:

Nhiều seller cùng bán
Admin phải quản lý tất cả

👉 CV:

“Designed multi-vendor marketplace architecture with role-based access (user, seller, admin)”
11. 🔐 Phân quyền (Authorization)

Thực tế:

User ≠ Seller ≠ Admin

👉 CV:

“Implemented role-based authorization for user, seller, and admin workflows”
12. 💰 Voucher / khuyến mãi

Bạn có voucher_usages

Thực tế:

1 user chỉ dùng 1 lần
giới hạn số lượng

👉 CV:

“Handled voucher usage constraints and abuse prevention”
13. 📊 Báo cáo & analytics

Thực tế:

Seller muốn biết:
doanh thu
sản phẩm bán chạy

👉 nếu bạn làm thêm:

query thống kê

👉 CV:

“Built sales analytics for sellers (revenue, top products)”