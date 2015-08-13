package net.sf.sitemonitoring.push;

import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;

@Data
public class CheckResultDto {

	private String result;

	private Boolean success;

	private Date startDate;

	private Date finishDate;

	private long responseTime;
	
	public String getDateInterval() {
		if (startDate == null && finishDate == null) {
			return "";
		}
		StringBuilder interval = new StringBuilder();
		if (startDate != null) {
			interval.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(startDate));
			interval.append(" - ");
		}
		if (finishDate != null) {
			if (startDate != null) {
				String startDateDay = new SimpleDateFormat("dd.MM.yyyy").format(startDate);
				String finishDateDay = new SimpleDateFormat("dd.MM.yyyy").format(finishDate);
				if(startDateDay.equals(finishDateDay)) {
					interval.append(new SimpleDateFormat("HH:mm:ss").format(finishDate));
				} else {
					interval.append(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(finishDate));
				}
			}
		}
		return interval.toString();
	}

}
