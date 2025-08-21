package com.metaverse.planti_be.plant.service;

import com.metaverse.planti_be.plant.repository.PlantRepository;
import org.springframework.stereotype.Service;

@Service
public class PlantService {
    private final PlantRepository plantRepository;

    public PlantService(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }
}
