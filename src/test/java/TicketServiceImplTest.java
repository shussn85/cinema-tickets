import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketServiceValidatorImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.TicketServiceConstants.*;

public class TicketServiceImplTest {

    private TicketPaymentService ticketPaymentService;
    private SeatReservationService seatReservationService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    public void setUp() {
        ticketPaymentService = Mockito.mock(TicketPaymentService.class);
        seatReservationService = Mockito.mock(SeatReservationService.class);
        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService, new TicketServiceValidatorImpl());
    }

    @Test
    public void shouldPurchaseAdultTickets() {
        // Given
        Long accountId = 1L;
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3);

        // When
        ticketService.purchaseTickets(accountId, adultTicket);

        // Then
        verify(ticketPaymentService).makePayment(accountId, 75);
        verify(seatReservationService).reserveSeat(accountId, 3);
    }

    @Test
    public void shouldPurchaseAdultAndChildTickets() {
        // Given
        Long accountId = 2L;
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2);

        // When
        ticketService.purchaseTickets(accountId, adultTicket, childTicket);

        // Then
        verify(ticketPaymentService).makePayment(accountId, 80);
        verify(seatReservationService).reserveSeat(accountId, 4);
    }

    @Test
    public void shouldPurchaseAdultChildAndInfantTickets() {
        // Given
        Long accountId = 3L;
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);

        // When
        ticketService.purchaseTickets(accountId, adultTicket, childTicket, infantTicket);

        // Then
        verify(ticketPaymentService).makePayment(accountId, 65);
        verify(seatReservationService).reserveSeat(accountId, 3);
    }

    @ParameterizedTest(name = "#{index} - {0} adults, {1} children, {2} infants: £{3} and {4} seats")
    @CsvSource({
            // adultCount, childCount, infantCount, expectedAmountToPay, expectedTotalSeats
            "1, 0, 0, 25, 1",
            "2, 0, 0, 50, 2",
            "1, 1, 0, 40, 2",
            "1, 0, 1, 25, 1",
            "1, 1, 1, 40, 2",
            "2, 1, 1, 65, 3",
            "2, 3, 2, 95, 5",
            "3, 2, 0, 105, 5",
            "4, 4, 4, 160, 8"
    })
    public void shouldCalculateCorrectAmountAndSeats(int adultCount, int childCount, int infantCount,
                                                     int expectedAmountToPay, int expectedTotalSeats) {
        // Given
        Long accountId = 1L;

        TicketTypeRequest adultRequest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, adultCount);
        TicketTypeRequest childRequest = childCount > 0 ?
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, childCount) : null;
        TicketTypeRequest infantRequest = infantCount > 0 ?
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, infantCount) : null;

        // When
        if (childRequest != null && infantRequest != null) {
            ticketService.purchaseTickets(accountId, adultRequest, childRequest, infantRequest);
        } else if (childRequest != null) {
            ticketService.purchaseTickets(accountId, adultRequest, childRequest);
        } else if (infantRequest != null) {
            ticketService.purchaseTickets(accountId, adultRequest, infantRequest);
        } else {
            ticketService.purchaseTickets(accountId, adultRequest);
        }

        // Then
        verify(ticketPaymentService).makePayment(accountId, expectedAmountToPay);
        verify(seatReservationService).reserveSeat(accountId, expectedTotalSeats);
    }

    @Test
    void shouldThrowExceptionWhenInvalidAccountIDProvided() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);

        // When
        Exception exception = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(0L, adultTicket)
        );

        // Then
        assertEquals(INVALID_ACCOUNT_ID, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoAdultTicketPurchased() {
        // Given
        TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);

        // When
        Exception exception = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, childTicket)
        );

        // Then
        assertEquals(NO_ADULT_TICKET, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenInfantTicketExceedAdultTicket() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2);

        // When
        Exception exception = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, adultTicket, infantTicket)
        );

        // Then
        assertEquals(INFANT_TICKET_EXCEED_ADULT_TICKET, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTicketQuantityExceedsMaximum() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, MAX_TICKETS + 1);

        // When
        Exception exception = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, adultTicket)
        );

        // Then
        assertEquals(MAX_TICKETS_EXCEEDED, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNoTicketRequested() {
        // Given
        TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 0);

        // When
        Exception exception = assertThrows(InvalidPurchaseException.class, () ->
                ticketService.purchaseTickets(1L, adultTicket)
        );

        // Then
        assertEquals(NO_TICKETS_REQUESTED, exception.getMessage());
    }
}
