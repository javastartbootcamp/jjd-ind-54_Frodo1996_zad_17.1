package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */
    List<Payment> findPaymentsSortedByDateAsc() {
        return findPaymentsSortedBy(Comparator.comparing(Payment::getPaymentDate));
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        return findPaymentsSortedBy(Comparator.comparing(Payment::getPaymentDate).reversed());
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */
    List<Payment> findPaymentsSortedByItemCountAsc() {
        return findPaymentsSortedBy(Comparator.comparing(payment -> (long) payment.getPaymentItems().size()));
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */

    List<Payment> findPaymentsSortedByItemCountDesc() {
        return findPaymentsSortedBy(Comparator.comparing(payment -> (long) -payment.getPaymentItems().size()));
    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return findPaymentByMonth(payment -> payment.getPaymentDate().getMonth().equals(yearMonth.getMonth()))
                .stream()
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        return findPaymentByMonth(payment -> payment.getPaymentDate().getMonth().equals(YearMonth.now().getMonth()))
                .stream()
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentDate()
                        .isAfter(dateTimeProvider.zonedDateTimeNow().minusDays(days - 1)))
                .toList();
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        return paymentRepository.findAll().stream()
                .filter(payment -> YearMonth.from(payment.getPaymentDate()).equals(dateTimeProvider.yearMonthNow()))
                .flatMap(payment -> payment.getPaymentItems().stream()
                        .map(PaymentItem::getName))
                .collect(Collectors.toSet());
    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(payment -> YearMonth.from(payment.getPaymentDate()).equals(yearMonth))
                .map(payment -> payment.getPaymentItems().stream()
                        .map(PaymentItem::getFinalPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Policz i zwróć sumę przyznanych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll().stream()
                .filter(payment -> YearMonth.from(payment.getPaymentDate()).equals(yearMonth))
                .map(payment -> payment.getPaymentItems().stream()
                        .map(PaymentItem::calculateDiscount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll().stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .flatMap(payment -> payment.getPaymentItems().stream())
                .collect(Collectors.toList());
    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll().stream()
                .filter(payment -> calculateFinalPrice(payment) > value)
                .collect(Collectors.toSet());
    }

    private double calculateFinalPrice(Payment payment) {
        return payment.getPaymentItems().stream()
                .mapToDouble(paymentItem -> paymentItem.getFinalPrice().doubleValue())
                .sum();
    }

    private List<Payment> findPaymentsSortedBy(Comparator<Payment> comparator) {
        return paymentRepository.findAll().stream()
                .sorted(comparator)
                .toList();
    }

    private List<Payment> findPaymentByMonth(Predicate<Payment> predicate) {
        return paymentRepository.findAll().stream()
                .filter(predicate)
                .toList();
    }
}