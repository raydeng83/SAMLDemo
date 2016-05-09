package com.oscgc.security.saml.metadata;

import com.oscgc.security.saml.config.IdPConfiguration;
import org.opensaml.Configuration;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.springframework.security.saml.key.KeyManager;

/**
 * The class is responsible for generation of identity provider metadata
 * configuring in the IdP configuration page
 * 
 */
public class IdPMetadataGenerator {

	protected XMLObjectBuilderFactory builderFactory;
	protected KeyManager keyManager;
	private IdPConfiguration configuration;

	/**
	 * Default constructor.
	 */
	public IdPMetadataGenerator() {
		this.builderFactory = Configuration.getBuilderFactory();
	}

	public EntityDescriptor generateMetadata() {
		return null;
	}

	public IdPConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IdPConfiguration configuration) {
		this.configuration = configuration;
	}

}
