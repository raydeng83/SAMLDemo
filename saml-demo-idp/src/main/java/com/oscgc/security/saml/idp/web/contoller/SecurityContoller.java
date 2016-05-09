package com.oscgc.security.saml.idp.web.contoller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class SecurityContoller {
	protected static Logger logger = LoggerFactory
			.getLogger(SecurityContoller.class);

	@RequestMapping("/login")
	public String toLoginPage(ModelMap model) {
		logger.debug("Received request to show login page");
		return "login";
	}
}
