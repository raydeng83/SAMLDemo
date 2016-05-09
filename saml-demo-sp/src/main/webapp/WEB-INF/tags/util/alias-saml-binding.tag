<%@tag import="org.opensaml.common.xml.SAMLConstants"%>
<%@attribute name="content" required="true" type="java.lang.String"%><%
if(content.equals(SAMLConstants.SAML2_ARTIFACT_BINDING_URI)){
	out.write("HTTP-Artifact");
}else if(content.equals(SAMLConstants.SAML2_POST_BINDING_URI)){
	out.write("HTTP-POST");
}else if(content.equals(SAMLConstants.SAML2_SOAP11_BINDING_URI)){
	out.write("SOAP");
}else if(content.equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)){
	out.write("HTTP-Redirect");
}else if(content.equals(SAMLConstants.SAML2_PAOS_BINDING_URI)){
	out.write("PAOS");
}
%>
