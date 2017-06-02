package net.bytemix;

import net.bytemix.domain.MoneyAmount;
import net.bytemix.services.MoneyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Currency;
import java.util.List;

/**
 * Runnable that prints out all the registered payments.
 *
 * Created by Filip Nguyen on 31.5.17.
 */
public class PeriodicMoneyPrinter implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(PeriodicMoneyPrinter.class);
    private static MoneyService moneyService = MoneyService.getInstance();

    @Override
    public void run() {
        logger.debug("MoneyPrinter triggered");
        List<MoneyAmount> amounts = moneyService.getMoneyAmounts();

        for (MoneyAmount moneyAmount : amounts) {
            System.out.println(moneyService.getAmountStringWithExchange(moneyAmount));
        }
    }
}
