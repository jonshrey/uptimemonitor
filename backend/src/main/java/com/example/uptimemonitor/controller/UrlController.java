package com.example.uptimemonitor.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.uptimemonitor.model.Url;
import com.example.uptimemonitor.repository.UrlRepository;
import com.example.uptimemonitor.service.PingService;

@RestController
@RequestMapping("/api/urls")
@CrossOrigin(origins = "*") // Keeps the frontend connection happy
public class UrlController {

    private final UrlRepository urlRepo;
    private final PingService pingService;

    public UrlController(UrlRepository urlRepo, PingService pingService) {
        this.urlRepo = urlRepo;
        this.pingService = pingService;
    }

    @GetMapping
    public ResponseEntity<List<Url>> getAll() {
        List<Url> urls = urlRepo.findAll();
        return ResponseEntity.ok(urls);
    }

    @PostMapping
    public ResponseEntity<Url> addUrl(@RequestBody Map<String, String> payload) {
        String address = payload.get("address");

        if (address == null || address.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Url url = new Url();
        url.setAddress(address.trim());
        url.setStatus("PENDING");

        Url savedUrl = urlRepo.save(url);

        pingService.performPing(savedUrl);

        Url finalUrl = urlRepo.findById(savedUrl.getId()).orElseThrow();

        URI location = URI.create("/api/urls/" + finalUrl.getId());
        return ResponseEntity.created(location).body(finalUrl);
    }
}