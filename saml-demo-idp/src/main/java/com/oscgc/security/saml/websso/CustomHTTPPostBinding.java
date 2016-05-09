package com.oscgc.security.saml.websso;

import org.apache.velocity.app.VelocityEngine;
import org.opensaml.common.binding.security.SAMLProtocolMessageXMLSignatureSecurityPolicyRule;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.binding.decoding.HTTPPostDecoder;
import org.opensaml.saml2.binding.encoding.HTTPPostEncoder;
import org.opensaml.saml2.binding.security.SAML2HTTPPostSimpleSignRule;
import org.opensaml.ws.message.decoder.MessageDecoder;
import org.opensaml.ws.message.encoder.MessageEncoder;
import org.opensaml.ws.security.SecurityPolicyRule;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.OutTransport;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HTTPOutTransport;
import org.opensaml.ws.transport.http.HTTPTransport;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.processor.SAMLBindingImpl;

import java.util.List;

/**
 * Created by vanish on 3/5/14.
 */
public class CustomHTTPPostBinding extends SAMLBindingImpl {

    /**
     * Pool for message deserializers.
     */
    protected ParserPool parserPool;

    /**
     * Creates default implementation of the binding.
     *
     * @param parserPool     parserPool for message deserialization
     * @param velocityEngine engine for message formatting
     */
    public CustomHTTPPostBinding(ParserPool parserPool, VelocityEngine velocityEngine) {
        this(parserPool, new HTTPPostDecoder(parserPool), new HTTPPostEncoder(velocityEngine, "/template/saml2-post-binding.vm"));
    }

    /**
     * Implementation of the binding with custom encoder and decoder.
     *
     * @param parserPool parserPool for message deserialization
     * @param decoder    custom decoder implementation
     * @param encoder    custom encoder implementation
     */
    public CustomHTTPPostBinding(ParserPool parserPool, MessageDecoder decoder, MessageEncoder encoder) {
        super(decoder, encoder);
        this.parserPool = parserPool;
    }

    public boolean supports(InTransport transport) {
        if (transport instanceof HTTPInTransport) {
            HTTPTransport t = (HTTPTransport) transport;
            return "POST".equalsIgnoreCase(t.getHTTPMethod()) && (t.getParameterValue("SAMLRequest") != null || t.getParameterValue("SAMLResponse") != null);
        } else {
            return false;
        }
    }

    public boolean supports(OutTransport transport) {
        return transport instanceof HTTPOutTransport;
    }

    public String getBindingURI() {
        return SAMLConstants.SAML2_POST_BINDING_URI;
    }

    @Override
    public void getSecurityPolicy(List<SecurityPolicyRule> securityPolicy, SAMLMessageContext samlContext) {

        SignatureTrustEngine engine = samlContext.getLocalTrustEngine();
        securityPolicy.add(new SAML2HTTPPostSimpleSignRule(engine, parserPool, engine.getKeyInfoResolver()));
        securityPolicy.add(new SAMLProtocolMessageXMLSignatureSecurityPolicyRule(engine));

    }
}
