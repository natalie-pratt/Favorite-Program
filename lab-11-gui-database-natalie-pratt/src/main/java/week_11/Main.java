package week_11;

/** Launches the GUI, creates data store, connects GUI to data store.
 *
 * You do not need to modify this file */

public class Main {
    
    static TicketGUI ticketGUI;
    
    public static void main(String[] args) {
        String databaseURI = DBConfig.dbURI;
    
        TicketStore ticketStore = new TicketStore(databaseURI);
        TicketController ticketController = new TicketController(ticketStore);
        ticketGUI = new TicketGUI(ticketController);
    }
    
    
    public static void quit() {
        ticketGUI.dispose();
    }
    
}
