<%@tag description="Overall Page template" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@taglib prefix="template" tagdir="/WEB-INF/tags/template"%>
<%@attribute name="header" fragment="true"%>
<%@attribute name="pageTitle"%>
<template:genericpage pageTitle="${pageTitle}">
		<div class="navbar navbar-default navbar-fixed-top navbar-inverse">
			<div class="container">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse"
						data-target=".navbar-collapse">
						<span class="icon-bar"></span> <span class="icon-bar"></span> <span
							class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="#">Identity Provider</a>
				</div>
				<div class="collapse navbar-collapse">
						<jsp:invoke fragment="header" />
				</div>
				<!--/.nav-collapse -->
			</div>
		</div>
		<jsp:doBody />
</template:genericpage>