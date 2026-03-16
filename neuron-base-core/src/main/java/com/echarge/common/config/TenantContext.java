/*
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.echarge.common.config;

import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(TenantContext.class);
    private static ThreadLocal<String> currentTenant = new ThreadLocal();

    public static void setTenant(String tenant) {
        log.debug(" setting tenant to " + tenant);
        currentTenant.set(tenant);
    }

    public static String getTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}

