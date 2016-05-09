package com.oscgc.security.saml.idp.web.filter;

import com.oscgc.security.saml.websso.IDPWebSSOProfile;
import com.oscgc.security.saml.websso.IDPWebSSOProfileOptions;
import org.opensaml.common.SAMLException;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.processor.SAMLProcessor;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class SAMLAuthnRequestProcessingFilter extends GenericFilterBean {
	protected final static Logger logger = LoggerFactory
			.getLogger(SAMLAuthnRequestProcessingFilter.class);

	@Autowired
	protected SAMLContextProvider contextProvider;

	@Autowired
	protected IDPWebSSOProfile idpProfile;

	@Autowired
	protected SAMLProcessor processor;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		FilterInvocation fi = new FilterInvocation(request, response, chain);
		try {

			SAMLMessageContext context = contextProvider.getLocalEntity(
					fi.getHttpRequest(), fi.getResponse());

			processor.retrieveMessage(context, getBinding(fi.getHttpRequest()
					.getRequestURI()));

			// override properties
			context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);

			IDPWebSSOProfileOptions options = new IDPWebSSOProfileOptions();
			options.setAssertionConsumerIndex(1);
			options.setAudienceURLs(new String[] { "http://localhost:8080/saml-demo-sp" });
			idpProfile.sendAuthenticationResponse(context, options);
			chain.doFilter(request, response);
		} catch (MetadataProviderException e) {
			throw new ServletException(e);
		} catch (SAMLException e) {
			throw new ServletException(e);
		} catch (SecurityException e) {
			throw new ServletException(e);
		} catch (ValidationException e) {
			throw new ServletException(e);
		} catch (DecryptionException e) {
			throw new ServletException(e);
		} catch (MessageEncodingException e) {
			throw new ServletException(e);
		} catch (MessageDecodingException e) {
			throw new ServletException(e);
		}
	}

	protected String getBinding(String requestUrl) throws ServletException {
		if (requestUrl.indexOf("/SSORedirect/") > -1) {
			return SAMLConstants.SAML2_REDIRECT_BINDING_URI;
		} else if (requestUrl.indexOf("SSOPOST") > -1) {
			return SAMLConstants.SAML2_POST_BINDING_URI;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("{} is invaild single sign on service", requestUrl);
			}
			throw new ServletException(requestUrl
					+ " is invaild single sign on service");
		}
	}

}
