<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<template:main-nosign pageTitle="Spring Security SAML Extension - Metadata">
<!-- 
<a href="<c:url value="/saml/web/metadata/generate"/>">
    Generate new service provider metadata
</a>
<br/>

<div class="panel panel-default">
	<div class="panel-heading">Default hosted service provider</div>
	<div class="panel-body">
		<c:forEach var="entity" items="${hostedSP}">
			<a
				href="<c:url value="/saml/web/metadata/display"><c:param name="entityId" value="${hostedSP}"/></c:url>">
				<c:out value="${hostedSP}" />
			</a>
		</c:forEach>
		<c:if test="${empty spList}"> - </c:if>
		<br /> 
		<small><i>Default service provider is available
				without selection of alias.</i></small>
	</div>
</div>
 -->
<div class="panel panel-default">
	<div class="panel-heading">Service providers</div>
	<div class="panel-body">
	    <c:forEach var="entity" items="${spList}">
	        <a href="<c:url value="/saml/web/metadata/display"><c:param name="entityId" value="${entity}"/></c:url>">
	            <c:out value="${entity}"/></a><br/>
	    </c:forEach>
	    <c:if test="${empty spList}"> - </c:if>		
	</div>
</div>

<div class="panel panel-default">
	<div class="panel-heading">Identity providers</div>
	<div class="panel-body">
	    <c:forEach var="entity" items="${idpList}">
	        <a href="<c:url value="/saml/web/metadata/display"><c:param name="entityId" value="${entity}"/></c:url>">
	            <c:out value="${entity}"/></a><br/>
	    </c:forEach>
	    <c:if test="${empty idpList}"> - </c:if>	
	</div>
</div>
<!-- 
<div class="panel panel-default">
	<div class="panel-heading"> Metadata providers </div>
	<div class="panel-body">
	  <c:forEach var="entity" items="${metadata}" varStatus="status">
	        <a href="<c:url value="/saml/web/metadata/provider"><c:param name="providerIndex" value="${status.index}"/></c:url>">
	            <c:out value="${entity}"/></a><br/>
    	</c:forEach>	
	</div>
</div>
 -->
<form class="form" action="<c:url value="/saml/web/metadata/refresh"/>">
    <input type="submit" class="btn btn-default" value="Refersh metadata"/>
</form>
</template:main-nosign>