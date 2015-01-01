package net.sf.sitemonitoring.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Type;

@Getter
@Setter
@Data
@Entity
@Table(name = "monit_check_result")
public class CheckResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "check_id")
	private Check check;

	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(length = Integer.MAX_VALUE)
	private String description;

	private boolean success;

	@Column(name = "start_time", nullable = false)
	private Date startTime;

	@Column(name = "finish_time")
	private Date finishTime;

	/**
	 * Response time (in milliseconds)
	 */
	@Column(name = "response_time", nullable = false)
	private long responseTime;

}
