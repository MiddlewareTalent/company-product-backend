package com.accesshr.emsbackend.EmployeeController.Config;

public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_COUNTRY = new ThreadLocal<>();


    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void setCountry(String country) {
        CURRENT_COUNTRY.set(country);
    }

    public static String getCountry() {
        return CURRENT_COUNTRY.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_COUNTRY.remove();
    }
}
