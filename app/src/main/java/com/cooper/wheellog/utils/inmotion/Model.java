package com.cooper.wheellog.utils.inmotion;

import timber.log.Timber;

public enum Model {
    V11(6, "Inmotion V11"),
    V12(7, "Inmotion V12"),
    V13(8, "Inmotion V13"),
    UNKNOWN(0,"Inmotion Unknown");


    private final int value;
    private final String name;

    Model(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }
    public String getName() {
        return name;
    }

    public static Model findById(int id) {
        Timber.i("Model %d", id);
        for (Model m : Model.values()) {
            if (m.getValue() == id) return m;
        }
        return Model.UNKNOWN;
    }
}