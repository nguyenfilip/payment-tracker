package net.bytemix;


import net.bytemix.domain.MoneyAmount;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;

public class MoneyAmountTest {
    private MoneyAmount czk10;
    private MoneyAmount czk0;
    private MoneyAmount czk20;
    private MoneyAmount czk30;
    private MoneyAmount usd20;

    @Before
    public void setup() {
        czk0 = new MoneyAmount(BigDecimal.valueOf(0), Currency.getInstance("CZK"));
        czk10 = new MoneyAmount(BigDecimal.valueOf(10), Currency.getInstance("CZK"));
        czk20 = new MoneyAmount(BigDecimal.valueOf(20), Currency.getInstance("CZK"));
        czk30 = new MoneyAmount(BigDecimal.valueOf(30), Currency.getInstance("CZK"));
        usd20 = new MoneyAmount(BigDecimal.valueOf(20), Currency.getInstance("USD"));
    }

    @Test
    public void addTest() {
        Assert.assertEquals(czk20.add(czk10).getValue(), czk30.getValue());
        Assert.assertEquals(czk20.add(czk0).getValue(), czk20.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDifferentCurrencies() {
        czk20.add(usd20).getValue();
    }


    @Test()
    public void parseSuccess() throws MoneyParseException {
        MoneyAmount money = new MoneyAmount("USD 111000");
        Assert.assertEquals(money.getCurrency(), Currency.getInstance("USD"));
        Assert.assertEquals(money.getValue(), BigDecimal.valueOf(111000));
    }

    @Test()
    public void parseDecimalSuccess() throws MoneyParseException {
        MoneyAmount money = new MoneyAmount("ESP 111000.45");
        Assert.assertEquals(money.getCurrency(), Currency.getInstance("ESP"));
        Assert.assertEquals(money.getValue(), BigDecimal.valueOf(111000.45));
    }
}
