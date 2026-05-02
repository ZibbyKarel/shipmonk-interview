package com.shipmonk.testingday.provider.fixer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FixerErrorDetail(int code, String info) {}
