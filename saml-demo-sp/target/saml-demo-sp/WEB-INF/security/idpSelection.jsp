<%@page import="org.springframework.security.saml.metadata.MetadataManager"%>
<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletConfig().getServletContext());
    MetadataManager mm = context.getBean("metadata", MetadataManager.class);
    Set<String> idps = mm.getIDPEntityNames();
    pageContext.setAttribute("idp", idps);
%>
<template:main-nosign pageTitle="Spring Security SAML Extension - Metadata">
<h1>IDP selection</h1>

	<form class="form"
		action="<c:url value="${requestScope.idpDiscoReturnURL}"/>"
		method="GET">
		<div class="form-group">
			<label>Select IDP: </label>
			<c:forEach var="idpItem" items="${idp}">
				<div class="radio">
					<label> <input type="radio"
						name="${requestScope.idpDiscoReturnParam}"
						id="idp_<c:out value="${idpItem}"/>"
						value="<c:out value="${idpItem}"/>"> ${idpItem}
					</label>
				</div>
			</c:forEach>
		</div>

		<button type="submit" class="btn btn-default">Sign In</button>
	</form>
	<br/>
    <a href="<c:url value="/saml/web/metadata"/>">Metadata information</a>
</template:main-nosign>

