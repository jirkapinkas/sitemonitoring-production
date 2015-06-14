package net.sf.sitemonitoring.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.sitemonitoring.entity.Check;
import net.sf.sitemonitoring.push.ChartResultsStats;
import net.sf.sitemonitoring.push.CheckResultDto;
import net.sf.sitemonitoring.push.CheckResultDtoList;
import net.sf.sitemonitoring.service.CheckResultService;
import net.sf.sitemonitoring.service.CheckResultService.DatePeriod;
import net.sf.sitemonitoring.service.CheckService;

import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

@ManagedBean
@Data
@ViewScoped
@Slf4j
public class CheckResultsController implements Serializable {

	@PostConstruct
	public void init() {
		log.debug("constructed CheckResultController");
	}

	private static final long serialVersionUID = 1L;

	public enum ShowType {
		LAST, TABLE, CHART
	}

	/**
	 * custom cyclic reference in order to load checks in checkController from
	 * this bean
	 */
	private CheckController checkController;

	private Map<Integer, ShowType> show = new HashMap<Integer, ShowType>();

	private Map<Integer, CheckResultDtoList> tableResults = new HashMap<Integer, CheckResultDtoList>();

	private Map<Integer, LineChartModel> chartResults = new HashMap<Integer, LineChartModel>();

	private Map<Integer, ChartResultsStats> chartResultsStatsMap = new HashMap<Integer, ChartResultsStats>();

	private Map<Integer, Integer> chartOffsets = new HashMap<Integer, Integer>();

	private Check currentCheck;

	@ManagedProperty("#{checkService}")
	private CheckService checkService;

	@ManagedProperty("#{checkResultService}")
	private CheckResultService checkResultService;

	private Map<Integer, CheckResultDto> lastResults;

	private List<Check> checks;

	public void loadChecks() {
		checks = checkService.findAll();
		loadLastResults();
	}

	public void loadLastResults() {
		lastResults = checkResultService.getLastResults(checks);
	}

	public void updateResults() {
		for (Integer checkId : chartResults.keySet()) {
			loadChartResults(checkId);
		}
		for (Integer checkId : tableResults.keySet()) {
			loadTableResults(checkId);
		}
		loadLastResults();
	}

	public void loadTableResults(int checkId) {
		tableResults.put(checkId, checkResultService.getErrorTableResults(checkId));
	}

	private LineChartModel constructChartModel(DatePeriod datePeriod, LineChartSeries lineChartSeries1, LineChartSeries lineChartSeries2, int max) {
		LineChartModel lineChartModel = new LineChartModel();
		lineChartModel.setExtender("ext"); // must exist javascript function
											// ext(), which sets conf. data
		DateAxis xAxis = new DateAxis();
		xAxis.setMin(datePeriod.getDateFrom().getTime() - 10000);
		xAxis.setMax(datePeriod.getDateTo().getTime() + 10000);
		lineChartModel.getAxes().put(AxisType.X, xAxis);

		Axis yAxis = lineChartModel.getAxis(AxisType.Y);
		yAxis.setMin(0);
		yAxis.setMax(max + max / 10);

		lineChartModel.addSeries(lineChartSeries1);
		lineChartModel.addSeries(lineChartSeries2);
		return lineChartModel;
	}

	public DatePeriod getChartDatePeriod(int checkId) {
		return checkResultService.getDatePeriod(checkId, getOffset(checkId));
	}

	public void loadChartResults(int checkId) {
		DatePeriod datePeriod = getChartDatePeriod(checkId);
		CheckResultDtoList results = checkResultService.getChartResults(checkId, datePeriod);
		LineChartSeries lineChartSeries1 = new LineChartSeries();
		LineChartSeries lineChartSeries2 = new LineChartSeries();

		for (CheckResultDto checkResultDto : results.getList()) {
			if (checkResultDto.getSuccess()) {
				lineChartSeries1.set(checkResultDto.getStartDate().getTime(), checkResultDto.getResponseTime());
			} else {
				lineChartSeries1.set(checkResultDto.getStartDate().getTime(), 0);
				lineChartSeries2.set(checkResultDto.getStartDate().getTime(), 0);
			}
		}

		Integer maxMillis = checkResultService.findMaxMillis(checkId);
		if (maxMillis == null) {
			maxMillis = 0;
		}
		chartResults.put(checkId, constructChartModel(datePeriod, lineChartSeries1, lineChartSeries2, maxMillis));

		chartResultsStatsMap.put(checkId, constructStats(results.getList()));
	}

	private ChartResultsStats constructStats(List<CheckResultDto> checkResults) {
		ChartResultsStats chartResultsStats = new ChartResultsStats();
		long millisSum = 0;
		int down = 0;
		int up = 0;
		for (CheckResultDto checkResultDto : checkResults) {
			millisSum += checkResultDto.getResponseTime();
			if (checkResultDto.getSuccess()) {
				up++;
			} else {
				down++;
			}
		}
		chartResultsStats.setAverageResponseTime((int) (millisSum / checkResults.size()));
		chartResultsStats.setAverageUptime(Math.round((up * 100) / (up + down)));
		return chartResultsStats;
	}

	public void clearAllResults(int checkId) {
		tableResults.remove(checkId);
		chartResults.remove(checkId);
		chartResultsStatsMap.remove(checkId);
	}

	public void showTable(int checkId) {
		clearAllResults(checkId);
		show.put(checkId, ShowType.TABLE);
		loadTableResults(checkId);
	}

	public void showLast(int checkId) {
		show.remove(checkId);
	}

	public void showChart(int checkId) {
		clearAllResults(checkId);
		show.put(checkId, ShowType.CHART);
		loadChartResults(checkId);
	}

	public ShowType getShowType(int checkId) {
		ShowType showType = show.get(checkId);
		if (showType == null) {
			return ShowType.LAST;
		}
		return showType;
	}

	public void setCurrentCheck(int checkId) {
		currentCheck = checkService.findOne(checkId);
	}

	public void updateCheckPeriod(Check.IntervalType chartIntervalType, int chartIntervalValue) {
		checkService.updateChartInterval(currentCheck.getId(), chartIntervalType, chartIntervalValue);
		clearOffset(currentCheck.getId());
		updateResults();
		checkController.loadChecks();
	}

	public void incOffset(int checkId, int offset) {
		if (chartOffsets.containsKey(checkId)) {
			chartOffsets.put(checkId, chartOffsets.get(checkId) + offset);
		} else {
			chartOffsets.put(checkId, offset);
		}
		updateResults();
	}

	public void resetOffset(int checkId) {
		chartOffsets.remove(checkId);
		updateResults();
	}

	private int getOffset(int checkId) {
		if (chartOffsets.containsKey(checkId)) {
			return chartOffsets.get(checkId);
		}
		return 0;
	}

	private void clearOffset(int checkId) {
		chartOffsets.remove(checkId);
	}

}
