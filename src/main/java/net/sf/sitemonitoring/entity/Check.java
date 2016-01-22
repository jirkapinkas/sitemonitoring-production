package net.sf.sitemonitoring.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
		SITEMAP, SINGLE_PAGE, SPIDER, XML, XSD, JSON
	}

	public enum CheckCondition {
		CONTAINS, DOESNT_CONTAIN
	}

	public enum CheckState {
		NOT_RUNNING, RUNNING
	}

	public enum IntervalType {
		SECOND, MINUTE, HOUR, DAY, MONTH
	}

	public enum HttpMethod {
		GET, HEAD, POST, PUT, DELETE
	}

	@Column(name = "http_method")
	@Enumerated(EnumType.STRING)
	private HttpMethod httpMethod;

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

    @Column(nullable = true)
    private String header;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "condition_value", length = 500)
	private String condition;

	@Column(name = "text_result", length = 4000)
	private String textResult;

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

	@Column(name = "chart_max_millis")
	private Integer chartMaxMillis;

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
	
	@Column(name="follow_outbound_broken_links")
	private Boolean followOutboundBrokenLinks;

	@Column(name = "check_for_changes", nullable = false)
    private boolean checkForChanges;
	
	@Column(name = "check_for_changes_filter")
    private String checkForChangesFilter;

	// TODO Change to FetchType.LAZY
	@OneToOne(fetch = FetchType.EAGER, cascade = { CascadeType.ALL })
	@JoinColumn(name = "credentials_id")
	private Credentials credentials;

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
		type = checkType;
	}

	/**
	 * Used only in spider
	 */
	@Transient
	private String webPage;

	@Transient
	private boolean storeWebpage;

	/**
	 * Used in HTTP request, retrieved from Configuration
	 */

	@Transient
	private String userAgent;

	@Transient
	private String httpProxyServer;

	@Transient
	private Integer httpProxyPort;

	@Transient
	private String httpProxyUsername;

	@Transient
	private String httpProxyPassword;

	@ManyToOne(optional = true)
	@JoinColumn(name = "page_id")
	private Page page;

}
