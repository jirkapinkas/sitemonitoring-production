package net.sf.sitemonitoring.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.entity.CheckResult;
import net.sf.sitemonitoring.push.CheckResultDto;
import net.sf.sitemonitoring.push.CheckResultDtoList;
import net.sf.sitemonitoring.repository.CheckRepository;
import net.sf.sitemonitoring.repository.CheckResultRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckResultService {

	@Data
	public final static class DatePeriod {

		private Date dateFrom;

		private Date dateTo;

		public DatePeriod(Date dateFrom, Date dateTo) {
			this.dateFrom = dateFrom;
			this.dateTo = dateTo;
		}

	}

	@Autowired
	private CheckResultRepository checkResultRepository;

	@Autowired
	private CheckRepository checkRepository;
	
	private static final int HALF_MONTH_IN_DAYS = 15;
	
	private static final int HALF_DAY_IN_HOURS = 12;
	
	private static final int HALF_HOUR_IN_MINUTES = 30;

	/**
	 * Based on offset calculate date period shown in chart
	 * @param checkId
	 * @param offset
	 * @return
	 */
	public DatePeriod getDatePeriod(int checkId, int offset) {
		Check check = checkRepository.findOne(checkId);
		Calendar calendarFrom = new GregorianCalendar();
		Calendar calendarTo = new GregorianCalendar();
		// TODO make nicer and reduce period to 1/3 or 1/4 instead of 1/2
		switch (check.getChartPeriodType()) {
		case MONTH:
			calendarFrom.add(Calendar.MONTH, -check.getChartPeriodValue() + (offset / 2) * check.getChartPeriodValue());
			calendarTo.add(Calendar.MONTH, +((offset / 2) * check.getChartPeriodValue()));
			if (offset % 2 != 0) {
				if (offset > 0) {
					calendarFrom.add(Calendar.DATE, HALF_MONTH_IN_DAYS * check.getChartPeriodValue());
					calendarTo.add(Calendar.DATE, HALF_MONTH_IN_DAYS * check.getChartPeriodValue());
				} else {
					calendarFrom.add(Calendar.DATE, - HALF_MONTH_IN_DAYS * check.getChartPeriodValue());
					calendarTo.add(Calendar.DATE, - HALF_MONTH_IN_DAYS * check.getChartPeriodValue());
				}
			}
			break;
		case DAY:
			calendarFrom.add(Calendar.DATE, -check.getChartPeriodValue() + (offset / 2) * check.getChartPeriodValue());
			calendarTo.add(Calendar.DATE, +((offset / 2) * check.getChartPeriodValue()));
			if (offset % 2 != 0) {
				if (offset > 0) {
					calendarFrom.add(Calendar.HOUR_OF_DAY, HALF_DAY_IN_HOURS * check.getChartPeriodValue());
					calendarTo.add(Calendar.HOUR_OF_DAY, HALF_DAY_IN_HOURS * check.getChartPeriodValue());
				} else {
					calendarFrom.add(Calendar.HOUR_OF_DAY, - HALF_DAY_IN_HOURS * check.getChartPeriodValue());
					calendarTo.add(Calendar.HOUR_OF_DAY, - HALF_DAY_IN_HOURS * check.getChartPeriodValue());
				}
			}
			break;
		case HOUR:
			calendarFrom.add(Calendar.HOUR_OF_DAY, -check.getChartPeriodValue() + (offset / 2) * check.getChartPeriodValue());
			calendarTo.add(Calendar.HOUR_OF_DAY, +((offset / 2) * check.getChartPeriodValue()));
			if (offset % 2 != 0) {
				if (offset > 0) {
					calendarFrom.add(Calendar.MINUTE, HALF_HOUR_IN_MINUTES * check.getChartPeriodValue());
					calendarTo.add(Calendar.MINUTE, HALF_HOUR_IN_MINUTES * check.getChartPeriodValue());
				} else {
					calendarFrom.add(Calendar.MINUTE, - HALF_HOUR_IN_MINUTES * check.getChartPeriodValue());
					calendarTo.add(Calendar.MINUTE, - HALF_HOUR_IN_MINUTES * check.getChartPeriodValue());
				}
			}
			break;
		default:
			throw new UnsupportedOperationException("unknown chart interval type");
		}
		return new DatePeriod(calendarFrom.getTime(), calendarTo.getTime());
	}
	
	public CheckResultDtoList getErrorTableResults(int checkId) {
		List<CheckResult> checkResultList = checkResultRepository.findByCheckIdErrors(checkId);
		ArrayList<CheckResultDto> list = new ArrayList<CheckResultDto>();
		for (CheckResult checkResult : checkResultList) {
			list.add(transformCheckToDto(checkResult));
		}
		CheckResultDtoList checkResultDtoList = new CheckResultDtoList(list);
		return checkResultDtoList;
	}

	public CheckResultDtoList getChartResults(int checkId, DatePeriod datePeriod) {
		List<CheckResult> checkResultList = checkResultRepository.findByCheckIdDateRange(checkId, datePeriod.getDateFrom(), datePeriod.getDateTo());
		ArrayList<CheckResultDto> list = new ArrayList<CheckResultDto>();
		for (CheckResult checkResult : checkResultList) {
			list.add(transformCheckToDto(checkResult));
		}

		// if there's no result, send dummy data, otherwise client-side
		// component won't work
		if (list.isEmpty()) {
			CheckResultDto checkResultDto = new CheckResultDto();
			checkResultDto.setStartDate(new Date());
			checkResultDto.setSuccess(false);
			list.add(checkResultDto);
		}

		CheckResultDtoList checkResultDtoList = new CheckResultDtoList(list);
		return checkResultDtoList;
	}

	public Map<Integer, CheckResultDto> getLastResults(List<Check> checks) {
		Map<Integer, CheckResultDto> map = new HashMap<Integer, CheckResultDto>();
		for (Check check : checks) {
			// skip inactive checks
			if (!check.isActive()) {
				continue;
			}
			List<CheckResult> checkResultList = checkResultRepository.findByCheck(check, new PageRequest(0, 1, new Sort(Direction.DESC, "id")));
			CheckResultDto checkResultDto = new CheckResultDto();
			if (checkResultList.size() == 0) {
				// first time, no results yet
			} else {
				checkResultDto = transformCheckToDto(checkResultList.get(0));
			}
			map.put(check.getId(), checkResultDto);
		}
		return map;
	}
	
	private CheckResultDto transformCheckToDto(CheckResult checkResult) {
		CheckResultDto checkResultDto = new CheckResultDto();
		checkResultDto.setResult(checkResult.getDescription());
		checkResultDto.setSuccess(checkResult.isSuccess());
		checkResultDto.setStartDate(checkResult.getStartTime());
		checkResultDto.setFinishDate(checkResult.getFinishTime());
		checkResultDto.setResponseTime(checkResult.getResponseTime());
		return checkResultDto;
	}

	@Transactional
	public void deleteOld(int checkId, Date date) {
		checkResultRepository.deleteOld(checkId, date);
	}

	public int findMaxMillis(int checkId) {
		return checkResultRepository.findMaxMillis(checkId);
	}

}
