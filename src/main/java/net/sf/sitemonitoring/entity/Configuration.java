package net.sf.sitemonitoring.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "monit_configuration")
public class Configuration implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int id;

	@Column(name = "conn_timeout", nullable = false)
	private int connectionTimeout;

	@Column(name = "socket_timeout", nullable = false)
	private int socketTimeout;

	@Column(name = "broken_links", nullable = false)
	private boolean checkBrokenLinks;

	@Column(name = "admin_username", nullable = false)
	private String adminUsername;

	@Column(name = "admin_password", nullable = false)
	private String adminPassword;

	@Column(name = "default_single_check_int", nullable = false)
	private int defaultSingleCheckInterval;

	@Column(name = "default_sitemap_check_int", nullable = false)
	private int defaultSitemapCheckInterval;

	@Column(name = "too_long_running_check_min", nullable = false)
	private int tooLongRunningCheckMinutes;

	/*
	 * send email functionality
	 */

	@Column(name = "send_emails", nullable = false)
	private boolean sendEmails;

	@Column(name = "admin_email")
	private String adminEmail;

	@Column(name = "email_server_host")
	private String emailServerHost;

	@Column(name = "email_server_port")
	private Integer emailServerPort;

	@Column(name = "email_server_username")
	private String emailServerUsername;

	@Column(name = "email_server_password")
	private String emailServerPassword;

	@Column(name = "email_subject")
	private String emailSubject;

	@Column(name = "email_body")
	private String emailBody;

}
