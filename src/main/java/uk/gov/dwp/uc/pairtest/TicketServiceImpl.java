package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static uk.gov.dwp.uc.pairtest.TicketServiceConstants.*;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;
    private final TicketServiceValidatorImpl validator;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService, TicketServiceValidatorImpl validator) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
        this.validator = validator;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validator.validateAccountId(accountId);

        int adults = 0;
        int children = 0;
        int infants = 0;
        int totalTickets = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            TicketTypeRequest.Type type = request.getTicketType();
            int count = request.getNoOfTickets();
            totalTickets += count;

            switch (type) {
                case ADULT:
                    adults += count;
                    break;
                case CHILD:
                    children += count;
                    break;
                case INFANT:
                    infants += count;
                    break;
            }
        }

        validator.validateTotalTickets(totalTickets);
        validator.validateChildInfantWithAdult(adults, children, infants);
        validator.validateInfants(adults, infants);

        int totalAmount = calculateTotalAmount(adults, children);
        int seatsToReserve = calculateSeatsToReserve(adults, children);

        ticketPaymentService.makePayment(accountId, totalAmount);
        seatReservationService.reserveSeat(accountId, seatsToReserve);
    }

    private int calculateTotalAmount(int adults, int children) {
        return adults * ADULT_PRICE + children * CHILD_PRICE;
    }

    private int calculateSeatsToReserve(int adults, int children) {
        return adults + children;
    }

}
