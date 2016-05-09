package com.oscgc.security.saml.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class IdPConfiguration {
	private String entityId;
	private String entityAlias;
	private String signingKey = null;
	private String encryptionKey = null;
	private Collection<String> includedNameIDs = Collections.EMPTY_SET;
	private List<SingleSignOnService> ssoServices = new ArrayList<SingleSignOnService>();

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityAlias() {
		return entityAlias;
	}

	public void setEntityAlias(String entityAlias) {
		this.entityAlias = entityAlias;
	}

	public String getSigningKey() {
		return signingKey;
	}

	public void setSigningKey(String signingKey) {
		this.signingKey = signingKey;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public Collection<String> getIncludedNameIDs() {
		return includedNameIDs;
	}

	public void setIncludedNameIDs(Collection<String> includedNameIDs) {
		this.includedNameIDs = includedNameIDs;
	}

	public List<SingleSignOnService> getSsoServices() {
		return ssoServices;
	}

	public void setSsoServices(List<SingleSignOnService> ssoServices) {
		this.ssoServices = ssoServices;
	}

	static class SingleSignOnService {
		private String bindingAlias;
		private String location;

		public String getBindingAlias() {
			return bindingAlias;
		}

		public void setBindingAlias(String bindingAlias) {
			this.bindingAlias = bindingAlias;
		}

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}
	}
}
