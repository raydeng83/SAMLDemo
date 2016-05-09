<%@tag import="org.springframework.security.core.Authentication"%>
<%@tag import="org.springframework.security.saml.SAMLCredential"%>
<%@tag import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@tag description="Main template after sign" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="pageTitle"%>

<%
	String uri = request.getRequestURI();
	String menu1class = "";
	String menu2class = "";
	if(uri.indexOf("/web/metadata")>-1){
		menu1class = "active";
	}else if(uri.equalsIgnoreCase("/spdemo/saml/web/metadata")){
		menu2class = "active";
	}
	request.setAttribute("menu1class", menu1class);
	request.setAttribute("menu2class", menu2class);
%>

<template:main-parent pageTitle="${pageTitle}">
	<jsp:attribute name="header">
				<ul class="nav navbar-nav">
						<li class="${menu1class}"><a href="<c:url value="/web/metadata"/>">Metadata</a></li>
					</ul>
    </jsp:attribute>
	<jsp:body>
		<div class="container">
				<jsp:doBody />
		</div>
    </jsp:body>
</template:main-parent>