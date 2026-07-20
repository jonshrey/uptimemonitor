package com.example.uptimemonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.uptimemonitor.model.HealthCheck;

public interface HealthCheckRepository extends JpaRepository<HealthCheck, Long> {
}
