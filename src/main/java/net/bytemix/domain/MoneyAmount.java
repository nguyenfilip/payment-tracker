package net.bytemix.domain;

import net.bytemix.MoneyParseException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;

/**
 * Immutable class representing money
 *
 * It contains logic that can be comfortably done with the data owned by this object (encapsulation)
 *
 * As opposed to industry's prevalent POJO + Service procedural programming style, this class contains quite a lot of
 * logic. In other words, its not pure DTO or "Hibernate entity style" POJO. This is
 * intentional, but obviously there is nothing conceptual that stops us from moving the logic into MoneyService.
 *
 * Created by Filip Nguyen on 31.5.17.
 */
public class MoneyAmount {
    private BigDecimal value;
    private Currency currency;

    /**
     * Putting logic that parses string into the MoneyAmount object is more of a DDD (Domain Driven Design) style
     * of programming. Depending on project's practices, it might be more appropriate to put this into
     * the MoneyService
     *
     * @param moneyAmountString
     * @throws MoneyParseException
     */
    public MoneyAmount(String moneyAmountString) throws MoneyParseException {
        if (moneyAmountString == null || moneyAmountString.isEmpty())
            throw new MoneyParseException("Money amount string cannot be empty");

        String[] lineSplit = moneyAmountString.split(" ");

        if (lineSplit.length != 2)
            throw new MoneyParseException("The expected format is 2 Strings separated by a space. Actual: " + moneyAmountString);

        try {
            currency = Currency.getInstance(lineSplit[0]);
        } catch (Exception e) {
            throw new MoneyParseException("Error when parsing currency " + lineSplit[0], e);
        }
        try {
            value = new BigDecimal(lineSplit[1]);
        } catch (Exception e) {
            throw new MoneyParseException("Error when parsing value of the money amount: "+ lineSplit[1], e);
        }
    }

    public MoneyAmount(BigDecimal value, Currency currency) {
        if (value == null || currency == null)
            throw new IllegalArgumentException("You must specify both value and currency");

        this.value = value;
        this.currency = currency;
    }

    public MoneyAmount add(MoneyAmount money) {
        if (money == null)
            throw new IllegalArgumentException("Money cannot be null");

        if (money.currency != this.currency)
            throw new IllegalArgumentException("You can add only the same currency to the same currency");

        return new MoneyAmount(value.add(money.value), currency);
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

}
