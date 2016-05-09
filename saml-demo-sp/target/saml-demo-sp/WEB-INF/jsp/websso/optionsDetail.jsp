<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<%@taglib prefix="util" tagdir="/WEB-INF/tags/util"%>

<template:main-nosign pageTitle="WebSSO Profile Options Detail">
	<c:if test="${not empty msg}">
		<div class="alert alert-success"> ${msg} </div>
	</c:if>
	<c:if test="${hasErrors}">
		<div class="alert alert-danger">On snap! something wrong...</div>
	</c:if>
	<div class="panel panel-default">
		<div class="panel-heading">WebSSO Profile Options</div>
		<div class="panel-body">
			<form:form commandName="options" cssClass="form-horizontal" action="${pageContext.request.contextPath}/saml/web/websso/options/save" >
				<div class="form-group">
					<label for="entityId" class="col-lg-3 control-label">IDP's EntityId</label>
					<div class="col-lg-8">
						<input type="hidden" id="entityId" name="entityId" value="${options.entityId}">
						<p class="form-control-static">${options.entityId}</p>
					</div>
				</div>
				<div class="form-group">
					<label for="binding" class="col-lg-3 control-label">SAML Binding</label>
					<div class="col-lg-8">
						<form:select path="binding" multiple="false" cssClass="form-control">
							<c:forEach var="entity" items="${supportBindings}">
								<form:option
									value="${entity}"><util:alias-saml-binding content="${entity}"></util:alias-saml-binding></form:option>
							</c:forEach>
						</form:select>
						<span class="help-block">Sets binding to be used for for sending SAML message to IDP</span>
					</div>
				</div>
				<div class="form-group">
					<label for="assertionConsumerIndex" class="col-lg-3 control-label">Assertion Consumer Index</label>
					<div class="col-lg-8">
						<form:input path="assertionConsumerIndex" cssClass="form-control"/>
						<span class="help-block">When set determines assertionConsumerService and binding to which should IDP send response. By default service is determined automatically. Available indexes can be found in metadata of this service provider.</span>
					</div>
				</div>
				<div class="form-group">
					<label for="nameID" class="col-lg-3 control-label">NameID Policy</label>
					<div class="col-lg-8">
						<form:select path="nameID" multiple="false" cssClass="form-control">
							<form:option value="urn:oasis:names:tc:SAML:2.0:nameid-format:transient">TRANSIENT</form:option>
							<form:option value="urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddres">EMAIL</form:option>
							<form:option value="urn:oasis:names:tc:SAML:2.0:nameid-format:persistent">PERSISTENT</form:option>
							<form:option value="urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName">X509_SUBJECT</form:option>
							<form:option value="urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos">KERBEROS</form:option>
							<form:option value="urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified">UNSPECIFIED</form:option>
						</form:select>
						<span class="help-block">When set determines which NameIDPolicy will be requested as part of the AuthnRequest sent to the IDP.</span>
					</div>
				</div>
				<div class="form-group">
					<label for="allowCreate" class="col-lg-3 control-label">Allow Create</label>
					<div class="col-lg-8">
						<form:checkbox path="allowCreate"/>
						<span class="help-block">Flag indicating whether IDP can create new user based on the current authentication request. Null value will omit field from the request.</span>
					</div>
				</div>
				<div class="form-group">
					<label for="passive" class="col-lg-3 control-label">Passive</label>
					<div class="col-lg-8">
						<form:checkbox path="passive"/>
						<span class="help-block">Sets whether the IdP should refrain from interacting with the user during the authentication process</span>
					</div>
				</div>
				<div class="form-group">
					<label for="forceAuthn" class="col-lg-3 control-label">Force Authn</label>
					<div class="col-lg-8">
						<form:checkbox path="forceAuthn"/>
						<span class="help-block">Sets whether the IdP should force the user to reauthenticate</span>
					</div>
				</div>
				<div class="form-group">
					<label for="relayState" class="col-lg-3 control-label">RealyState</label>
					<div class="col-lg-8">
						<form:input path="relayState" cssClass="form-control"/>
						<span class="help-block">Relay state sent to the IDP as part of the authentication request. Value will be returned by IDP.</span>
					</div>
				</div>				
			  <div class="form-group">
			    <div class="col-lg-offset-3 col-lg-8">
			      <button type="submit" class="btn btn-default">Save</button>
			    </div>
			  </div>
			</form:form>
		</div>
	</div>
</template:main-nosign>