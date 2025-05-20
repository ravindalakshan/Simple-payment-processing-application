package com.ebank.paymentservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

@Service
public class CountryResolverServiceImpl implements CountryResolverService {

    private static final Logger log = LoggerFactory.getLogger(CountryResolverServiceImpl.class);
    @Override
    public void logRequestCountry() {
        try {
            HttpServletRequest request =
                    ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String ip = getClientIp(request);

            if (ip != null && !ip.isEmpty()) {
                String countryCode = resolveCountryCode(ip);
                if (countryCode != null) {
                    log.info("Request from country: {}", countryCode);
                }
            }
        } catch (Exception e) {
            log.error("Failed to resolve country", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String resolveCountryCode(String ip) throws IOException {

        URL url = URI.create("http://ip-api.com/json/" + ip + "?fields=countryCode").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            String response = scanner.useDelimiter("\\A").next();
            return response.contains("countryCode") ?
                    response.split(":")[1].replace("\"", "").replace("}", "").trim() : null;
        }
    }
}
