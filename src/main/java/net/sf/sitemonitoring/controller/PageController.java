package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.Data;
import net.sf.sitemonitoring.annotation.ScopeSession;
import net.sf.sitemonitoring.entity.Page;
import net.sf.sitemonitoring.service.PageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@ScopeSession
public class PageController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
	private PageService pageService;

	@Autowired
	private MenubarController menubarController;

	private List<Page> pages;

	private Page page = new Page();

	@PostConstruct
	public void loadPages() {
		pages = pageService.findAll();
		menubarController.init();
	}

	public void save() {
		if (page.getName() == null || page.getName().trim().isEmpty()) {
			return;
		}
		pageService.save(page);
		loadPages();
		page = new Page();
	}

	public void delete(int pageId) {
		pageService.delete(pageId);
		loadPages();
		page = new Page();
	}

}
