<%@tag import="org.springframework.security.core.Authentication"%>
<%@tag import="org.springframework.security.saml.SAMLCredential"%>
<%@tag import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@tag description="Main template after sign" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="pageTitle"%>

<%
	SAMLCredential credential = (SAMLCredential) SecurityContextHolder.getContext().getAuthentication().getCredentials();
	String userName =  credential.getNameID().getValue();
	request.setAttribute("userName", userName);
%>

<template:main-parent pageTitle="${pageTitle}">
	<jsp:attribute name="header">
				<ul class="nav navbar-nav">
						<li class="active"><a href="<c:url value="/"/>">Single Sign-On</a></li>
						<li><a href="<c:url value="/saml/web/metadata"/>">Metadata</a></li>
						<li><a href="<c:url value="/saml/web/websso/options"/>">WebSSO Profile Options</a></li>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li class="dropdown"><a href="#" class="dropdown-toggle"
				data-toggle="dropdown">${userName}<b class="caret"></b></a>
							<ul class="dropdown-menu">
								<li><a href="<c:url value="/saml/logout?local=true"/>">Logout</a></li>
								 <li><a href="<c:url value="/saml/logout"/>">Global Logout</a><br/></li>
							</ul>
						</li>					
					</ul>
    </jsp:attribute>
	<jsp:body>
		<div class="container">
				<jsp:doBody />
		</div>
    </jsp:body>
</template:main-parent>