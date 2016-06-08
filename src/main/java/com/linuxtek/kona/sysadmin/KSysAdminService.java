/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.sysadmin;


/**
 * KSysAdminService.
 */

public interface KSysAdminService {

    public Boolean isDomainAvailable(String domain) throws KSysAdminException;

    public String[] listDomains() throws KSysAdminException;

    public void createDomain(String domain, String password, 
            boolean createWeb, boolean createMail) 
            throws KSysAdminException;

    /**
     * Delete domain and all its subdomains.
     */
    public void deleteDomain(String domain)
            throws KSysAdminException;

    public void addServer(String hostname, String domain)
            throws KSysAdminException;

    public void deleteServer(String hostname, String domain)
            throws KSysAdminException;

    public void addWebServerAlias(String domain, String alias)
            throws KSysAdminException;

    public void setWebProxy(String domain, String proxy)
            throws KSysAdminException;
}
