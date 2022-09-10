package week_11;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TicketGUI extends JFrame {

    // You don't need to modify the form or the GUI design in TicketGUI.form
    
    protected JPanel mainPanel;
    
    // Components for adding tickets
    protected JPanel addTicketPanel;
    protected JTextField descriptionTextField;
    protected JTextField reporterTextField;
    protected JComboBox<Integer> priorityComboBox;
    protected JButton addButton;
    
    // Components for displaying ticket list
    protected JPanel ticketListPanel;
    protected JList<Ticket> ticketList;
    protected JLabel ticketListStatusDescription;
    
    // Components for searching
    protected JPanel searchPanel;
    protected JTextField descriptionSearchTextBox;
    protected JTextField idSearchTextBox;
    protected JButton searchDescriptionButton;
    protected JButton searchIdButton;
    protected JButton showAllOpenTicketsButton;
    
    // quit button and JPanel container
    protected JPanel controlsPanel;
    protected JButton quitButton;
    
    // Resolving
    protected JButton resolveSelectedButton;

    protected DefaultListModel<Ticket> ticketListModel;

    // Strings for messages that will be shown in ticketListStatusDescription
    static final String ALL_TICKETS = "Showing all open tickets";
    static final String TICKETS_MATCHING_SEARCH_DESCRIPTION = "Open tickets matching search description";
    static final String TICKET_MATCHING_ID = "Ticket matching ID";
    static final String NO_TICKETS_FOUND = "No matching tickets";
    static final String INVALID_TICKET_ID = "Invalid ticket ID";
    
    
    // A reference to the TicketController object
    // This GUI will be able to call the methods in this class to add, search for, and update tickets.
    // See example in quitProgram method.
    private TicketController controller;
    
    
    TicketGUI(TicketController controller) {


        this.controller = controller;

        /* In your code, when you need to send
        a message to the TicketProgram controller, use this controller object. So if you need
        to add a new ticket, you'll create a new Ticket object, then ask the TicketProgram controller
        to add the new Ticket to the database with
        controller.newTicket(myNewTicket);  */

        // GUI window setup and configuration
        setTitle("Support Ticket Manager");
        setContentPane(mainPanel);
        setPreferredSize(new Dimension(700, 600));
        pack();
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        for (int x = 1; x < 6; x++) { // Populate combo box

            priorityComboBox.addItem(x);

        }

        // Create new default list model for ticketList
        ticketListModel = new DefaultListModel<>();
        ticketList.setModel(ticketListModel);
        ticketList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Load all open tickets upon startup
        List<Ticket> openTickets = controller.loadAllOpenTicketsFromStore();
        ticketListStatusDescription.setText(ALL_TICKETS);

        for (Ticket ticket : openTickets) {

            ticketListModel.addElement(ticket);

        }

        // Handle action listeners
        actionListeners();

    }

    private void actionListeners() {

        // Add new ticket
        addButton.addActionListener(e -> {

            try {

                String description = descriptionTextField.getText(); // Get all user input
                int priority =  priorityComboBox.getSelectedIndex() + 1; // Get selected item (+1? Index starts at 0, combo box starts at 1)
                String reporter = reporterTextField.getText();
                Date dateReported = new Date();

                if (description.isBlank() || reporter.isBlank() || priorityComboBox.getSelectedItem() == null) {

                    showMessageDialog("Please ensure that correct data is entered."); // Make sure all data entered

                } else {

                    Ticket newTicket = new Ticket(description, priority, reporter, dateReported); // Create new ticket

                    controller.addTicket(newTicket); // Add ticket to database

                    ticketListModel.clear(); // Clear currently displayed tickets
                    List<Ticket> openTickets = controller.loadAllOpenTicketsFromStore(); // Get list of open tickets

                    ticketListStatusDescription.setText(ALL_TICKETS);

                    for (Ticket ticket : openTickets) { // Add all open tickets to JList

                        ticketListModel.addElement(ticket);

                    }

                }

            } catch(NullPointerException npe) {

                System.err.println("Error with priority comboBox - " + npe);

            }

        });

        // Get all open tickets
        showAllOpenTicketsButton.addActionListener(e -> {

            ticketListModel.clear(); // Clear all currently displayed tickets
            List<Ticket> openTickets = controller.loadAllOpenTicketsFromStore(); // Get open tickets

            ticketListStatusDescription.setText(ALL_TICKETS);

            for (Ticket ticket : openTickets) { // Add each ticket to list

                ticketListModel.addElement(ticket);

            }

        });


        searchIdButton.addActionListener(e -> {

            try { // Try/catch number format exception

                String searchByID = idSearchTextBox.getText(); // Get id to search for
                int intID = Integer.parseInt(searchByID); // Parse to get int form of string

                ticketListModel.clear();

                Ticket newTicket = controller.searchById(intID); // Call controller to search for given ID

                if (searchByID.isBlank() || intID <  1) { // If there is no id, or ID invalid, set text to constant

                    ticketListStatusDescription.setText(INVALID_TICKET_ID);

                } else if (newTicket == null) { // If there is no matching ticket, set to constant below

                    ticketListStatusDescription.setText(NO_TICKETS_FOUND);

                } else { // If there is a matching ticket, add it to JList and show constant in JLabel

                    ticketListModel.addElement(newTicket);
                    ticketListStatusDescription.setText(TICKET_MATCHING_ID);

                }

            } catch (NumberFormatException nfe) {

                ticketListModel.clear();
                ticketListStatusDescription.setText(INVALID_TICKET_ID);
                System.err.println("Error parsing int - " + nfe);

            }

        });

        // Search by description
        searchDescriptionButton.addActionListener(e -> {

            String description = descriptionSearchTextBox.getText(); // Get user input
            ticketListModel.clear();

            if (description.isBlank()) { // If there is no description entered, let user know

                ticketListStatusDescription.setText(NO_TICKETS_FOUND);

            } else { // Else if there is a description, call search method

                List<Ticket> matchingTickets = controller.searchByDescription(description);

                if (matchingTickets.isEmpty()) { // If there are no matching tickets, let user know

                    ticketListStatusDescription.setText(NO_TICKETS_FOUND);

                } else { // If there are matching tickets, let user know and display said tickets

                    ticketListStatusDescription.setText(TICKETS_MATCHING_SEARCH_DESCRIPTION);

                    for (Ticket ticket : matchingTickets) {

                        ticketListModel.addElement(ticket);

                    }

                }

            }

        });


        // Resolve tickets
        resolveSelectedButton.addActionListener(e -> {

            Ticket selectedTicket = ticketList.getSelectedValue(); // Get selected ticket

            if (selectedTicket == null) { // If no ticket selected, show dialog

                showMessageDialog("Please select a ticket to be resolved");

            } else if (selectedTicket.getStatus() == Ticket.TicketStatus.RESOLVED){ // If ticket already resolved, show dialog

                showMessageDialog("This ticket is already resolved");

            } else {

                    // Show input dialog to ask user for resolution
                    String resolution = showInputDialog("Enter resolution for ticket");

                    if (resolution != null) { // If user hits cancel, resolution will be null

                        selectedTicket.setResolution(resolution); // If user enters resolution, set new info for ticket
                        selectedTicket.setDateResolved(new Date());
                        selectedTicket.setStatus(Ticket.TicketStatus.RESOLVED);

                        controller.updateTicket(selectedTicket); // Update ticket in database

                        ticketListModel.removeElement(selectedTicket); // Remove resolved ticket from JList
                        showMessageDialog("Ticket Resolved and Deleted"); // Show confirmation

                    }

            }


        });


        quitButton.addActionListener(e -> {

            controller.quitProgram(); // Call quit method when user hits quit button

        });

    }


    // Call this method to quit the program.
    // Don't modify or delete this method.
    protected void quitProgram() {
        controller.quitProgram();    // Ask the controller to quit the program.
    }
    
    // Use this method if you need to show a message dialog displaying the message given.
    // Otherwise tests for code that shows alert dialogs will time out and fail.
    // Don't modify or delete this method.
    protected void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    
    // Use this method if you need to show input dialogs asking the given question.
    // Otherwise tests for code that shows input dialogs will time out and fail.
    // If user presses the cancel button, this method will return null.
    // Don't modify or delete this method.
    protected String showInputDialog(String question) {
        return JOptionPane.showInputDialog(this, question);
    }

}


