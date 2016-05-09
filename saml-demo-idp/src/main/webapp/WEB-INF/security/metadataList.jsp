<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<template:main-signed pageTitle="Spring Security SAML Extension - Metadata">
<!-- 
<a href="<c:url value="/saml/web/metadata/generate"/>">
    Generate new service provider metadata
</a>
<br/>

<a href="<c:url value="/saml/login"/>">
    Initialize Single Sign-On
</a>
 -->
<div class="panel panel-default">
	<div class="panel-heading">Service providers</div>
	<div class="panel-body">
	    <c:forEach var="entity" items="${spList}">
	        <a href="<c:url value="/web/metadata/display"><c:param name="entityId" value="${entity}"/></c:url>">
	            <c:out value="${entity}"/></a><br/>
	    </c:forEach>
	    <c:if test="${empty spList}"> - </c:if>		
	</div>
</div>

<div class="panel panel-default">
	<div class="panel-heading">Identity providers</div>
	<div class="panel-body">
	    <c:forEach var="entity" items="${idpList}">
	        <a href="<c:url value="/web/metadata/display"><c:param name="entityId" value="${entity}"/></c:url>">
	            <c:out value="${entity}"/></a><br/>
	    </c:forEach>
	    <c:if test="${empty idpList}"> - </c:if>	
	</div>
</div>

<form class="form" action="<c:url value="/web/metadata/refresh"/>">
    <input type="submit" class="btn btn-default" value="Refersh metadata"/>
</form>
</template:main-signed>