package com.oscgc.security.saml.websso;

import org.springframework.security.saml.websso.WebSSOProfileOptions;

public class WebSSOProfileOptionsExt extends WebSSOProfileOptions {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the IDP's entityId
	 */
	private String entityId;

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

}
