<%@tag import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@tag description="Main template after sign" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="pageTitle"%>

<%
	System.out.println(SecurityContextHolder.getContext().getAuthentication().getCredentials());
	System.out.println(SecurityContextHolder.getContext().getAuthentication().getDetails());
	System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	System.out.println(SecurityContextHolder.getContext().getAuthentication().getName());
	request.setAttribute("userName", SecurityContextHolder.getContext().getAuthentication().getName());
	
	String uri = request.getRequestURI();
	String menu1class = "";
	String menu2class = "";
	if(uri.indexOf("/saml/spsites")>-1 || uri.equals("/")){
		menu1class = "active";
	}else if(uri.indexOf("/metadata")>-1){
		menu2class = "active";
	}
	request.setAttribute("menu1class", menu1class);
	request.setAttribute("menu2class", menu2class);
%>

<template:main-parent pageTitle="${pageTitle}">
	<jsp:attribute name="header">
				<ul class="nav navbar-nav">
						<li class="${menu1class }"><a href="<c:url value="/web/saml/spsites"/>">Single Sign-On(IdP initiate)</a></li>
						<li class="${menu2class }"><a href="<c:url value="/web/metadata"/>">Metadatas</a></li>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li class="dropdown"><a href="#" class="dropdown-toggle"
				data-toggle="dropdown">${userName}<b class="caret"></b></a>
							<ul class="dropdown-menu">
								<li><a href="<c:url value="/j_spring_security_logout" />">Logout</a></li>
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