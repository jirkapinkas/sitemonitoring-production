package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import lombok.Data;
import net.sf.sitemonitoring.entity.Page;
import net.sf.sitemonitoring.service.PageService;

@Data
@ManagedBean
@SessionScoped
public class PageController implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManagedProperty("#{pageService}")
	private PageService pageService;
	
	@ManagedProperty("#{menubarController}")
	private MenubarController menubarController;

	private List<Page> pages;
	
	@PostConstruct
	public void loadPages() {
		pages = pageService.findAll();
		menubarController.init();
	}
	
	public void save(String pageName) {
		if(pageName == null || pageName.trim().isEmpty()) {
			return;
		}
		Page page = new Page();
		page.setName(pageName);
		pageService.save(page);
		loadPages();
	}

	public void delete(int pageId) {
		pageService.delete(pageId);
		loadPages();
	}

}
