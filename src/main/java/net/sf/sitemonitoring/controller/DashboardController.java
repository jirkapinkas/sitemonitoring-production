package net.sf.sitemonitoring.controller;

import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import lombok.Data;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.push.CheckResultDto;
import net.sf.sitemonitoring.service.CheckResultService;
import net.sf.sitemonitoring.service.CheckService;

@ManagedBean
@ViewScoped
@Data
public class DashboardController {

	private Map<Integer, CheckResultDto> lastResults;

	private List<Check> checks;

	@ManagedProperty("#{checkResultService}")
	private CheckResultService checkResultService;

	@ManagedProperty("#{checkService}")
	private CheckService checkService;

	public void loadChecks() {
		checks = checkService.findAll();
		lastResults = checkResultService.getLastResults(checks);
	}

	public int successCount() {
		int result = 0;
		for (CheckResultDto checkResultDto : lastResults.values()) {
			if (checkResultDto.getSuccess() != null && checkResultDto.getSuccess() == true) {
				result++;
			}
		}
		return result;
	}

	public int failureCount() {
		int result = 0;
		for (CheckResultDto checkResultDto : lastResults.values()) {
			if (checkResultDto.getSuccess() != null && checkResultDto.getSuccess() == false) {
				result++;
			}
		}
		return result;
	}

}
