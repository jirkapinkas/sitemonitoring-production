package net.sf.sitemonitoring.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@Entity
@Table(name = "monit_check")
public class Check implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	public enum CheckType {
		SITEMAP, SINGLE_PAGE, SPIDER
	}

	public enum CheckCondition {
		CONTAINS, DOESNT_CONTAIN
	}

	public enum CheckState {
		NOT_RUNNING, RUNNING
	}

	public enum IntervalType {
		MINUTE, HOUR, DAY, MONTH
	}

	@Column(name = "check_state", nullable = false, updatable = false)
	@Enumerated(EnumType.STRING)
	private CheckState checkState;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private CheckType type;

	@Column(nullable = false, name = "condition_type")
	@Enumerated(EnumType.STRING)
	private CheckCondition conditionType;

	@Column(nullable = false, length = 500)
	@Size(min = 1, message = "Site name cannot be empty!")
	@URL
	private String url;

	@Min(value = 0, message = "Incorrect HTTP code!")
	@Column(name = "return_http_code")
	private int returnHttpCode;

	@Size(min = 1, message = "Name cannot be empty!")
	private String name;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "condition_value", length = 500)
	private String condition;

	@Column(name = "excluded_urls", length = 1000)
	private String excludedUrls;

	@Column(name = "do_not_follow_urls", length = 1000)
	private String doNotFollowUrls;

	@OneToMany(mappedBy = "check", cascade = { CascadeType.REMOVE })
	@OrderBy("startTime desc")
	private List<CheckResult> checkResults;

	@Column(name = "check_start_date", updatable = false)
	private Date startDate;

	@Column(name = "check_end_date", updatable = false)
	private Date endDate;

	@Column(name = "scheduled_next_date", updatable = false)
	private Date scheduledNextDate;

	@Column(name = "scheduled_start_date", nullable = false)
	private Date scheduledStartDate;

	@Column(name = "scheduled_interval", nullable = false)
	private int scheduledInterval;

	@Column(name = "scheduled_interval_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private IntervalType scheduledIntervalType;

	@Column(name = "connection_timeout", nullable = false)
	private int connectionTimeout;

	@Column(name = "socket_timeout", nullable = false)
	private int socketTimeout;

	@Column(name = "check_broken_links", nullable = false)
	private boolean checkBrokenLinks;

	@Column(name = "chart_period_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private IntervalType chartPeriodType;

	@Column(name = "chart_period_value", nullable = false)
	private int chartPeriodValue;

	@Column(name = "keep_results_type", nullable = false)
	private IntervalType keepResultType;

	@Column(name = "keep_results_value", nullable = false)
	private int keepResultsValue;

	/*
	 * follows sending emails functionality
	 */

	@Column(name = "max_error_count", nullable = false)
	private int maxErrorCount;

	@Column(name = "send_emails", nullable = false)
	private boolean sendEmails;

	@Column(name = "current_error_count", nullable = false, updatable = false)
	private int currentErrorCount;

	@Column(name = "last_sent_email", updatable = false)
	private Date lastSentEmail;

	public Check() {
		active = true;
		returnHttpCode = HttpServletResponse.SC_OK;
		checkState = CheckState.NOT_RUNNING;
		keepResultType = IntervalType.DAY;
		keepResultsValue = 1;
		currentErrorCount = 0;
		maxErrorCount = 0;
	}

	public Check(CheckType checkType) {
		this();
		switch (checkType) {
		case SINGLE_PAGE:
			type = CheckType.SINGLE_PAGE;
			break;

		case SITEMAP:
			type = CheckType.SITEMAP;
			break;

		case SPIDER:
			type = CheckType.SPIDER;
			break;
		}
	}

	/**
	 * Used only in spider
	 */
	@Transient
	private String webPage;
	
	@Transient
	private boolean storeWebpage;
	
}
