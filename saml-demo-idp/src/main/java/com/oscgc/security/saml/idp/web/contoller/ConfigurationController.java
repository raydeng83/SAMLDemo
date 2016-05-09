package com.oscgc.security.saml.idp.web.contoller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/config")
public class ConfigurationController {
	@RequestMapping(value = "/idp", method = RequestMethod.GET)
	public String configIdP() {
		return "config/configIdP";
	}
}
