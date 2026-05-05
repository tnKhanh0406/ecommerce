package com.prj.ecommerce.common;

public enum ReportReason {
    FAKE("Sản phẩm giả mạo"),
    LOW_QUALITY("Chất lượng thấp"),
    WRONG_DESCRIPTION("Mô tả không đúng"),
    COUNTERFEIT("Hàng nhái"),
    FRAUDULENT("Hàng lừa đảo"),
    PROHIBITED_ITEM("Sản phẩm bị cấm"),
    DUPLICATE_LISTING("Listing trùng lặp"),
    OTHER("Khác");

    private final String label;

    ReportReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
