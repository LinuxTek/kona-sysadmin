/*
 * Copyright (C) 2011 LINUXTEK, Inc.  All Rights Reserved.
 */
package com.linuxtek.kona.sysadmin;


/**
 * KSysAdminException.
 */

@SuppressWarnings("serial")
public class KSysAdminException extends Exception {
	public KSysAdminException(String message) {
        super(message);
    }

    public KSysAdminException(String message, Throwable cause) {
        super(message, cause);
    }

    public KSysAdminException(Throwable cause) {
        super(cause);
    }
}
