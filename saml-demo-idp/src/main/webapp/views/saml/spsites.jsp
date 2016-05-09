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
<template:main-signed pageTitle="Service Provider selection">
<h1>Service Provider selection</h1>
	<form class="form"
		action="<c:url value="/web/saml/idpinitiate"/>"
		method="GET">
		<div class="form-group">
			<label>Select SP: </label>
			<c:forEach var="spEntityId" items="${spList}">
				<div class="radio">
					<label> <input type="radio"
						name="spEntityId"
						id="sp_<c:out value="${spEntityId}"/>"
						value="<c:out value="${spEntityId}"/>"> ${spEntityId}
					</label>
				</div>
			</c:forEach>
		</div>
		
		<button type="submit" class="btn btn-default">Sign In</button>
	</form>
	
	<br/>
	<script type="text/javascript">
	var testKeepAlive = function(){
	$.ajax({
				url : "http://localhost:7070/saml/keepalive",
				type : "POST",
				success : function() {
					alert(1);
				}
			});
	}
	
</script>
</template:main-signed>

