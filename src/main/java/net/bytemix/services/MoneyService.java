package net.bytemix.services;

import net.bytemix.MoneyParseException;
import net.bytemix.domain.MoneyAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service that handles more complex logic done with MoneyAmount, especially those operations that either require
 * persistence, interaction with file system, or usually customizable behavior (e.g. formatting).
 *
 * The service also provides persistence for exchange rates and money amounts. If there would be more logic, the
 * persistence aspect would be separated into Repository or Data Access Object.
 *
 * Created by Filip Nguyen on 31.5.17.
 */
public class MoneyService {
    private static MoneyService instance;
    private static DecimalFormat formatter;
    private Logger logger = LoggerFactory.getLogger(MoneyService.class);
    /**
     * Because we use ConcurrentHashMap and there is only single writer in this application,
     * we don't need any additional synchronization (lost updates are not a danger). On top of that
     * we use immutable MoneyAmount
     *
     * Non-trivial application would use a repository pattern or data access object pattern and would
     * store data in a database.
     */
    private Map<Currency, MoneyAmount> moneyAmountByCurrency = new ConcurrentHashMap<>();
    private Map<Currency, Map<Currency, BigDecimal>> rate = new HashMap<>();


    public static synchronized MoneyService getInstance() {
        if (instance == null)
            instance = new MoneyService();

        return instance;
    }

    private MoneyService() {
        formatter = new DecimalFormat("0.##");
    }

    /**
     *
     * @param money
     * @param targetCurrency
     * @return null if there is no exchange rate to the targetCurrency
     */
    public MoneyAmount exchange(MoneyAmount money, Currency targetCurrency) {
        if (money == null || targetCurrency == null)
            throw new IllegalArgumentException("Money to be exchanged and target currency must be specified");

        if (targetCurrency.equals(money.getCurrency()))
            return money;

        //Depending on project's standards, one can throw some checked exception
        if (!rate.containsKey(money.getCurrency()) || !rate.get(money.getCurrency()).containsKey(targetCurrency))
            return null;

        return new MoneyAmount(rate.get(money.getCurrency()).get(targetCurrency).multiply(money.getValue()), targetCurrency);
    }

    public void setExchangeRate(Currency from, Currency to, BigDecimal exchangeRate) {
        if (!rate.containsKey(from))
            rate.put(from, new HashMap<Currency, BigDecimal>());

        rate.get(from).put(to, exchangeRate);
    }

    public String formatMoneyAmount(MoneyAmount moneyAmount) {
        return String.format("%s %s", moneyAmount.getCurrency().getCurrencyCode(),
                formatter.format(moneyAmount.getValue()));
    }

    /**
     * Creates a formatted String representation of the moneyAmount, including the
     * exchanged USD currency if it exists.
     * @param moneyAmount
     * @return
     */
    public String getAmountStringWithExchange(MoneyAmount moneyAmount) {
        if (!moneyAmount.getCurrency().equals(Currency.getInstance("USD"))) {
            MoneyAmount exchangedToUsd = exchange(moneyAmount, Currency.getInstance("USD"));

            if (exchangedToUsd == null)  {
                //Without exchange rate, just output the normal amount
                return formatMoneyAmount(moneyAmount);
            }

            return String.format("%s (%s)", formatMoneyAmount(moneyAmount), formatMoneyAmount(exchangedToUsd));
        } else {
            return formatMoneyAmount(moneyAmount);
        }
    }

    public void loadFromFile(Path file) {

        if (file == null)
            throw new IllegalArgumentException("File cannot be null");

        Stream<String> lines = null;
        try {
            lines = Files.lines(file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read file", e);
        }

        for (String line : lines.collect(Collectors.toList())) {
            try {
                registerPayment(new MoneyAmount(line));
            } catch (MoneyParseException mpe) {
                logger.info("Line [" + line + "] had bad format", mpe);
            }
        }

    }

    public void loadExchangeRatesFromFile(Path file) {
        if (file == null)
            throw new IllegalArgumentException("File cannot be null");

        Stream<String> lines = null;
        try {
            lines = Files.lines(file);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read file", e);
        }

        for (String line : lines.collect(Collectors.toList())) {
            try {
                saveExchangeRate(line);
            } catch (Exception ex) {
                logger.warn("Error parsing exchange rate: " + line);
            }
        }
    }

    public void saveExchangeRate(String line) {
        String[] split = line.split(",");

        if (split.length != 3)
            throw new RuntimeException("The exchange rate line should have 3 components");

        Currency c1 = Currency.getInstance(split[0].trim());
        Currency c2 = Currency.getInstance(split[1].trim());
        BigDecimal rate =  new BigDecimal(split[2].trim());

        /**
         * Set exchange rate both ways
         */
        setExchangeRate(c1, c2, rate);
        setExchangeRate(c2, c1, BigDecimal.ONE.divide(rate, 5,RoundingMode.HALF_UP));

    }

    public void registerPayment(MoneyAmount payment) {
        if (payment == null)
            throw new IllegalArgumentException("Money cannot be null");

        if (!moneyAmountByCurrency.containsKey(payment.getCurrency()))
            moneyAmountByCurrency.put(payment.getCurrency(), payment);
        else {
            MoneyAmount balance = moneyAmountByCurrency.get(payment.getCurrency());
            moneyAmountByCurrency.put(payment.getCurrency(), balance.add(payment));
        }
    }

    public List<MoneyAmount> getMoneyAmounts() {
        return new ArrayList<>(moneyAmountByCurrency.values());
    }


}

