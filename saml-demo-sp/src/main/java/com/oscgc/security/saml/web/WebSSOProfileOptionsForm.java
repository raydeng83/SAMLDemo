package com.oscgc.security.saml.web;

import java.util.Collection;
import java.util.Set;

public class WebSSOProfileOptionsForm {
	private String entityId;
	private String binding;
	private Set<String> allowedIDPs;
	private String providerName;
	private Integer assertionConsumerIndex;

	// Name ID policy
	private String nameID;
	private boolean allowCreate;
	private boolean passive = false;
	private boolean forceAuthn = false;
	private boolean includeScoping = true;
	private Integer proxyCount = 2;

	private String relayState;
	private Collection<String> authnContexts;

	public String getBinding() {
		return binding;
	}

	public void setBinding(String binding) {
		this.binding = binding;
	}

	public Set<String> getAllowedIDPs() {
		return allowedIDPs;
	}

	public void setAllowedIDPs(Set<String> allowedIDPs) {
		this.allowedIDPs = allowedIDPs;
	}

	public String getProviderName() {
		return providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public Integer getAssertionConsumerIndex() {
		return assertionConsumerIndex;
	}

	public void setAssertionConsumerIndex(Integer assertionConsumerIndex) {
		this.assertionConsumerIndex = assertionConsumerIndex;
	}

	public String getNameID() {
		return nameID;
	}

	public void setNameID(String nameID) {
		this.nameID = nameID;
	}

	public boolean isAllowCreate() {
		return allowCreate;
	}

	public void setAllowCreate(boolean allowCreate) {
		this.allowCreate = allowCreate;
	}

	public boolean isPassive() {
		return passive;
	}

	public void setPassive(boolean passive) {
		this.passive = passive;
	}

	public boolean isForceAuthn() {
		return forceAuthn;
	}

	public void setForceAuthn(boolean forceAuthn) {
		this.forceAuthn = forceAuthn;
	}

	public boolean isIncludeScoping() {
		return includeScoping;
	}

	public void setIncludeScoping(boolean includeScoping) {
		this.includeScoping = includeScoping;
	}

	public Integer getProxyCount() {
		return proxyCount;
	}

	public void setProxyCount(Integer proxyCount) {
		this.proxyCount = proxyCount;
	}

	public String getRelayState() {
		return relayState;
	}

	public void setRelayState(String relayState) {
		this.relayState = relayState;
	}

	public Collection<String> getAuthnContexts() {
		return authnContexts;
	}

	public void setAuthnContexts(Collection<String> authnContexts) {
		this.authnContexts = authnContexts;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

}
