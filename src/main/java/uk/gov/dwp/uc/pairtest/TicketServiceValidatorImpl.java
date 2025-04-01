package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static uk.gov.dwp.uc.pairtest.TicketServiceConstants.*;

public class TicketServiceValidatorImpl {

    public void validateAccountId(long accountId) throws InvalidPurchaseException {
        if (accountId <= 0) {
            throw new InvalidPurchaseException(INVALID_ACCOUNT_ID);
        }
    }

    public void validateTotalTickets(int totalTickets) throws InvalidPurchaseException {
        if (totalTickets == 0) {
            throw new InvalidPurchaseException(NO_TICKETS_REQUESTED);
        }
        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException(MAX_TICKETS_EXCEEDED);
        }
    }

    public void validateChildInfantWithAdult(int adults, int children, int infants) throws InvalidPurchaseException {
        if ((children > 0 || infants > 0) && adults == 0) {
            throw new InvalidPurchaseException(NO_ADULT_TICKET);
        }
    }

    public void validateInfants(int adults, int infants) throws InvalidPurchaseException {
        if (infants > adults) {
            throw new InvalidPurchaseException(INFANT_TICKET_EXCEED_ADULT_TICKET);
        }
    }
}
