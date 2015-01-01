package net.sf.sitemonitoring.push;

import java.util.List;

import lombok.Data;

@Data
public class CheckResultDtoList {

	private List<CheckResultDto> list;

	public CheckResultDtoList(List<CheckResultDto> list) {
		this.list = list;
	}

	public CheckResultDtoList() {
	}

}
