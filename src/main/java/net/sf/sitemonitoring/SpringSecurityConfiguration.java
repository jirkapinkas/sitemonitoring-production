package net.sf.sitemonitoring;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableWebSecurity
@Configuration
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, DataSource dataSource) throws Exception {
		// auth.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN");
		auth.jdbcAuthentication()
				.dataSource(dataSource)
				.usersByUsernameQuery("select admin_username, admin_password, 1 from monit_configuration where admin_username = ?")
				.authoritiesByUsernameQuery("select admin_username, 'ROLE_ADMIN' from monit_configuration where admin_username = ?")
				.passwordEncoder(new BCryptPasswordEncoder());
	}

	protected void configure(HttpSecurity http) throws Exception {
		http
				.csrf()
				.disable()
				.logout()
				.logoutUrl("/logout")
				.and()
				.authorizeRequests()
				.antMatchers("/admin/**")
				.authenticated()
				.and()
				.formLogin()
				.loginPage("/index.xhtml")
				.loginProcessingUrl("/login")
				.usernameParameter("username")
				.passwordParameter("password")
				.defaultSuccessUrl("/admin/dashboard.xhtml")
				.and()
				.httpBasic();
	}
}
