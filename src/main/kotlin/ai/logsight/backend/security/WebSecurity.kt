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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import kotlin.jvm.Throws

@EnableWebSecurity
class WebSecurity(val userDetailsService: UserDetailsServiceImpl) : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.cors().and().csrf().disable().authorizeRequests().antMatchers(HttpMethod.POST, "/api/v1/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/fast_try/**").permitAll().antMatchers(HttpMethod.POST, "/api/put/**")
            .permitAll().antMatchers(HttpMethod.POST, "/api/logs/**").permitAll().antMatchers(HttpMethod.GET, "/**")
            .permitAll().antMatchers(HttpMethod.POST, "/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/applications/**").permitAll()
            .antMatchers(HttpMethod.GET, "/api/applications/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
            .antMatchers(HttpMethod.PUT, "/api/auth/activate").permitAll()
            .antMatchers(HttpMethod.POST, "/api/v1/user/login").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/login_id").permitAll()
            .antMatchers(HttpMethod.POST, "/api/auth/login/login-link").permitAll()
            .antMatchers(HttpMethod.POST, "/api/payments/webhook**").permitAll().antMatchers("/v2/api-docs/**")
            .permitAll().antMatchers("/swagger.json").permitAll().antMatchers("/swagger-ui.html").permitAll()
            .antMatchers("/swagger-resources/**").permitAll().antMatchers("/webjars/**").permitAll().anyRequest()
            .authenticated().and().addFilter(JWTAuthenticationFilter(authenticationManager()))
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
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", CorsConfiguration().applyPermitDefaultValues())
        return source
    }

    @Bean
    @Throws(Exception::class)
    override fun authenticationManagerBean(): AuthenticationManager? {
        return super.authenticationManagerBean()
    }
}
