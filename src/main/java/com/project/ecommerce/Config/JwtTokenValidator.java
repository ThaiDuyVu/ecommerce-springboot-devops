package com.project.ecommerce.Config;

import com.project.ecommerce.Exceptions.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator extends OncePerRequestFilter {


    private final SecretKey key;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {


        String jwt=request.getHeader("Authorization");


        if(jwt!=null && jwt.startsWith("Bearer ")){

            jwt=jwt.substring(7);


            try {


                Claims claims =
                        Jwts.parser()
                                .verifyWith(key)
                                .build()
                                .parseSignedClaims(jwt)
                                .getPayload();



                String email =
                        String.valueOf(claims.get("email"));


                String authorities =
                        String.valueOf(
                                claims.get("authorities")
                        );


                List<GrantedAuthority> auths =
                        AuthorityUtils
                                .commaSeparatedStringToAuthorityList(
                                        authorities
                                );


                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                auths
                        );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);



            }
            catch(JwtException e){

                throw new com.project.ecommerce.Exceptions.JwtException(
                        "Invalid JWT token"
                );

            }

        }


        filterChain.doFilter(request,response);
    }

}