package com.oscgc.security.saml.idp.web.contoller;

import com.oscgc.security.saml.websso.IDPWebSSOProfile;
import com.oscgc.security.saml.websso.IDPWebSSOProfileOptions;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLException;
import org.opensaml.common.SAMLObjectBuilder;
import org.opensaml.common.SAMLRuntimeException;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.saml2.metadata.*;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.security.ServletRequestX509CredentialAdapter;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.ws.transport.http.HttpServletResponseAdapter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.ChainingEncryptedKeyResolver;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.trust.ExplicitX509CertificateTrustEngine;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.BasicX509CredentialNameEvaluator;
import org.opensaml.xml.security.x509.PKIXX509CredentialTrustEngine;
import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.signature.impl.PKIXSignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLConstants;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.processor.SAMLProcessor;
import org.springframework.security.saml.storage.HttpSessionStorageFactory;
import org.springframework.security.saml.storage.SAMLMessageStorageFactory;
import org.springframework.security.saml.trust.CertPathPKIXTrustEvaluator;
import org.springframework.security.saml.trust.MetadataCredentialResolver;
import org.springframework.security.saml.trust.PKIXInformationResolver;
import org.springframework.security.saml.util.SAMLUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.*;

@Controller
@RequestMapping("/saml")
public class SAMLSSOContoller implements InitializingBean {

	private final static Logger log = LoggerFactory
			.getLogger(SAMLSSOContoller.class);

	// Way to obtain encrypted key info from XML Encryption
	private static ChainingEncryptedKeyResolver encryptedKeyResolver = new ChainingEncryptedKeyResolver();

    private Resource resource = new ClassPathResource("samlAttributes.properties");

    private Properties props;

	static {
		encryptedKeyResolver.getResolverChain().add(
				new InlineEncryptedKeyResolver());
		encryptedKeyResolver.getResolverChain().add(
				new EncryptedElementTypeEncryptedKeyResolver());
		encryptedKeyResolver.getResolverChain().add(
				new SimpleRetrievalMethodEncryptedKeyResolver());
	}

	@Autowired
	protected MetadataManager metadataManager;

	@Autowired
	protected KeyManager keyManager;

	@Autowired
	protected SAMLProcessor processor;

	protected PKIXInformationResolver pkixResolver;

	protected MetadataCredentialResolver metadataResolver;

	protected SAMLMessageStorageFactory storageFactory = new HttpSessionStorageFactory();

	protected XMLObjectBuilderFactory builderFactory;

	@Autowired
	protected IDPWebSSOProfile idpProfile;
	@Autowired
	protected SAMLContextProvider contextProvider;

	private String idpEntityId = "http://localhost:9090/saml-demo-idp";

	/**
	 * Maximum time from assertion creation when the message is deemed valid.
	 */
	private int assertionLifetime = 60;



	public SAMLSSOContoller() {
		builderFactory = Configuration.getBuilderFactory();
	}

	@RequestMapping("/spsites")
	public ModelAndView listSPSites() {
		ModelAndView model = new ModelAndView("/saml/spsites");
		model.addObject("spList", metadataManager.getSPEntityNames());
		return model;
	}

	@RequestMapping("/sso/**")
	public void spInitiated(HttpServletRequest request,
			HttpServletResponse response) throws MetadataProviderException,
			MessageDecodingException, SAMLException, SecurityException,
			ServletException, MessageEncodingException, ValidationException,
			DecryptionException {
        Enumeration rnames=request.getParameterNames();
        for (Enumeration e = rnames ; e.hasMoreElements() ;) {
            String thisName=e.nextElement().toString();
            String thisValue=request.getParameter(thisName);
            System.out.println(thisName+"-------"+thisValue);
        }
		SAMLMessageContext context = contextProvider.getLocalEntity(request,
				response);
        // Decode the message
//        MessageDecoder decoder = new  HTTPRedirectDeflateDecoder();
//        decoder.decode(context);
//        context.getInboundMessage().getDOM().
//        System.out.println("bindings---->"+getBinding(request.getRequestURI()));
		processor.retrieveMessage(context, getBinding(request.getRequestURI()));

		// override properties
		context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);


		IDPWebSSOProfileOptions options = new IDPWebSSOProfileOptions();
		options.setAssertionConsumerIndex(1);
		options.setAudienceURLs(new String[] { "http://localhost:8080/saml-demo-sp" });

