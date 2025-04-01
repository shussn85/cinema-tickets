package uk.gov.dwp.uc.pairtest;

public class TicketServiceConstants {
    public static final int MAX_TICKETS = 25;
    public static final int INFANT_PRICE = 0;
    public static final int CHILD_PRICE = 15;
    public static final int ADULT_PRICE = 25;
    public static final String INVALID_ACCOUNT_ID = "Invalid account ID";
    public static final String NO_TICKETS_REQUESTED = "No tickets requested";
    public static final String NO_ADULT_TICKET = "Child and Infant tickets require at least one Adult ticket";
    public static final String INFANT_TICKET_EXCEED_ADULT_TICKET = "Infant tickets cannot exceed Adult tickets";
    public static final String MAX_TICKETS_EXCEEDED = String.format("Cannot purchase more than %1$s tickets", MAX_TICKETS);

}
