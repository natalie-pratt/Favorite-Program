package week_11;

import java.sql.SQLException;
import java.util.List;

/**
 *
 * Manages interactions between GUI and Ticket Store DB
 *
 * TicketGUI will call the methods in this class.
 *
 * You do not need to modify this file.
 *
 * */

public class TicketController {
    
    TicketStore ticketStore;
    
    TicketController(TicketStore store) {
        ticketStore = store;
    }

    protected List<Ticket> loadAllOpenTicketsFromStore() {
        List<Ticket> openTickets = ticketStore.getAllOpenTickets();
        return openTickets;
    }
    
    
    protected boolean addTicket(Ticket newTicket) {
        
        try{
            ticketStore.add(newTicket);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    
    // Search the ticket store for this ticket. Returns null if ticket not found.
    protected Ticket searchById(int ticketId) {
        Ticket ticket = ticketStore.getTicketById(ticketId);
        return ticket;
    }
    
    
    protected void updateTicket(Ticket ticket) {
        ticketStore.updateTicket(ticket);
    }
    
    
    // Find and return a list of matching tickets. If nothing matches, the list will be empty.
    protected List<Ticket> searchByDescription(String searchTerm) {
        List<Ticket> matchingTickets = ticketStore.searchByDescription(searchTerm);
        return matchingTickets;
    }
    
    
    protected void quitProgram() {
        Main.quit();
    }
    
}
