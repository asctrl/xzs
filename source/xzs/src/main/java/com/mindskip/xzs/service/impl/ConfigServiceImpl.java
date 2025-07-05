package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.domain.Config;
import com.mindskip.xzs.repository.ConfigMapper;
import com.mindskip.xzs.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigServiceImpl implements ConfigService {
    @Autowired
    private ConfigMapper configMapper;

    @Override
    public Map<String, String> getAllConfigs() {
        List<Config> configs = configMapper.selectAll();
        Map<String, String> map = new HashMap<>();
        for (Config config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    @Override
    public void updateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            configMapper.updateConfigValue(entry.getKey(), entry.getValue());
        }
    }
} 