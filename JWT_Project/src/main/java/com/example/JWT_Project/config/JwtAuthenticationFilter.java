package com.example.JWT_Project.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

//        if (request.getServletPath().contains("/api/v1/auth")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
        //هنا جاب التوكين من هيدر الريكويست
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        //هنا ساوى بعد كلمة bearer اللي هي مكونه من 7 حروف عشان كدة خد الهيدر بس من بعد 7

        // from the next line we create the class called jwt service to get what token have and extract from it the user email
        userEmail = jwtService.extractUsername(jwt);
        //we will check if the user email is null or not and also we will is the user authenticated or not because if yes we don't need to do this again and then we will check
        //if the user is valid or not if user and token is valid so we creat an object of type usernamePasswordAuthenticationToken then we pass user details credentials and authorities as parameters
        //and then we extend this auth token with the details of our request and then we update the token
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
          if (jwtService.isTokenValid(jwt, userDetails)) {
              UsernamePasswordAuthenticationToken authToken =new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
              authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authToken);
          }

        }

        filterChain.doFilter(request, response);
    }
}