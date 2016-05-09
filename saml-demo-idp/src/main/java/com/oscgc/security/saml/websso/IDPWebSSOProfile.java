package com.oscgc.security.saml.websso;

import org.opensaml.common.SAMLException;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.validation.ValidationException;
import org.springframework.security.saml.context.SAMLMessageContext;

public interface IDPWebSSOProfile {
	public void sendAuthenticationResponse(SAMLMessageContext context,
			IDPWebSSOProfileOptions options) throws SAMLException,
			org.opensaml.xml.security.SecurityException, ValidationException,
			DecryptionException, MetadataProviderException,
			MessageEncodingException;
}
