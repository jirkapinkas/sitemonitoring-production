package net.sf.sitemonitoring.push;

import java.util.Date;

import lombok.Data;

@Data
public class CheckResultDto {

	private String result;

	private Boolean success;

	private Date startDate;

	private Date finishDate;

	private long responseTime;

}
