package uhc.tensuraUHC.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import uhc.tensuraUHC.TensuraUHC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HelpOpManager {

    private final TensuraUHC main;
    public HelpOpManager(TensuraUHC main)
    {
        this.main = main;
    }

    private int nextTicketId = 1;
    // Associe l'ID du ticket à l'UUID de l'émetteur
    private final Map<Integer, UUID> ticketSenders = new HashMap<>();

    public int createTicket(Player sender) {
        int id = nextTicketId++;
        ticketSenders.put(id, sender.getUniqueId());
        return id;
    }

    public UUID getSenderUUID(int ticketId) {
        return ticketSenders.get(ticketId);
    }

    public void removeTicket(int ticketId) {
        ticketSenders.remove(ticketId);
    }
}