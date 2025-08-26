package com.metaverse.planti_be.sensor.controller;

import com.metaverse.planti_be.sensor.dto.SensorLogRequestDto;
import com.metaverse.planti_be.sensor.service.SensorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SensorLogController {
    private final SensorLogService sensorLogService;

    @PostMapping("/sensor_log")
    public void createSensorLog(@RequestBody SensorLogRequestDto sensorLogDto) {
        sensorLogService.createSensorLog(sensorLogDto);
    }
}
