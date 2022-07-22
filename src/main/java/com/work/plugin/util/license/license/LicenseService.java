package com.work.plugin.util.license.license;

import java.util.List;

/**
 * License Service Component
 *
 * @author Masato Morita
 * @since 1.0.0
 */
public interface LicenseService
{
    /**
     * Checks whether this application has the valid license in order to use this plugin.
     *
     * @return true if this application has the valid license, false otherwise
     */

    boolean hasValidLicense();
    List<PRLInfo> getPRLInfofromByte(String keyBytes);
    List<PRLInfo> getLicenseInfos();
    String getReleaseData(String namekey);
    boolean getPLS_A(String PP);
    String getPLS_A_U_C();
    boolean getPLS_S(String PP);
    boolean getPLS_U(String PP);
    boolean getPLS_C(String PP);
}
