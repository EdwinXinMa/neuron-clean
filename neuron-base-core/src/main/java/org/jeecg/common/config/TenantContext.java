package org.jeecg.common.config;

/**
 * Compatibility shim for hibernate-re jar.
 * Delegates to com.echarge.config.mybatis.TenantContext.
 */
public class TenantContext {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static String getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}
