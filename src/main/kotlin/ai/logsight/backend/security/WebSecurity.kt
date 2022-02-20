package ai.logsight.backend.security

import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@EnableWebSecurity
class WebSecurity(val userDetailsService: UserDetailsServiceImpl) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable().authorizeRequests()
            .antMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/users/activate").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/users/reset_password").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/users/forgot_password").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/users/resend_activation").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            .antMatchers("/v2/api-docs/**").permitAll()
            .antMatchers("/swagger.json").permitAll()
            .antMatchers("/swagger-ui.html").permitAll()
            .antMatchers("/swagger-resources/**").permitAll()
            .antMatchers("/webjars/**", "/v3/api-docs", "/swagger-ui.html", "/swagger-ui/**").permitAll()
            .anyRequest().authenticated().and().addFilter(JWTAuthenticationFilter(authenticationManager()))
            .addFilter(JWTAuthorizationFilter(authenticationManager())).sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Throws(Exception::class)
    public override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService<UserDetailsService>(userDetailsService).passwordEncoder(passwordEncoder())
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConf = CorsConfiguration()
        corsConf.applyPermitDefaultValues()
        corsConf.addAllowedOrigin("*")
        corsConf.addAllowedHeader("*")
        corsConf.addAllowedMethod("GET")
        corsConf.addAllowedMethod("PUT")
        corsConf.addAllowedMethod("POST")
        corsConf.addAllowedMethod("DELETE")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConf)
        return source
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        return super.authenticationManagerBean()
    }
}
