package net.sf.sitemonitoring.service;

import java.util.List;

import net.sf.sitemonitoring.entity.Page;
import net.sf.sitemonitoring.repository.PageRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageService {

	@Autowired
	private PageRepository pageRepository;

	public List<Page> findAll() {
		return pageRepository.findAllFetchChecks();
	}

	public void save(Page page) {
		pageRepository.save(page);
	}

	public void delete(int pageId) {
		pageRepository.deleteById(pageId);
	}

	public Page findOne(int id) {
		return pageRepository.findOne(id);
	}

}
