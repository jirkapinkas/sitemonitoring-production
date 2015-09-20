package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import net.sf.sitemonitoring.annotation.ScopeView;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.push.CheckResultDto;
import net.sf.sitemonitoring.service.CheckResultService;
import net.sf.sitemonitoring.service.CheckService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
@ScopeView
public class DashboardController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Data
	public class CheckAndCheckResultDto {
		private Check check;
		private CheckResultDto checkResultDto;

		public CheckAndCheckResultDto(Check check, CheckResultDto checkResultDto) {
			this.check = check;
			this.checkResultDto = checkResultDto;
		}
	}

	private Map<Integer, CheckResultDto> lastResults;

	private List<Check> checks;

	/**
	 * contains only erroneous results
	 */
	private Map<Integer, CheckAndCheckResultDto> checksWithResults;

	@Autowired
	private CheckResultService checkResultService;

	@Autowired
	private CheckService checkService;

	public void loadChecks() {
		checks = checkService.findAll();
		checksWithResults = new HashMap<Integer, CheckAndCheckResultDto>();
		lastResults = checkResultService.getLastResults(checks);
		for (Check check : checks) {
			CheckResultDto lastResult = lastResults.get(check.getId());
			if (lastResult != null && lastResult.getSuccess() != null && lastResult.getSuccess() == false) {
				checksWithResults.put(check.getId(), new CheckAndCheckResultDto(check, lastResult));
			}
		}
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

	public int inactiveCount() {
		int result = 0;
		for (Check check : checks) {
			if (!check.isActive()) {
				result++;
			}
		}
		return result;
	}

}
