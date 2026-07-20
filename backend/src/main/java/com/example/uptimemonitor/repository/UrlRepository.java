package com.example.uptimemonitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.uptimemonitor.model.Url;


public interface UrlRepository extends JpaRepository<Url, Long> {}