package com.metaverse.planti_be.plant.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Stage {
    SEED("Seed"),
    GERMINATION("Germination"),
    MATURE("Mature"),
    FRUITING("Fruiting");

    private final String displayName;

}