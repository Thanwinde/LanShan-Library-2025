package com.LanShan.Library.utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component  //处理JWT的类
public class JWTService {

    private final String jwtSigningKey = "CQUPTWYHCQUPTWYHCQUPTWYHCQUPTWYHCQUPTWYHCQUPTWYHCQUPTWYH";  // JWT签名密钥

    // 提取用户名
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 生成JWT令牌
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);  //要加东西可以在这里面加,目前只加了用户名
    }

    // 判断JWT令牌是否有效（检查用户名是否匹配且是否过期）
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);  // 从token中提取用户名
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);  // 用户名匹配且未过期则有效
    }

    // 提取JWT中的具体字段（例如用户名、过期时间等）
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);  // 提取所有声明信息
        return claimsResolvers.apply(claims);  // 根据传入的函数解析所需字段
    }

    // 生成JWT令牌
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()  // 构建JWT
                .setClaims(extraClaims)  // 设置额外的声明信息
                .setSubject(userDetails.getUsername())  // 设置主题为用户名
                .setIssuedAt(new Date(System.currentTimeMillis()))  // 设置当前时间为签发时间
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 24))  // 设置过期时间为24小时后
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 使用指定的密钥和算法签名
                .compact();  // 压缩生成JWT字符串
    }

    // 判断JWT是否过期
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());  // 比较过期时间与当前时间
    }

    // 提取JWT的过期时间
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);  // 提取过期时间
    }

    // 提取JWT中的所有声明信息（例如用户名、过期时间等）
    private Claims extractAllClaims(String token) {
        return Jwts.parser()  // 创建解析器
                .setSigningKey(getSigningKey())  // 设置签名密钥
                .build()
                .parseClaimsJws(token)  // 解析JWT
                .getBody();  // 获取JWT体中的所有声明信息
    }

    // 获取签名密钥，将字符串转为密钥
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);  // 解码Base64编码的密钥
        return Keys.hmacShaKeyFor(keyBytes);  // 使用HMAC SHA算法生成签名密钥
    }
}
