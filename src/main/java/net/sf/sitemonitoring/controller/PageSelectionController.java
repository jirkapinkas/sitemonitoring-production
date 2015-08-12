package net.sf.sitemonitoring.controller;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import lombok.Data;
import net.sf.sitemonitoring.entity.Page;

@Data
@ManagedBean
@ViewScoped
public class PageSelectionController {

	@ManagedProperty("#{pageController}")
	private PageController pageController;

	private int selectedPage;

	public List<SelectItem> getSelectOneMenuValues() {
		List<SelectItem> result = new ArrayList<SelectItem>();
		result.add(new SelectItem(-1, "Uncategorized"));
		for (Page page : pageController.getPages()) {
			result.add(new SelectItem(page.getId(), page.getName()));
		}
		return result;
	}

}
