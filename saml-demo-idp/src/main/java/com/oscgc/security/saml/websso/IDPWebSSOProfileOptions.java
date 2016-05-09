package com.oscgc.security.saml.websso;

import java.io.Serializable;

/**
 * JavaBean contains properties allowing customization of SAML response message
 * sent to the SP.
 * 
 */
public class IDPWebSSOProfileOptions implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String binding;
	private Integer assertionConsumerIndex;
	private String issuerFormat = null;
	private String[] audienceURLs = null;
	private String idFormat = "unspecified";
	private int assertionLifetime = 0;
	private String authenticationMethod = "password";

	// Default is bearer
	// Relate to org.opensaml.saml2.core.SubjectConfirmation
	// Relate to
	// com.osc.security.saml.websso.IDPWebSSOProfileImpl#getSubjectConfirmation
	private String scMethod = "bearer";

	public String getBinding() {
		return binding;
	}

	public void setBinding(String binding) {
		this.binding = binding;
	}

	public Integer getAssertionConsumerIndex() {
		return assertionConsumerIndex;
	}

	public void setAssertionConsumerIndex(Integer assertionConsumerIndex) {
		this.assertionConsumerIndex = assertionConsumerIndex;
	}

	public String getIssuerFormat() {
		return issuerFormat;
	}

	public void setIssuerFormat(String issuerFormat) {
		this.issuerFormat = issuerFormat;
	}

	public String[] getAudienceURLs() {
		return audienceURLs;
	}

	public void setAudienceURLs(String[] audienceURLs) {
		this.audienceURLs = audienceURLs;
	}

	public String getIdFormat() {
		return idFormat;
	}

	public void setIdFormat(String idFormat) {
		this.idFormat = idFormat;
	}

	public String getScMethod() {
		return scMethod;
	}

	public void setScMethod(String scMethod) {
		this.scMethod = scMethod;
	}

	public int getAssertionLifetime() {
		return assertionLifetime;
	}

	public void setAssertionLifetime(int assertionLifetime) {
		this.assertionLifetime = assertionLifetime;
	}

	public String getAuthenticationMethod() {
		return authenticationMethod;
	}

	public void setAuthenticationMethod(String authenticationMethod) {
		this.authenticationMethod = authenticationMethod;
	}

}
