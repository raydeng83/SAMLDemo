package com.oscgc.security.saml;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.WebSSOProfileOptions;

import com.oscgc.security.saml.websso.WebSSOProfileOptionsRepository;

/**
 * override the getProfileOptions with that find it by IdP's entityId
 * 
 */
public class SAMLEntryPoint extends
		org.springframework.security.saml.SAMLEntryPoint {

	@Autowired
	protected WebSSOProfileOptionsRepository optionsRepository;

	/**
	 * 
	 */
	@Override
	protected WebSSOProfileOptions getProfileOptions(
			SAMLMessageContext context, AuthenticationException exception)
			throws MetadataProviderException {
		WebSSOProfileOptions result = optionsRepository.findByEntityId(context
				.getPeerEntityId());
		if (result == null)
			return super.getProfileOptions(context, exception);
		else
			return result;
	}

}
