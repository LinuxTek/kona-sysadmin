/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.sysadmin;

import com.linuxtek.kona.http.KHttpClientException;
import org.apache.log4j.Logger;

/**
 * KSysAdminService.
 */

public class KSysAdminServiceFactory {
    private static Logger logger = 
            Logger.getLogger(KSysAdminServiceFactory.class);

    public static KSysAdminService getWebminService(String baseUrl, 
            String username, String password) throws KSysAdminException {
        return getWebminService(baseUrl, username, password, false);
    }

    public static KSysAdminService getWebminService(String baseUrl, 
            String username, String password, boolean ignoreSSLCertWarning) 
            throws KSysAdminException {

        try {
            logger.debug("Webmin service requested.");
            KSysAdminService webmin = new WebminServiceImpl(baseUrl, 
                    username, password, ignoreSSLCertWarning);
            return webmin;
        } catch (KHttpClientException e) {
            throw new KSysAdminException(e);
        }
    }
}
