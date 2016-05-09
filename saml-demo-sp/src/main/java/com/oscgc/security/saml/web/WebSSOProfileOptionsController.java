package com.oscgc.security.saml.web;

import java.util.ArrayList;
import java.util.List;

import com.oscgc.security.saml.websso.WebSSOProfileOptionsExt;
import org.apache.commons.lang.StringUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/websso/options")
public class WebSSOProfileOptionsController {
	private final Logger log = LoggerFactory
			.getLogger(WebSSOProfileOptionsController.class);

//	@Autowired
//	protected WebSSOProfileOptionsRepository optionsRepository;

	@Autowired
	protected MetadataManager metadataManager;

	@Autowired
	protected WebSSOProfileOptions defaultWebSSOProfileOptions;

	@RequestMapping
	public ModelAndView list() {
		ModelAndView mav = new ModelAndView("/websso/options");
		// mav.addObject("options", optionsRepository.findAll());
		mav.addObject("idpList", metadataManager.getIDPEntityNames());
		return mav;
	}

	@RequestMapping("/edit")
	public ModelAndView edit(@RequestParam("entityId") String entityId)
			throws MetadataProviderException {
		if (StringUtils.isEmpty(entityId)) {
			log.error("entityId is null not allow");
			throw new IllegalArgumentException("entityId is null not allow");
		}

		EntityDescriptor ed = metadataManager.getEntityDescriptor(entityId);
		if (null == ed) {
			log.error("entityId {} is invaild.", entityId);
			throw new IllegalArgumentException("entityId " + entityId
					+ " is invaild.");
		}

		IDPSSODescriptor idpSSO = ed
				.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
		if (null == idpSSO) {
			log.error("entityId {} is invaild IDP.", entityId);
			throw new IllegalArgumentException("entityId " + entityId
					+ " is invaild IDP.");
		}

		List<String> bindings = new ArrayList<String>();
		for (SingleSignOnService ssons : idpSSO.getSingleSignOnServices()) {
			bindings.add(ssons.getBinding());
		}

		ModelAndView mav = new ModelAndView("/websso/optionsDetail");

//		WebSSOProfileOptionsExt options = optionsRepository
//				.findByEntityId(entityId);
//		if (null == options) {
//			options = defaultProfileOptions();
//			options.setEntityId(entityId);
//		}
//		mav.addObject("options", form(options));
//		mav.addObject("supportBindings", bindings);
		return mav;
	}

	@RequestMapping("/save")
	public ModelAndView save(
			@ModelAttribute("options") WebSSOProfileOptionsForm form,
			BindingResult bindingResult) throws MetadataProviderException {
		if (bindingResult.hasErrors()) {
			ModelAndView modelAndView = new ModelAndView(
					"/websso/optionsDetails");
			modelAndView.addObject("hasErrors", true);
			modelAndView.addObject("errors", bindingResult.getAllErrors());
			return modelAndView;
		}

		WebSSOProfileOptionsExt options = new WebSSOProfileOptionsExt();
		BeanUtils.copyProperties(form, options);
		options.setAllowCreate(form.isAllowCreate());
		options.setForceAuthN(form.isForceAuthn());
		options.setIncludeScoping(form.isIncludeScoping());
		options.setPassive(form.isPassive());

//		optionsRepository.save(options);

		ModelAndView mav = edit(form.getEntityId());
		mav.addObject("msg", "Save done.");
		return mav;
	}

	protected WebSSOProfileOptionsForm form(WebSSOProfileOptionsExt options) {
		WebSSOProfileOptionsForm form = new WebSSOProfileOptionsForm();
		BeanUtils.copyProperties(options, form);
		form.setAllowCreate(options.isAllowCreate());
		form.setForceAuthn(options.getForceAuthN());
		form.setIncludeScoping(options.isIncludeScoping());
		form.setPassive(options.getPassive());
		return form;
	}

	protected WebSSOProfileOptionsExt defaultProfileOptions() {
		if (null == defaultWebSSOProfileOptions)
			return new WebSSOProfileOptionsExt();
		WebSSOProfileOptionsExt result = new WebSSOProfileOptionsExt();
		BeanUtils.copyProperties(defaultWebSSOProfileOptions, result);
		result.setAllowCreate(defaultWebSSOProfileOptions.isAllowCreate());
		result.setForceAuthN(defaultWebSSOProfileOptions.getForceAuthN());
		result.setIncludeScoping(defaultWebSSOProfileOptions.isIncludeScoping());
		result.setPassive(defaultWebSSOProfileOptions.getPassive());
		return result;
	}
}
