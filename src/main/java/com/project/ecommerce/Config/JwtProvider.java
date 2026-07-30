package com.project.ecommerce.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class JwtProvider {

    private final SecretKey key;

    public String generateToken(Authentication auth){

        Collection<? extends GrantedAuthority> authorities =
                auth.getAuthorities();

        String roles = populateAuthorities(authorities);


        return Jwts.builder()
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()+86400000)
                )
                .claim("email", auth.getName())
                .claim("authorities", roles)
                .signWith(key)
                .compact();
    }

    public String getEmailFromJwtToken(String jwt) {

        try {

            if(jwt == null || !jwt.startsWith("Bearer ")) {
                throw new com.project.ecommerce.Exceptions.JwtException(
                        "Missing or invalid JWT token"
                );
            }


            jwt = jwt.substring(7);


            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
            return claims.get("email", String.class);
        } catch (ExpiredJwtException e) {
            throw new com.project.ecommerce.Exceptions.JwtException(
                    "JWT token expired"
            );
        } catch (MalformedJwtException e) {
            throw new com.project.ecommerce.Exceptions.JwtException(
                    "Invalid JWT token format"
            );
        } catch (SignatureException e) {

            throw new com.project.ecommerce.Exceptions.JwtException(
                    "Invalid JWT signature"
            );
        }
        catch (UnsupportedJwtException e) {

            throw new com.project.ecommerce.Exceptions.JwtException(
                    "Unsupported JWT token"
            );
        } catch (IllegalArgumentException e) {

            throw new com.project.ecommerce.Exceptions.JwtException(
                    "JWT token is empty"
            );
        }
        catch (io.jsonwebtoken.JwtException e){

            throw new com.project.ecommerce.Exceptions.JwtException(
                    "Invalid JWT token"
            );
        }
    }

    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> auths = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            auths.add(authority.getAuthority());
        }
        return String.join(",", auths);
    }
}