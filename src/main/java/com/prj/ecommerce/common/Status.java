package com.prj.ecommerce.common;

public enum Status {
    ACTIVE("Hoạt Động"),
    INACTIVE("Không Hoạt Động"),
    BLOCKED("Bị Chặn");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

