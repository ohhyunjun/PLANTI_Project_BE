package com.metaverse.planti_be.plant.entity;

public enum stage {
    SEED("Seed"),
    GERMINATION("Germination"),
    MATURE("Mature"),
    FRUITING("Fruiting");

    private final String displayName;

    stage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}