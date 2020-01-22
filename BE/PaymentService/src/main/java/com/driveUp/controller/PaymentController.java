package softserve.academy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import softserve.academy.converter.PayToDriverConverter;
import softserve.academy.domain.*;
import softserve.academy.dto.PayToDriver;
import softserve.academy.dto.RequestPayToDriver;
import softserve.academy.dto.ResponsePayToDriver;
import softserve.academy.repos.PaymentDetailsRepo;
import softserve.academy.service.PaymentDetailsService;
import softserve.academy.service.PaymentService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class PaymentController {

    final PaymentService paymentService;
    final PaymentDetailsService paymentDetailsService;
    final PayToDriverConverter driverConverter;
    final PaymentDetailsRepo detailsRepo;

    @PostMapping("toDrivers")
    public List<ResponsePayToDriver> payToDrivers(
            @RequestBody List<RequestPayToDriver> driversToPay) {
        List<ResponsePayToDriver> payedDrivers = new ArrayList<>();
        for (PayToDriver driver : driverConverter.convertAll(driversToPay)) {
            Transaction tempTransaction = new Transaction(new Date(System.currentTimeMillis()),
                    paymentService.getCompanyId(), driver.getDriverId(), driver.getSummary());
            PaymentDetails tempDriverDetails = detailsRepo.findByDriverIdAndIsActual(
                    driver.getDriverId(), true);
            Payment payment = new LiqPayPayment(tempTransaction, tempDriverDetails);
            paymentService.payToDriver(payment);
            payedDrivers.add(new ResponsePayToDriver(
                    tempDriverDetails.getDriverId(), payment.isSuccessful()));
        }

        return payedDrivers;
    }

    @PostMapping("liqpay/forOrder")
    public boolean payForOrder(@RequestParam double summary,
                               @RequestParam LiqpayPaymentType liqPayPaymentType,
                               @RequestParam UUID customerId) {
        Transaction transaction = new Transaction(new Date(System.currentTimeMillis()),
                customerId, paymentService.getCompanyId(),
                BigDecimal.valueOf(summary).setScale(2));
        System.out.println(transaction.getSummary());
        System.out.println(transaction.getDate());
        System.out.println(transaction.getToWhoId());
        System.out.println(transaction.getFromWhoId());
        Payment payment = new LiqPayPayment(transaction, liqPayPaymentType);
        paymentService.payOnline(payment);
        return false;
    }
}
