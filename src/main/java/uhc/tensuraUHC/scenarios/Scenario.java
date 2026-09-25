package uhc.tensuraUHC.scenarios;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import uhc.tensuraUHC.TensuraUHC;

public abstract class Scenario implements Listener {

    protected final TensuraUHC main;
    private final String name;
    private final Material icon;
    private final String description;
    private boolean enabled;

    public Scenario(TensuraUHC main, String name, Material icon, String description) {
        this.main = main;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.enabled = false;
    }

    public String getName() { return name; }
    public Material getIcon() { return icon; }
    public String getDescription() { return description; }
    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }
}