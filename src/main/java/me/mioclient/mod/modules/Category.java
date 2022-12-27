package me.mioclient.mod.modules;

public enum Category {

    COMBAT("Combat"),
    MISC("Misc"),
    RENDER("Render"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    EXPLOIT("Exploit"),
    CLIENT("Client"),
    HUD("HUD");

    /**
     * HUD Category is for future HUDEditor elements which I am trying to make.
     * - asphyxia
     */

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
