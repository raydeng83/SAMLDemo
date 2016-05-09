<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>

<template:main-nosign pageTitle="WebSSO Profile Options List">
	<div class="panel panel-default">
		<div class="panel-heading">IDP Sites</div>
		<div class="panel-body">
		    <c:forEach var="entity" items="${idpList}">
		        <a href="<c:url value="/saml/web/websso/options/edit"><c:param name="entityId" value="${entity}"/></c:url>">
		            <c:out value="${entity}"/></a><br/>
		    </c:forEach>
		    <c:if test="${empty idpList}"> No any IDP sites </c:if>			
		</div>
	</div>
</template:main-nosign>