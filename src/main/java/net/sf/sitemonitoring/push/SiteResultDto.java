package net.sf.sitemonitoring.push;

import lombok.Data;

@Data
public class SiteResultDto {
	
	private int failedChecks;

	private int activeChecks;

}
