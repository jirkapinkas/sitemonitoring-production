package net.sf.sitemonitoring.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import lombok.Data;
import net.sf.sitemonitoring.entity.Configuration;
import net.sf.sitemonitoring.service.ConfigurationService;

@Data
@ManagedBean
@ViewScoped
public class ConfigurationController implements Serializable {

	private static final long serialVersionUID = 1L;

	private Configuration configuration;

	@ManagedProperty("#{configurationService}")
	private ConfigurationService configurationService;

	@PostConstruct
	public void loadConfiguration() {
		configuration = configurationService.find();
	}

	public void save() {
		configurationService.save(configuration);
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Configuration saved"));
	}
}
