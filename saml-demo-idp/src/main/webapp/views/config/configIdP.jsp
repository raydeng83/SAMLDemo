<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<template:main-signed pageTitle="IdP Configuration">
	<form role="form">
		<div class="form-group">
			<label for="samlVersion">SAML Version</label> <select
				id="samlVersion" name="samlVersion" class="form-control">
				<option value="2.0">2.0</option>
			</select>
		</div>
		<div class="form-group">
			<label for="issuer">Issuer</label> <input type="text"
				class="form-control" id="issuer" name="issuer"
				placeholder="Enter Issuer">
		</div>
		<div class="form-group">
			<label for="idpCert">Identity Provider Certificate</label> <input
				type="file" id="idpCert">
			<p class="help-block">Upload the Certificate file which provided
				by Identity Provider</p>
		</div>
		<div class="form-group">
			<label for="customErrorUrl">Custom Error URL</label> <input
				type="text" class="form-control" id="customErrorUrl"
				name="customErrorUrl" placeholder="Enter Cutom Error URL">
		</div>
		<div class="form-group">
			<label>SAML Identity Type</label>
			<div class="radio">
				<label> <input type="radio" name="samlIdentityType"
					id="samlIdentityType1" value="username" checked> Assertion
					contains User's username
				</label>
			</div>
			<div class="radio">
				<label> <input type="radio" name="samlIdentityType"
					id="samlIdentityType2" value="federationId"> Assertion
					contains the Federation ID from the User object
				</label>
			</div>
		</div>

		<div class="form-group">
			<label>SAML Identity Location</label>
			<div class="radio">
				<label> <input type="radio" name="samlIdentityLocation"
					id="samlIdentityLocation1" value="subject" checked>Identity
					is in the NameIdentifier element of the Subject statement
				</label>
			</div>
			<div class="radio">
				<label> <input type="radio" name="samlIdentityLocation"
					id="samlIdentityLocation2" value="attribute">Identity is in
					an Attribute element
				</label>
			</div>
		</div>

		<button type="submit" class="btn btn-default">Save</button>
	</form>
</template:main-signed>
