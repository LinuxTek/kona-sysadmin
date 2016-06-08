/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.sysadmin;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.linuxtek.kona.http.KHttpClientException;
import com.linuxtek.kona.http.KHttpClientRequest;

/**
 * WebminServiceImpl.
 */

public class WebminServiceImpl extends KHttpClientRequest
        implements KSysAdminService {

    private static Logger logger = Logger.getLogger(WebminServiceImpl.class);

    private String baseUrl = null;

    public WebminServiceImpl(String baseUrl, String username, 
            String password) throws KHttpClientException {
        this(baseUrl, username, password, false);
    }

    public WebminServiceImpl(String baseUrl, String username, 
            String password, boolean ignoreSSLCertWarning) 
            throws KHttpClientException {
        super(baseUrl, username, password, ignoreSSLCertWarning);
        this.baseUrl = baseUrl;
        logger.debug("WebminServiceImpl initialized");
    }

    private boolean isDemo() {
    	if (baseUrl == null || baseUrl.equalsIgnoreCase("demo")) {
    		return true;
    	}
        return false;
    }
    
    /**
     * Create a new top-level domain.
     */
    public void createDomain(String domain, String password, 
            boolean createWeb, boolean createMail) throws KSysAdminException {
        if (isDemo()) return;
        
        if (!isDomainAvailable(domain)) {
            throw new KSysAdminException(
                "Cannot create domain. Domain already exists: " + domain);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("program=create-domain");
        buffer.append("&domain=" + domain);
        buffer.append("&pass=" + password);
        buffer.append("&unix=");
        buffer.append("&dir=");
        buffer.append("&dns=");
        buffer.append("&limits-from-plan=");

        if (createWeb) {
            buffer.append("&web=");
        }

        if (createMail) {
            buffer.append("&mail=");
        }

        doRequest(buffer.toString());
    }

    public Boolean isDomainAvailable(String domain) throws KSysAdminException {
        if (isDemo()) return true;
        
        String[] domains = listDomains();
        for (String d : domains) {
            if (domain.trim().equalsIgnoreCase(d.trim())) return false;
        }
        return true;
    }

    public void addServer(String hostname, String domain) 
            throws KSysAdminException {
        if (isDemo()) return;
        
        String server = hostname + "." + domain;

        StringBuffer buffer = new StringBuffer();
        buffer.append("program=create-domain");
        buffer.append("&domain=" + server);
        buffer.append("&parent=" + domain);
        buffer.append("&dir=");
        buffer.append("&dns=");
        buffer.append("&web=");
        buffer.append("&limits-from-plan=");

        doRequest(buffer.toString());
    }

    public void deleteDomain(String domain)
            throws KSysAdminException {
        if (isDemo()) return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("program=delete-domain");
        buffer.append("&domain=" + domain);

        doRequest(buffer.toString());
    }

    public void deleteServer(String hostname, String domain)
            throws KSysAdminException {
        if (isDemo()) return;
        String server = hostname + "." + domain;
        deleteDomain(server);
    }

    public void setWebProxy(String domain, String proxy)
            throws KSysAdminException {
        if (isDemo()) return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("program=modify-web");
        buffer.append("&domain=" + domain);
        buffer.append("&proxy=" + proxy);

        doRequest(buffer.toString());
    }


    /*
     * NOTE: To add a webserver alias to an existing domain, the 
     * command line uses the create-domain sub-command.  create-domain
     * takes the webserver alias as the --domain argument and the 
     * server to which it is aliased as the --alias argument.  Therefore,
     * if site1.jinnsite.net is the original domain and you would like to
     * alias site1.linuxtek.com to it, the following command would be issued:
     * 
     * virtualmin create-domain \
     *      --domain site1.linuxtek.com --alias site1.jinnsite.net --web
     *
     * The addWebServerAlias() method below takes the original domain
     * as the first argument and the alias of that domain as the second.
     */
    public void addWebServerAlias(String domain, String alias)
            throws KSysAdminException {
        if (isDemo()) return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("program=create-domain");
        buffer.append("&domain=" + alias);
        buffer.append("&alias=" + domain);
        buffer.append("&web=");

        doRequest(buffer.toString());
    }


    /*
     * virtualmin list-domains: Expected output:
     *
     *    Domain                         Username        Description
     *    ------------------------------ --------------- -----------
     *    linuxtek.net                   linuxtek                   
     *    
     *    Exit status: 0
     */
    public String[] listDomains() throws KSysAdminException {
        if (isDemo()) return null;
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("program=list-domains");

        String[] result = formatResponse(doRequest(buffer.toString()));

        String[] domains = new String[result.length];
        for (int i=0; i<result.length; i++) {
            String[] tokens = result[i].split("\\s+");
            domains[i] = tokens[0].trim();
        }

        return domains;
    }

    private String doRequest(String params) throws KSysAdminException {
        String result = null;
        try {
            result = super.doRequest("", params);
            checkResult(params, result);
        } catch (Exception e) {
            throw new KSysAdminException(e);
        }
        return result;
    }

    /*
     * NOTE: Last line of result is the Exit status
     *      Exit status: 0
     */
    protected void checkResult(String params, String response)
            throws KSysAdminException {
        String request = getBaseUrl() + "?" + params;

        if (response == null) {
            throw new KSysAdminException(
                "Null server response. Check server request: " + request);
        }

        // split the response by new lines
        String[] lines = response.split("\n");
        if (lines == null || lines.length == 0) {
            throw new KSysAdminException("Invalid response: " + lines);
        }

        // get the final line
        String status = lines[lines.length - 1];
        String[] tokens = status.split(":");
        Integer statusCode = null;

        try {
            statusCode = Integer.parseInt(tokens[1].trim());
        } catch (Exception e) {
            throw new KSysAdminException("Invalid valid status: " + status, e);
        }

        if (statusCode != 0) {
            throw new KSysAdminException("Error executing request:"
                + "\nrequest: " + request
                + "\nresponse: " + response);
        }
    }

    protected String[] formatResponse(String response)
            throws KSysAdminException {

        // split the response by new lines
        String[] lines = response.split("\n");

        /*
         * if request executed successfully, then we should have a response
         * in the form of:
         *    Domain                         Username        Description
         *    ------------------------------ --------------- -----------
         *    linuxtek.net                   linuxtek                   
         *    
         *    Exit status: 0
         *   
         * therefore, we need to strip off the first 2 lines and the final
         * line of the output.
         */

        ArrayList<String> result = new ArrayList<String>();

        try {
            for (int i=2; i<(lines.length)-1; i++) {
                if (lines[i] != null && lines[i].length()>0) {
                    result.add(lines[i]);
                }
            }
        } catch (Exception e) {
            throw new KSysAdminException("Error processing server response:"
                + "\nresponse: " + response);
        }

        // if we get here, then we have a valid response to return
        return result.toArray(new String[0]);
    }
}
