package com.sandymandy.pleasurehorizons.config;

public class ModConfig {
    public static ModConfig INSTANCE = new ModConfig();
    public Girls girls = new Girls();
    public Keybinds keybinds = new Keybinds();

    public static class Girls {
        public boolean disableShading = false;
    }
    public static class Keybinds {
        public boolean holdThrust = true;
    }

    public static void init() {}
}
