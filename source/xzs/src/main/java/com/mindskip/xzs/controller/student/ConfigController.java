package com.mindskip.xzs.controller.student;

import com.mindskip.xzs.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {
    @Autowired
    private ConfigService configService;

    @GetMapping("/all")
    public Map<String, String> getAllConfigs() {
        return configService.getAllConfigs();
    }

    @PostMapping("/update")
    public void updateConfig(@RequestBody Map<String, String> configs) {
        configService.updateConfigs(configs);
    }
} 