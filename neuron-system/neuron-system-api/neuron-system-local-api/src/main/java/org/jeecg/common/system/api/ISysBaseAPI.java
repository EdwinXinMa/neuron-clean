package org.jeecg.common.system.api;

/**
 * Compatibility shim for hibernate-re jar.
 * Extends the real interface so beans implementing this also satisfy
 * the com.echarge version.
 */
public interface ISysBaseAPI extends com.echarge.common.system.api.ISysBaseAPI {
}
