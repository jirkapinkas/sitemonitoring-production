package net.sf.sitemonitoring.controller;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import lombok.Data;
import net.sf.sitemonitoring.entity.Page;
import net.sf.sitemonitoring.service.PageService;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

@Data
@ManagedBean
@SessionScoped
public class MenubarController implements Serializable {

	private static final long serialVersionUID = 1L;

	private MenuModel menubar;
	
	@ManagedProperty("#{pageService}")
	private PageService pageService;

	@PostConstruct
	public void init() {
		menubar = new DefaultMenuModel();
		DefaultMenuItem menuItemDashboard = new DefaultMenuItem();
		menuItemDashboard.setUrl("dashboard.xhtml");
		menuItemDashboard.setValue("Dashboard");
		menubar.addElement(menuItemDashboard);

		DefaultSubMenu subMenuChecks = new DefaultSubMenu();
		subMenuChecks.setLabel("Checks");

		DefaultMenuItem menuItemUncategorizedChecks = new DefaultMenuItem();
		menuItemUncategorizedChecks.setUrl("checks.xhtml");
		menuItemUncategorizedChecks.setValue("Uncategorized");

		subMenuChecks.addElement(menuItemUncategorizedChecks);

		for (Page page : pageService.findAll()) {
			DefaultMenuItem menuItemDynamicPage = new DefaultMenuItem();
			menuItemDynamicPage.setUrl("checks.xhtml?page=" + page.getId());
			menuItemDynamicPage.setValue(page.getName());
			subMenuChecks.addElement(menuItemDynamicPage);
		}

		menubar.addElement(subMenuChecks);

		DefaultMenuItem menuItemPages = new DefaultMenuItem();
		menuItemPages.setUrl("pages.xhtml");
		menuItemPages.setValue("Manage pages");
		menubar.addElement(menuItemPages);

		DefaultMenuItem menuItemConfiguration = new DefaultMenuItem();
		menuItemConfiguration.setUrl("configuration.xhtml");
		menuItemConfiguration.setValue("Configuration");
		menubar.addElement(menuItemConfiguration);

		DefaultMenuItem menuItemLogout = new DefaultMenuItem();
		menuItemLogout.setUrl("../logout");
		menuItemLogout.setValue("Logout");
		menubar.addElement(menuItemLogout);
	}

}
