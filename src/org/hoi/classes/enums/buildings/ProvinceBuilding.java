package org.hoi.classes.enums.buildings;

public enum  ProvinceBuilding implements Building {
    NAVAL_BASE,
    BUNKER,
    COASTAL_BUNKER;

    @Override
    public int index() {
        return 11 + ordinal();
    }
}
