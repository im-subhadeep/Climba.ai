package com.questiongenerator.service;

public interface AIProvider {
    String generateResponse(String prompt, double temperature) throws Exception;
}

