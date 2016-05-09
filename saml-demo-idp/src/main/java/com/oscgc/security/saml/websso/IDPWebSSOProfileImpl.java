package com.oscgc.security.saml.websso;

import static org.springframework.security.saml.util.SAMLUtil.isDateTimeSkewValid;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.joda.time.DateTime;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObject;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLRuntimeException;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.websso.AbstractProfileBase;

import javax.servlet.ServletException;

public class IDPWebSSOProfileImpl extends AbstractProfileBase implements
		IDPWebSSOProfile ,InitializingBean {
	private final static Logger log = LoggerFactory
			.getLogger(IDPWebSSOProfileImpl.class);
	/**
	 * Maximum time from authn request creation when the message is deemed
	 * valid.
	 */
	private int requestSkew = 60;

	/**
	 * Maximum time from assertion creation when the message is deemed valid.
	 */
	private int assertionLifetime = 60;

    private Resource resource = new ClassPathResource("samlAttributes.properties");

    private Properties props;

	@Override
	public void sendAuthenticationResponse(SAMLMessageContext context,
			IDPWebSSOProfileOptions options) throws SAMLException,
			SecurityException, ValidationException, DecryptionException,
			MetadataProviderException, MessageEncodingException {

		SAMLObject message = context.getInboundSAMLMessage();
		if (!(message instanceof AuthnRequest)) {
			if (log.isDebugEnabled())
				log.debug("Received request is not of a AuthnRequest object type");
			throw new SAMLException(
					"Received request is not of a AuthnRequest object type");
		}

		AuthnRequest request = (AuthnRequest) message;
		// Verify issue time
		DateTime time = request.getIssueInstant();
		if (!isDateTimeSkewValid(getRequestSkew(), time)) {
			log.debug(
					"Request issue time is either too old or with date in the future, skew {}, time {}",
					getResponseSkew(), time);
			throw new SAMLException(
					"Request issue time is either too old or with date in the future");
		}

		// Verify issuer
		verifyIssuer(request.getIssuer(), context);

		// Find PeerEntityMetadata by issuer
		String peerEntityId = request.getIssuer().getValue();
		context.setPeerEntityId(peerEntityId);
		context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
		if (!metadata.isSPValid(peerEntityId)) {
			log.debug("SP Site which entityId is {} not existed.", peerEntityId);
			throw new SAMLException("SP Site which entityId is " + peerEntityId
					+ " not existed.");
		}
		EntityDescriptor peerEntityDescriptor;
		peerEntityDescriptor = metadata.getEntityDescriptor(peerEntityId);
		context.setPeerEntityMetadata(peerEntityDescriptor);

		RoleDescriptor roleDescriptor = metadata.getRole(peerEntityId,
				context.getPeerEntityRole(),
				org.opensaml.common.xml.SAMLConstants.SAML20P_NS);
		context.setPeerEntityRoleMetadata(roleDescriptor);

		// TODO more validation to the authn request object

		// generate the response

		// Load the entities from the context
		IDPSSODescriptor idpssoDescriptor = (IDPSSODescriptor) context
				.getLocalEntityRoleMetadata();
		SPSSODescriptor spDescriptor = (SPSSODescriptor) context
				.getPeerEntityRoleMetadata();

		if (spDescriptor == null || idpssoDescriptor == null) {
			throw new SAMLRuntimeException(
					"SPSSODescriptor, IDPSSODescriptor are not present in the SAMLContext");
		}

		SingleSignOnService ssoService = getSingleSignOnService(options,
				idpssoDescriptor, spDescriptor);
		AssertionConsumerService consumerService = getAssertionConsumerService(
				options, idpssoDescriptor, spDescriptor, request);
        Response response = null;
        try {
            response = getResponse(context, options, consumerService,
                    ssoService, request.getID());
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        context.setCommunicationProfileId(getProfileIdentifier());
		context.setOutboundMessage(response);
		context.setOutboundSAMLMessage(response);
		context.setPeerEntityEndpoint(consumerService);

		boolean sign = spDescriptor.getWantAssertionsSigned();
		sendMessage(context, sign);
	}

	protected void verifyIssuer(Issuer issuer, SAMLMessageContext context)
			throws SAMLException {
		if (null == issuer) {
			log.debug("No issuer in the request object");
			throw new SAMLException("No issuer in the request object");
		}

		// Validate format of issuer
		if (issuer.getFormat() != null
				&& !issuer.getFormat().equals(NameIDType.ENTITY)) {
			log.debug("Assertion invalidated by issuer type {}",
					issuer.getFormat());
			throw new SAMLException("Assertion invalidated by issuer type");
		}
	}

	protected SingleSignOnService getSingleSignOnService(
			IDPWebSSOProfileOptions options, IDPSSODescriptor idpssoDescriptor,
			SPSSODescriptor spDescriptor) throws MetadataProviderException {

		// User specified value
		String userBinding = options.getBinding();
		// Find the endpoint
		List<SingleSignOnService> services = idpssoDescriptor
				.getSingleSignOnServices();
		for (SingleSignOnService service : services) {
			if (isEndpointSupported(service)) {
				if (userBinding != null) {
					if (isEndpointMatching(service, userBinding)) {
						log.debug("Found user specified binding");
						return service;
					}
				} else {
					// Use as a default
					return service;
				}
			}
		}

		// No value found
		if (userBinding != null) {
			log.debug("User specified binding {} not found for IDP",
					userBinding);
			throw new MetadataProviderException(
					"User specified binding is not supported by the Identity Provider using profile "
							+ getProfileIdentifier());
		} else {
			log.debug("No binding found for IDP " + userBinding);
			throw new MetadataProviderException(
					"No supported binding was found for profile "
							+ getProfileIdentifier());
		}

	}

	protected AssertionConsumerService getAssertionConsumerService(
			IDPWebSSOProfileOptions options, IDPSSODescriptor idpSSODescriptor,
			SPSSODescriptor spDescriptor, AuthnRequest request)
			throws MetadataProviderException {

		List<AssertionConsumerService> services = spDescriptor
				.getAssertionConsumerServices();
		// Use request protocol binding
		if (request.getProtocolBinding() != null) {
			for (AssertionConsumerService service : services) {
				if (service.getBinding().equals(request.getProtocolBinding())) {
					if (!isEndpointSupported(service)) {
						throw new MetadataProviderException(
								"Endpoint designated by the protocal binding in the AuthnRequest is not supported by this profile");
					} else {
						log.debug(
								"Using consumer service determined by AuthnRequest protocal binding with binding {}",
								service.getBinding());
						return service;
					}
				}
			}
		}

		// Use user preference
		if (options.getAssertionConsumerIndex() != null) {
			for (AssertionConsumerService service : services) {
				if (options.getAssertionConsumerIndex().equals(
						service.getIndex())) {
					if (!isEndpointSupported(service)) {
						throw new MetadataProviderException(
								"Endpoint designated by the value in the IDPWebSSOProfileOptions is not supported by this profile");
					} else {
						log.debug(
								"Using consumer service determined by user preference with binding {}",
								service.getBinding());
						return service;
					}
				}
			}
			throw new MetadataProviderException("AssertionConsumerIndex "
					+ options.getAssertionConsumerIndex()
					+ " not found for spDescriptor " + spDescriptor);
		}

		// Use default
		if (spDescriptor.getDefaultAssertionConsumerService() != null
				&& isEndpointSupported(spDescriptor
						.getDefaultAssertionConsumerService())) {
			AssertionConsumerService service = spDescriptor
					.getDefaultAssertionConsumerService();
			log.debug("Using default consumer service with binding {}",
					service.getBinding());
			return service;
		}

		// Iterate and find first match
		if (services.size() > 0) {
			for (AssertionConsumerService service : services) {
				if (isEndpointSupported(service)) {
					log.debug(
							"Using first available consumer service with binding {}",
							service.getBinding());
					return service;
				}
			}
		}

		log.debug("No supported assertion consumer service found for SP");
		throw new MetadataProviderException(
				"Service provider has no assertion consumer services available for the selected profile"
						+ spDescriptor);

	}

	protected boolean isEndpointSupported(AssertionConsumerService endpoint)
			throws MetadataProviderException {
		return org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI
				.equals(endpoint.getBinding())
				| org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI
						.equals(endpoint.getBinding());
	}

	protected boolean isEndpointSupported(SingleSignOnService endpoint)
			throws MetadataProviderException {
		return org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI
				.equals(endpoint.getBinding())
				|| org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI
						.equals(endpoint.getBinding())
				|| org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI
						.equals(endpoint.getBinding());
	}

	protected Response getResponse(SAMLMessageContext context,
			IDPWebSSOProfileOptions options,
			AssertionConsumerService assertionConsumer,
			SingleSignOnService bindingService, String requestID)
            throws SAMLException, MetadataProviderException, ConfigurationException {
		SAMLObjectBuilder<Response> builder = (SAMLObjectBuilder<Response>) builderFactory
				.getBuilder(Response.DEFAULT_ELEMENT_NAME);
		Response response = builder.buildObject();
		// set common attributes
		response.setVersion(SAMLVersion.VERSION_20);
		response.setID(generateID());
		response.setInResponseTo(requestID);

		DateTime issueInstant = new DateTime();

		response.setIssueInstant(issueInstant);
		if (null != assertionConsumer) {
			response.setDestination(assertionConsumer.getLocation());
		}
		response.setIssuer(getIssuer(context.getLocalEntityId(),
				options.getIssuerFormat()));

		// set status
		response.setStatus(getStatus(StatusCode.SUCCESS_URI, null));

		// set assertion
		response.getAssertions().add(
				getAssertion(context, options, issueInstant, requestID,
						assertionConsumer));

		return response;
	}

	protected Issuer getIssuer(String localEntityId, String issuerFormat) {
		SAMLObjectBuilder<Issuer> issuerBuilder = (SAMLObjectBuilder<Issuer>) builderFactory
				.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
		Issuer issuer = issuerBuilder.buildObject();
		issuer.setValue(localEntityId);
		if (null != issuerFormat) {
			issuer.setFormat(issuerFormat);
		}
		return issuer;
	}

	protected Assertion getAssertion(SAMLMessageContext context,
			IDPWebSSOProfileOptions options, DateTime issueInstant,
			String inResponseTo, AssertionConsumerService acs)
            throws SAMLException, ConfigurationException {

		SAMLObjectBuilder<Assertion> builder = (SAMLObjectBuilder<Assertion>) builderFactory
				.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
		Assertion assertion = builder.buildObject();
		assertion.setID(generateID());
		assertion.setVersion(SAMLVersion.VERSION_20);

		assertion.setIssuer(getIssuer(context.getLocalEntityId(),
				options.getIssuerFormat()));
		assertion.setIssueInstant(issueInstant);

		assertion.setConditions(getConditions(options, issueInstant));

		assertion.setSubject(getSubject(options, issueInstant, inResponseTo,
				acs));

		assertion.getAuthnStatements().add(getAuthnStatement(options));

        assertion.getAttributeStatements().add(getAttribuite());

		return assertion;

	}


    private AttributeStatement getAttribuite() throws ConfigurationException {
        // Builder Attributes
        SAMLObjectBuilder attrStatementBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(AttributeStatement.DEFAULT_ELEMENT_NAME);
        AttributeStatement attrStatement = (AttributeStatement) attrStatementBuilder.buildObject();

        for (Object key : props.keySet()) {
            Attribute attrFirstName = buildStringAttribute((String)key, (String)props.get(key), builderFactory);
            attrStatement.getAttributes().add(attrFirstName);
        }


        return attrStatement;
    }

    /**
     * Builds a SAML Attribute of type String
     *
     * @param name
     * @param value
     * @param builderFactory
     * @return
     * @throws org.opensaml.xml.ConfigurationException
     */
    public static Attribute buildStringAttribute(String name, String value, XMLObjectBuilderFactory builderFactory) throws ConfigurationException {
        SAMLObjectBuilder attrBuilder = (SAMLObjectBuilder) builderFactory.getBuilder(Attribute.DEFAULT_ELEMENT_NAME);
        Attribute attrFirstName = (Attribute) attrBuilder.buildObject();
        attrFirstName.setName(name);

        // Set custom Attributes
        XMLObjectBuilder stringBuilder = builderFactory.getBuilder(XSString.TYPE_NAME);
        XSString attrValueFirstName = (XSString) stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        attrValueFirstName.setValue(value);

        attrFirstName.getAttributeValues().add(attrValueFirstName);
        return attrFirstName;
    }

	protected Conditions getConditions(IDPWebSSOProfileOptions options,
			DateTime issuerInstant) {
		SAMLObjectBuilder<Conditions> builder = (SAMLObjectBuilder<Conditions>) builderFactory
				.getBuilder(Conditions.DEFAULT_ELEMENT_NAME);
		SAMLObjectBuilder<AudienceRestriction> audienceRestrictionBuilder = (SAMLObjectBuilder<AudienceRestriction>) builderFactory
				.getBuilder(AudienceRestriction.DEFAULT_ELEMENT_NAME);
		SAMLObjectBuilder<Audience> audienceBuilder = (SAMLObjectBuilder<Audience>) builderFactory
				.getBuilder(Audience.DEFAULT_ELEMENT_NAME);

		// TODO: add One time use??
		// TODO: add other Condition elements??

		Conditions conditions = builder.buildObject();
		conditions.setNotBefore(issuerInstant);

		String[] audienceURLs = options.getAudienceURLs();
		if (null != audienceURLs) {
			AudienceRestriction audienceRestriction = audienceRestrictionBuilder
					.buildObject();
			conditions.getAudienceRestrictions().add(audienceRestriction);
			for (String audienceURL : audienceURLs) {
				Audience audience = audienceBuilder.buildObject();
				audience.setAudienceURI(audienceURL);
				audienceRestriction.getAudiences().add(audience);
			}
		}
		return conditions;
	}

	protected Subject getSubject(IDPWebSSOProfileOptions options,
			DateTime issueInstant, String inResponseTo,
			AssertionConsumerService acs) {
		SAMLObjectBuilder<Subject> builder = (SAMLObjectBuilder<Subject>) builderFactory
				.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
		Subject subject = builder.buildObject();
		subject.setNameID(getNameID(options));

		subject.getSubjectConfirmations()
				.add(getSubjectConfirmation(options, issueInstant,
						inResponseTo, acs));
		return subject;
	}

	protected NameID getNameID(IDPWebSSOProfileOptions options) {
		SAMLObjectBuilder<NameID> builder = (SAMLObjectBuilder<NameID>) builderFactory
				.getBuilder(NameID.DEFAULT_ELEMENT_NAME);
		NameID nameID = builder.buildObject();

		String idFormat = options.getIdFormat();
		if (idFormat.equals("unspecified"))
			nameID.setFormat(NameIDType.UNSPECIFIED);
		else if (idFormat.equals("email"))
			nameID.setFormat(NameIDType.EMAIL);
		else if (idFormat.equals("transient"))
			nameID.setFormat(NameIDType.TRANSIENT);

		// TODO set current user's id
		nameID.setValue(SecurityContextHolder.getContext().getAuthentication().getName());

		return nameID;
	}

	protected SubjectConfirmation getSubjectConfirmation(
			IDPWebSSOProfileOptions options, DateTime issueInstant,
			String inResponseTo, AssertionConsumerService acs) {
		SAMLObjectBuilder<SubjectConfirmation> builder = (SAMLObjectBuilder<SubjectConfirmation>) builderFactory
				.getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		SubjectConfirmation sc = builder.buildObject();
		// For SSO Profile, Bearer method is used
		sc.setMethod(SubjectConfirmation.METHOD_BEARER);
		sc.setSubjectConfirmationData(getSubjectConfirmationData(options,
				issueInstant, inResponseTo, acs));
		return sc;
	}

	protected SubjectConfirmationData getSubjectConfirmationData(
			IDPWebSSOProfileOptions options, DateTime issueInstant,
			String inResponseTo, AssertionConsumerService acs) {
		SAMLObjectBuilder<SubjectConfirmationData> builder = (SAMLObjectBuilder<SubjectConfirmationData>) builderFactory
				.getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		SubjectConfirmationData scd = builder.buildObject();
		int assertionLifetime = options.getAssertionLifetime() <= 0 ? getAssertionLifetime()
				: options.getAssertionLifetime();
		scd.setNotOnOrAfter(issueInstant.plusSeconds(assertionLifetime));

		scd.setRecipient(acs.getLocation());

		if (null != inResponseTo) {
			scd.setInResponseTo(inResponseTo);
		}
		return scd;
	}

	protected AuthnStatement getAuthnStatement(IDPWebSSOProfileOptions options)
			throws SAMLException {
		SAMLObjectBuilder<AuthnStatement> builder = (SAMLObjectBuilder<AuthnStatement>) builderFactory
				.getBuilder(AuthnStatement.DEFAULT_ELEMENT_NAME);
		SAMLObjectBuilder<AuthnContext> authnContextBuilder = (AuthnContextBuilder) builderFactory
				.getBuilder(AuthnContext.DEFAULT_ELEMENT_NAME);
		SAMLObjectBuilder<AuthnContextClassRef> authnContextClassRefBuilder = (AuthnContextClassRefBuilder) builderFactory
				.getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);

		AuthnStatement as = builder.buildObject();
		// TODO
		/**
		 * Specifies the time at which the authentication took place. should get
		 * the time from spring security
		 */
		as.setAuthnInstant(new DateTime());

		AuthnContext authnContext = authnContextBuilder.buildObject();

		AuthnContextClassRef authnContextClassRef = authnContextClassRefBuilder
				.buildObject();

		// TODO: (Phase2) what other authentication methods are supported...
		// for Phase1 only password authentication is supported
		if (options.getAuthenticationMethod().equals("password"))
			authnContextClassRef
					.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
		else {
			log.debug("Unsupport authentication method {}",
					options.getAuthenticationMethod());
			throw new SAMLException("Unsupport authentication method"
					+ options.getAuthenticationMethod());
		}

		authnContext.setAuthnContextClassRef(authnContextClassRef);
		as.setAuthnContext(authnContext);
		return as;
	}

	@Override
	public String getProfileIdentifier() {
		return SAMLConstants.SAML2_WEBSSO_PROFILE_URI;
	}

	public int getRequestSkew() {
		return requestSkew;
	}

	public void setRequestSkew(int requestSkew) {
		this.requestSkew = requestSkew;
	}

	public int getAssertionLifetime() {
		return assertionLifetime;
	}

	public void setAssertionLifetime(int assertionLifetime) {
		this.assertionLifetime = assertionLifetime;
	}

    public void afterPropertiesSet() throws ServletException, IOException {
        props = PropertiesLoaderUtils.loadProperties(resource);

    }
}
