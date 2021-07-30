package com.bit.yourmine.config;

import com.bit.yourmine.config.auth.CustomOAuth2UserService;
import com.bit.yourmine.service.UsersService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UsersService usersService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        webSecurity.ignoring().antMatchers("/css/**", "/js/**", "/img/**", "/lib/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().ignoringAntMatchers("/readCheck")
                .and()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/myPage", "/userModify", "/passwordModify", "/profileModify"
                        , "/delProfile", "/chat/**", "/adminPage/**").authenticated()         // 로그인시 접속가능
                .antMatchers("/posts/save", "/posts/modify/*", "/posts/delete/*"
                        , "/posts/review/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')") // admin or user 권한 접속가능
                .anyRequest().permitAll()                                  // 모두 접속가능
                .and()
                .formLogin()
                .loginPage("/loginPage")
                .usernameParameter("id")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/") // 로그인시 이동페이지
                .permitAll()
                .successHandler(customSuccessHandler)
                .failureHandler(customFailureHandler)
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/") // 로그아웃시 갈 주소
                .invalidateHttpSession(true)    // 세션 초기화
                .deleteCookies("JSESSIONID", "remember-me")
                .and().exceptionHandling().accessDeniedPage("/accessDenied")    // 권한오류시 이동페이지
                .and()
                .oauth2Login().userInfoEndpoint()
                .userService(customOAuth2UserService);
        http
                .sessionManagement()    // 세션관리기능
                .maximumSessions(-1) // 동시에 사용가능한 세션 수
                .maxSessionsPreventsLogin(true) // 동시 로그인 차단
                .expiredUrl("/");    // 세션이 만료된경우
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(usersService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

}