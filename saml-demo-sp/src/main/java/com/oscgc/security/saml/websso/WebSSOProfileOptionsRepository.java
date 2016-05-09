package com.oscgc.security.saml.websso;

public interface WebSSOProfileOptionsRepository {
	public WebSSOProfileOptionsExt findByEntityId(String entityId);

	public WebSSOProfileOptionsExt save(WebSSOProfileOptionsExt options);

	public Iterable<WebSSOProfileOptionsExt> findAll();
}