		idpProfile.sendAuthenticationResponse(context, options);
	}

	protected String getBinding(String requestUrl) throws ServletException {
		if (requestUrl.indexOf("/SSORedirect/") > -1) {
			return org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
		} else if (requestUrl.indexOf("SSOPOST") > -1) {
			return org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("{} is invaild single sign on service", requestUrl);
			}
			throw new ServletException(requestUrl
					+ " is invaild single sign on service");
		}
	}

	@RequestMapping("/idpinitiate")
	public void idpInitiated(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam("spEntityId") String spEntityId)
            throws MetadataProviderException, SAMLException,
            MessageEncodingException, ConfigurationException {
		if (StringUtils.isEmpty(spEntityId)) {
			throw new IllegalArgumentException("no spEntityId is not allowed");
		}

		SAMLMessageContext context = new SAMLMessageContext();
		populateGenericContext(request, response, context);
		populateLocalEntityId(context, getIdpEntityId());
		populateLocalContext(context);
		populatePeerEntityId(context, spEntityId);
		populatePeerContext(context);

		IDPWebSSOProfileOptions options = new IDPWebSSOProfileOptions();
		options.setAssertionConsumerIndex(1);
		options.setAudienceURLs(new String[] { spEntityId });

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
				options, idpssoDescriptor, spDescriptor);
		Response samlResponse = getResponse(context, options, consumerService,
				ssoService);
		context.setCommunicationProfileId(getProfileIdentifier());
		context.setOutboundMessage(samlResponse);
		context.setOutboundSAMLMessage(samlResponse);
		context.setPeerEntityEndpoint(consumerService);

		boolean sign = spDescriptor.getWantAssertionsSigned();
		processor.sendMessage(context, sign);
	}

	protected String generateID() {
		Random r = new Random();
		return 'a' + Long.toString(Math.abs(r.nextLong()), 20)
				+ Long.toString(Math.abs(r.nextLong()), 20);
	}

	protected Response getResponse(SAMLMessageContext context,
			IDPWebSSOProfileOptions options,
			AssertionConsumerService assertionConsumer,
			SingleSignOnService bindingService) throws SAMLException,
            MetadataProviderException, ConfigurationException {
		SAMLObjectBuilder<Response> builder = (SAMLObjectBuilder<Response>) builderFactory
				.getBuilder(Response.DEFAULT_ELEMENT_NAME);
		Response response = builder.buildObject();
		// set common attributes
		response.setVersion(SAMLVersion.VERSION_20);
		response.setID(generateID());

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
		response.getAssertions()
				.add(getAssertion(context, options, issueInstant,
						assertionConsumer));

		return response;
	}

	protected Assertion getAssertion(SAMLMessageContext context,
			IDPWebSSOProfileOptions options, DateTime issueInstant,
			AssertionConsumerService acs) throws SAMLException, ConfigurationException {

		SAMLObjectBuilder<Assertion> builder = (SAMLObjectBuilder<Assertion>) builderFactory
				.getBuilder(Assertion.DEFAULT_ELEMENT_NAME);
		Assertion assertion = builder.buildObject();
		assertion.setID(generateID());
		assertion.setVersion(SAMLVersion.VERSION_20);

		assertion.setIssuer(getIssuer(context.getLocalEntityId(),
				options.getIssuerFormat()));
		assertion.setIssueInstant(issueInstant);

		assertion.setConditions(getConditions(options, issueInstant));

		assertion.setSubject(getSubject(options, issueInstant, acs));

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

	protected Subject getSubject(IDPWebSSOProfileOptions options,
			DateTime issueInstant, AssertionConsumerService acs) {
		SAMLObjectBuilder<Subject> builder = (SAMLObjectBuilder<Subject>) builderFactory
				.getBuilder(Subject.DEFAULT_ELEMENT_NAME);
		Subject subject = builder.buildObject();
		subject.setNameID(getNameID(options));

		subject.getSubjectConfirmations().add(
				getSubjectConfirmation(options, issueInstant, acs));
		return subject;
	}

	protected SubjectConfirmation getSubjectConfirmation(
			IDPWebSSOProfileOptions options, DateTime issueInstant,
			AssertionConsumerService acs) {
		SAMLObjectBuilder<SubjectConfirmation> builder = (SAMLObjectBuilder<SubjectConfirmation>) builderFactory
				.getBuilder(SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		SubjectConfirmation sc = builder.buildObject();
		// For SSO Profile, Bearer method is used
		sc.setMethod(SubjectConfirmation.METHOD_BEARER);
		sc.setSubjectConfirmationData(getSubjectConfirmationData(options,
				issueInstant, acs));
		return sc;
	}

	protected SubjectConfirmationData getSubjectConfirmationData(
			IDPWebSSOProfileOptions options, DateTime issueInstant,
			AssertionConsumerService acs) {
		SAMLObjectBuilder<SubjectConfirmationData> builder = (SAMLObjectBuilder<SubjectConfirmationData>) builderFactory
				.getBuilder(SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		SubjectConfirmationData scd = builder.buildObject();
		int assertionLifetime = options.getAssertionLifetime() <= 0 ? getAssertionLifetime()
				: options.getAssertionLifetime();
		scd.setNotOnOrAfter(issueInstant.plusSeconds(assertionLifetime));

		scd.setRecipient(acs.getLocation());
		return scd;
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
		nameID.setValue(SecurityContextHolder.getContext().getAuthentication()
				.getName());

		return nameID;
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

	protected Status getStatus(String code, String statusMessage) {
		SAMLObjectBuilder<StatusCode> codeBuilder = (SAMLObjectBuilder<StatusCode>) builderFactory
				.getBuilder(StatusCode.DEFAULT_ELEMENT_NAME);
		StatusCode statusCode = codeBuilder.buildObject();
		statusCode.setValue(code);

		SAMLObjectBuilder<Status> statusBuilder = (SAMLObjectBuilder<Status>) builderFactory
				.getBuilder(Status.DEFAULT_ELEMENT_NAME);
		Status status = statusBuilder.buildObject();
		status.setStatusCode(statusCode);

		if (statusMessage != null) {
			SAMLObjectBuilder<StatusMessage> messageBuilder = (SAMLObjectBuilder<StatusMessage>) builderFactory
					.getBuilder(StatusMessage.DEFAULT_ELEMENT_NAME);
			StatusMessage statusMessageObject = messageBuilder.buildObject();
			statusMessageObject.setMessage(statusMessage);
			status.setStatusMessage(statusMessageObject);
		}

		return status;
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

	protected boolean isEndpointSupported(SingleSignOnService endpoint)
			throws MetadataProviderException {
		return org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI
				.equals(endpoint.getBinding())
				|| org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI
						.equals(endpoint.getBinding())
				|| org.opensaml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI
						.equals(endpoint.getBinding());
	}

	protected boolean isEndpointMatching(Endpoint endpoint, String binding) {
		return binding.equals(getEndpointBinding(endpoint));
	}

	protected String getEndpointBinding(Endpoint endpoint) {
		return SAMLUtil.getBindingForEndpoint(endpoint);
	}

	protected boolean isEndpointSupported(AssertionConsumerService endpoint)
			throws MetadataProviderException {
		return org.opensaml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI
				.equals(endpoint.getBinding())
				| org.opensaml.common.xml.SAMLConstants.SAML2_ARTIFACT_BINDING_URI
						.equals(endpoint.getBinding());
	}

	protected AssertionConsumerService getAssertionConsumerService(
			IDPWebSSOProfileOptions options, IDPSSODescriptor idpSSODescriptor,
			SPSSODescriptor spDescriptor) throws MetadataProviderException {

		List<AssertionConsumerService> services = spDescriptor
				.getAssertionConsumerServices();

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

	public String getProfileIdentifier() {
		return SAMLConstants.SAML2_WEBSSO_PROFILE_URI;
	}

	protected void populatePeerContext(SAMLMessageContext samlContext)
			throws MetadataProviderException {

		String peerEntityId = samlContext.getPeerEntityId();
		QName peerEntityRole = samlContext.getPeerEntityRole();

		if (peerEntityId == null) {
			throw new MetadataProviderException(
					"Peer entity ID wasn't specified, but is requested");
		}

		EntityDescriptor entityDescriptor = metadataManager
				.getEntityDescriptor(peerEntityId);
		RoleDescriptor roleDescriptor = metadataManager.getRole(peerEntityId,
				peerEntityRole,
				org.opensaml.common.xml.SAMLConstants.SAML20P_NS);
		ExtendedMetadata extendedMetadata = metadataManager
				.getExtendedMetadata(peerEntityId);

		if (entityDescriptor == null || roleDescriptor == null) {
			throw new MetadataProviderException("Metadata for entity "
					+ peerEntityId + " and role " + peerEntityRole
					+ " wasn't found");
		}

		samlContext.setPeerEntityMetadata(entityDescriptor);
		samlContext.setPeerEntityRoleMetadata(roleDescriptor);
		samlContext.setPeerExtendedMetadata(extendedMetadata);
	}

	protected void populatePeerEntityId(SAMLMessageContext context,
			String spEntityId) throws MetadataProviderException {

		context.setPeerEntityId(spEntityId);
		context.setPeerEntityRole(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
	}

	protected void populateGenericContext(HttpServletRequest request,
			HttpServletResponse response, SAMLMessageContext context)
			throws MetadataProviderException {

		HttpServletRequestAdapter inTransport = new HttpServletRequestAdapter(
				request);
		HttpServletResponseAdapter outTransport = new HttpServletResponseAdapter(
				response, request.isSecure());

		// Store attribute which cannot be located from InTransport directly
		request.setAttribute(
				org.springframework.security.saml.SAMLConstants.LOCAL_CONTEXT_PATH,
				request.getContextPath());

		context.setMetadataProvider(metadataManager);
		context.setInboundMessageTransport(inTransport);
		context.setOutboundMessageTransport(outTransport);

		context.setMessageStorage(storageFactory.getMessageStorage(request));

	}

	protected void populateLocalEntityId(SAMLMessageContext context,
			String idpEntityId) throws MetadataProviderException {
		context.setLocalEntityId(idpEntityId);
		context.setLocalEntityRole(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
	}

	protected void populateLocalContext(SAMLMessageContext context)
			throws MetadataProviderException {

		populateLocalEntity(context);
		populateDecrypter(context);
		populateSSLCredential(context);
		populatePeerSSLCredential(context);
		populateTrustEngine(context);
		populateSSLTrustEngine(context);

	}

	protected void populateSSLTrustEngine(SAMLMessageContext samlContext) {
		TrustEngine<X509Credential> engine;
		if ("pkix".equalsIgnoreCase(samlContext.getLocalExtendedMetadata()
				.getSslSecurityProfile())) {
			engine = new PKIXX509CredentialTrustEngine(pkixResolver,
					new CertPathPKIXTrustEvaluator(),
					new BasicX509CredentialNameEvaluator());
		} else {
			engine = new ExplicitX509CertificateTrustEngine(metadataResolver);
		}
		samlContext.setLocalSSLTrustEngine(engine);
	}

	protected void populateLocalEntity(SAMLMessageContext samlContext)
			throws MetadataProviderException {

		String localEntityId = samlContext.getLocalEntityId();
		QName localEntityRole = samlContext.getLocalEntityRole();

		if (localEntityId == null) {
			throw new MetadataProviderException(
					"No hosted service provider is configured and no alias was selected");
		}

		EntityDescriptor entityDescriptor = metadataManager
				.getEntityDescriptor(localEntityId);
		RoleDescriptor roleDescriptor = metadataManager.getRole(localEntityId,
				localEntityRole,
				org.opensaml.common.xml.SAMLConstants.SAML20P_NS);
		ExtendedMetadata extendedMetadata = metadataManager
				.getExtendedMetadata(localEntityId);

		if (entityDescriptor == null || roleDescriptor == null) {
			throw new MetadataProviderException("Metadata for entity "
					+ localEntityId + " and role " + localEntityRole
					+ " wasn't found");
		}

		samlContext.setLocalEntityMetadata(entityDescriptor);
		samlContext.setLocalEntityRoleMetadata(roleDescriptor);
		samlContext.setLocalExtendedMetadata(extendedMetadata);

		if (extendedMetadata.getSigningKey() != null) {
			samlContext.setLocalSigningCredential(keyManager
					.getCredential(extendedMetadata.getSigningKey()));
		} else {
			samlContext.setLocalSigningCredential(keyManager
					.getDefaultCredential());
		}

	}

	protected void populateSSLCredential(SAMLMessageContext samlContext) {

		X509Credential tlsCredential;
		if (samlContext.getLocalExtendedMetadata().getTlsKey() != null) {
			tlsCredential = (X509Credential) keyManager
					.getCredential(samlContext.getLocalExtendedMetadata()
							.getTlsKey());
		} else {
			tlsCredential = null;
		}

		samlContext.setLocalSSLCredential(tlsCredential);

	}

	protected void populatePeerSSLCredential(SAMLMessageContext samlContext) {

		X509Certificate[] chain = (X509Certificate[]) samlContext
				.getInboundMessageTransport()
				.getAttribute(
						ServletRequestX509CredentialAdapter.X509_CERT_REQUEST_ATTRIBUTE);

		if (chain != null && chain.length > 0) {

			BasicX509Credential credential = new BasicX509Credential();
			credential.setEntityCertificate(chain[0]);
			credential.setEntityCertificateChain(Arrays.asList(chain));
			samlContext.setPeerSSLCredential(credential);

		}

	}

	/**
	 * Populates a decrypter based on settings in the extended metadata or using
	 * a default credential when no encryption credential is specified in the
	 * extended metadata.
	 * 
	 * @param samlContext
	 *            context to populate decryptor for.
	 */
	protected void populateDecrypter(SAMLMessageContext samlContext) {

		// Locate encryption key for this entity
		Credential encryptionCredential;
		if (samlContext.getLocalExtendedMetadata().getEncryptionKey() != null) {
			encryptionCredential = keyManager.getCredential(samlContext
					.getLocalExtendedMetadata().getEncryptionKey());
		} else {
			encryptionCredential = keyManager.getDefaultCredential();
		}

		// Entity used for decrypting of encrypted XML parts
		// Extracts EncryptedKey from the encrypted XML using the
		// encryptedKeyResolver and attempts to decrypt it
		// using private keys supplied by the resolver.
		KeyInfoCredentialResolver resolver = new StaticKeyInfoCredentialResolver(
				encryptionCredential);

		Decrypter decrypter = new Decrypter(null, resolver,
				encryptedKeyResolver);
		decrypter.setRootInNewDocument(true);

		samlContext.setLocalDecrypter(decrypter);

	}

	/**
	 * Based on the settings in the extended metadata either creates a PKIX
	 * trust engine with trusted keys specified in the extended metadata as
	 * anchors or (by default) an explicit trust engine using data from the
	 * metadata or from the values overridden in the ExtendedMetadata.
	 * 
	 * @param samlContext
	 *            context to populate
	 */
	protected void populateTrustEngine(SAMLMessageContext samlContext) {
		SignatureTrustEngine engine;
		if ("pkix".equalsIgnoreCase(samlContext.getLocalExtendedMetadata()
				.getSecurityProfile())) {
			engine = new PKIXSignatureTrustEngine(pkixResolver, Configuration
					.getGlobalSecurityConfiguration()
					.getDefaultKeyInfoCredentialResolver(),
					new CertPathPKIXTrustEvaluator(),
					new BasicX509CredentialNameEvaluator());
		} else {
			engine = new ExplicitKeySignatureTrustEngine(metadataResolver,
					Configuration.getGlobalSecurityConfiguration()
							.getDefaultKeyInfoCredentialResolver());
		}
		samlContext.setLocalTrustEngine(engine);
	}

	@Override
	public void afterPropertiesSet() throws ServletException, IOException {

		metadataResolver = new MetadataCredentialResolver(metadataManager,
				keyManager);
		metadataResolver.setMeetAllCriteria(false);
		metadataResolver.setUnevaluableSatisfies(true);
		pkixResolver = new PKIXInformationResolver(metadataResolver,
				metadataManager, keyManager);

        props = PropertiesLoaderUtils.loadProperties(resource);

	}

	public String getIdpEntityId() {
		return idpEntityId;
	}

	public void setIdpEntityId(String idpEntityId) {
		this.idpEntityId = idpEntityId;
	}

	public int getAssertionLifetime() {
		return assertionLifetime;
	}

	public void setAssertionLifetime(int assertionLifetime) {
		this.assertionLifetime = assertionLifetime;
	}

}
