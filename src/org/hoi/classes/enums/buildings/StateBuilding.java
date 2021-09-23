package org.hoi.classes.enums.buildings;

public enum StateBuilding implements Building {
    INFRASTRUCTURE,
    AIR_BASE,
    ANTI_AIR_BUILDING,
    RADAR_STATION,
    ARMS_FACTORY,
    INDUSTRIAL_COMPLEX,
    DOCKYARD,
    SYNTHETIC_REFINERY,
    FUEL_SILO,
    ROCKET_SITE,
    NUCLEAR_REACTOR;

    @Override
    public int index() {
        return ordinal();
    }
}
