package week_11;

import javax.xml.transform.Result;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * For add, edit, search operations on the Ticket database.
 * Use try-with-resources exception handing in all the methods to ensure the
 * database connection is closed at the end of the method.
 */


public class TicketStore {
    
    private final String dbURI;
    
    TicketStore(String databaseURI) {

        this.dbURI = databaseURI;

        try(Connection connection = DriverManager.getConnection(dbURI); // Try with resources
            Statement statement = connection.createStatement()){

            // SQL statement to create ticket table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS ticket (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Autogenerate ID number
                    "description TEXT NOT NULL," +
                    "reporter TEXT NOT NULL," +
                    "priority INTEGER NOT NULL CHECK ( priority > 0 AND priority <= 5 )," + // Priority between 1-5
                    "dateReported INTEGER NOT NULL," +
                    "resolution TEXT," +
                    "dateResolved INTEGER," +
                    "status TEXT CHECK ( status == 'OPEN' OR status == 'RESOLVED' ))";

            statement.execute(createTableSQL); // Execute statement

        } catch(SQLException sqle) { // Check for exceptions

            System.err.println("Error creating table because of " + sqle);

        }

    }
    

    // Get all open tickets
    public List<Ticket> getAllOpenTickets() {

        String selectAllSQL = "SELECT * FROM ticket WHERE status = 'OPEN' ORDER BY priority ASC"; // SQL statement to get open tickets

        try (Connection connection = DriverManager.getConnection(dbURI);
            Statement statement = connection.createStatement()) {

            List<Ticket> tickets = new ArrayList<>(); // New list to store open tickets

            ResultSet resultSet = statement.executeQuery(selectAllSQL);

            while (resultSet.next()) { // Get all information

                String description = resultSet.getString("description");
                int priority = resultSet.getInt("priority");
                String reporter = resultSet.getString("reporter");
                long dateRep = resultSet.getLong("dateReported");
                int id = resultSet.getInt("id");
                String resolution = resultSet.getString("resolution");
                String status = resultSet.getString("status");

                Date dateReported = new Date(dateRep);

                if (resultSet.getLong("dateResolved") == 0) { // Check long resolvedDate not null

                    Ticket openTicket = new Ticket(description, priority, reporter, dateReported, id, null, resolution, Ticket.TicketStatus.valueOf(status));

                    tickets.add(openTicket); // If it's null, set date resolved as null and add to list

                } else {

                    long dateRes = resultSet.getLong("dateResolved");

                    Date dateResolved = new Date(dateRes); // Create date for resolved

                    Ticket openTicket = new Ticket(description, priority, reporter, dateReported, id, dateResolved, resolution, Ticket.TicketStatus.valueOf(status));

                    tickets.add(openTicket); // Add open ticket to list

                }




            }

            return tickets; // Return open tickets

        } catch (SQLException sqlException) { // Check for exceptions

            System.err.println("Error retrieving all items because " + sqlException);

            return null;

        }

    }

    
    /** Add ticket to the database. */
    public void add(Ticket newTicket) throws SQLException {

        String insertSQL = "INSERT INTO ticket " + // Add with prepared statement
                "(description, priority, reporter, dateReported, dateResolved, resolution, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(dbURI);
            PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            // Set all variables with prepared statement
            preparedStatement.setString(1, newTicket.getDescription());
            preparedStatement.setInt(2, newTicket.getPriority());
            preparedStatement.setString(3, newTicket.getReporter());
            preparedStatement.setLong(4, newTicket.getDateReported().getTime());
            if (newTicket.getDateResolved() != null) { // Check that long type isn't null
                preparedStatement.setLong(5, newTicket.getDateResolved().getTime());
            } else {
                preparedStatement.setNull(5, Types.INTEGER);
            }
            preparedStatement.setString(6, newTicket.getResolution());
            preparedStatement.setString(7, newTicket.getStatus().name());

            preparedStatement.executeUpdate();

            ResultSet keys = preparedStatement.getGeneratedKeys(); // Get id for ticket
            keys.next();
            int id = keys.getInt(1);
            newTicket.setTicketID(id);

        } catch(SQLException SQLE) {

            System.err.println("Error adding ticket to database because of " + SQLE);
            throw SQLE;

        }

    }


    public Ticket getTicketById(int id) {

        String getTicketIDSQL = "SELECT * FROM ticket WHERE id = (?) ORDER BY priority ASC"; // Get ID where user input equals ?

        try (Connection connection = DriverManager.getConnection(dbURI);
            PreparedStatement preparedStatement = connection.prepareStatement(getTicketIDSQL)) {

            preparedStatement.setInt(1, id); // Set ID variable as user input
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet == null) { // If there aren't any matching tickets, return null

                return null;

            } else { // Else get all information from given ticket

                    String description = resultSet.getString("description");
                    int priority = resultSet.getInt("priority");
                    String reporter = resultSet.getString("reporter");
                    Date dateReported = resultSet.getDate("dateReported");
                    int ticketID = resultSet.getInt("id");
                    Date dateResolved = resultSet.getDate("dateResolved");
                    String resolution = resultSet.getString("resolution");
                    String status = resultSet.getString("status");

                return new Ticket(description, priority, reporter, dateReported, // Return matching ticket
                        ticketID, dateResolved, resolution, Ticket.TicketStatus.valueOf(status));

            }

        } catch (SQLException e) {

            System.err.println("Error searching for ticket because of " + e);
            return null;

        }

    }
    
    
    public void updateTicket(Ticket ticket) { // Update ticket with new information

        String updateSQL = "UPDATE ticket SET description = ?," + // Update with variables
                "priority = ?, reporter = ?, dateReported = ?, " +
                "dateResolved = ?, resolution = ?, status = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(dbURI);
            PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {

            preparedStatement.setString(1, ticket.getDescription());
            preparedStatement.setInt(2, ticket.getPriority());
            preparedStatement.setString(3, ticket.getReporter());
            preparedStatement.setLong(4, ticket.getDateReported().getTime());
            if (ticket.getDateResolved() != null) { // Check not null
                preparedStatement.setLong(5, ticket.getDateResolved().getTime());
            } else {
                preparedStatement.setNull(5, Types.INTEGER);
            }
            preparedStatement.setString(6, ticket.getResolution());
            preparedStatement.setString(7, "RESOLVED"); // Set as resolved
            preparedStatement.setInt(8, ticket.getTicketID());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            System.err.println("Error updating ticket - " + e);

        }

    }
    
    
    public List<Ticket> searchByDescription(String ticketDescription) {

        List<Ticket> matchingTickets = new ArrayList<>(); // Matching ticket list

        // Statement to compare user input with descriptions regardless of case, ordered by priority
        String selectSQL = "SELECT * FROM ticket WHERE UPPER(description) LIKE UPPER(?) ORDER BY priority ASC";

        try (Connection connection = DriverManager.getConnection(dbURI);
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL)) {

            if (ticketDescription == null || ticketDescription.isBlank()) { // If there is no description from user, return empty list

                return Collections.emptyList();

            } else {

                preparedStatement.setString(1, "%" + ticketDescription + "%"); // Check for strings containing
                                                                                                        // search string
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) { // Get all information if there is a matching tickdet

                    String description = resultSet.getString("description");
                    int priority = resultSet.getInt("priority");
                    String reporter = resultSet.getString("reporter");
                    Date dateReported = resultSet.getDate("dateReported");
                    int ticketID = resultSet.getInt("id");
                    Date dateResolved = resultSet.getDate("dateResolved");
                    String resolution = resultSet.getString("resolution");
                    String status = resultSet.getString("status");

                    Ticket newTicket = new Ticket(description, priority, reporter, dateReported, // Create matching ticket
                            ticketID, dateResolved, resolution, Ticket.TicketStatus.valueOf(status));

                    matchingTickets.add(newTicket); // Add matching ticket

                }

                return matchingTickets; // Return list of tickets

            }

        } catch (SQLException e) {

            System.err.println("Error searching by description because of " + e);
            return Collections.emptyList();

        }

    }

}
