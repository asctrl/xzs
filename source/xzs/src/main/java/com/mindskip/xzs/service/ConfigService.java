package com.mindskip.xzs.service;

import java.util.Map;

public interface ConfigService {
    Map<String, String> getAllConfigs();
    void updateConfigs(Map<String, String> configs);
} 