package com.onedao.additional;

import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512);
        System.out.println(java.util.Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}
