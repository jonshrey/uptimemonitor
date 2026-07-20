package com.example.uptimemonitor.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.uptimemonitor.model.HealthCheck;
import com.example.uptimemonitor.model.Url;
import com.example.uptimemonitor.repository.HealthCheckRepository;
import com.example.uptimemonitor.repository.UrlRepository;

@Service
public class PingService {

    private final UrlRepository urlRepo;
    private final HealthCheckRepository healthRepo;
    private final HttpClient client;

    public PingService(UrlRepository urlRepo, HealthCheckRepository healthRepo) {
        this.urlRepo = urlRepo;
        this.healthRepo = healthRepo;
        
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Scheduled(fixedRate = 60000) 
    public void pingUrls() {
        List<Url> urls = urlRepo.findAll();
        for (Url url : urls) {
            performPing(url);
        }
    }

    public void performPing(Url url) {
        long start = System.currentTimeMillis();
        String status = "DOWN";
        long responseTime = -1;
        
        String address = url.getAddress();
        if (!address.startsWith("http://") && !address.startsWith("https://")) {
            address = "http://" + address;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(address))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            responseTime = System.currentTimeMillis() - start;
            
            if (response.statusCode() >= 200 && response.statusCode() < 400) {
                status = "UP";
            }
        } catch (IOException | InterruptedException e) {
            responseTime = System.currentTimeMillis() - start;
        }

        url.setStatus(status);
        url.setResponseTimeMs(responseTime);
        url.setLastChecked(LocalDateTime.now());
        urlRepo.save(url);

        HealthCheck check = new HealthCheck();
        check.setUrlAddress(url.getAddress());
        check.setStatus(status);
        check.setResponseTimeMs(responseTime);
        check.setTimestamp(LocalDateTime.now());
        healthRepo.save(check);
    }
}