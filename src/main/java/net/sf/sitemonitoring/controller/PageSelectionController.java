package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import lombok.Data;
import net.sf.sitemonitoring.annotation.ScopeView;
import net.sf.sitemonitoring.entity.Page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@ScopeView
public class PageSelectionController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Autowired
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
