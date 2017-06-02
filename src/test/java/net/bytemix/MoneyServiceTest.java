package net.bytemix;


import net.bytemix.domain.MoneyAmount;
import net.bytemix.services.MoneyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public class MoneyServiceTest {
    private MoneyService service = MoneyService.getInstance();
    private Currency czk = Currency.getInstance("CZK");
    private Currency usd = Currency.getInstance("USD");
    private Currency gbp = Currency.getInstance("GBP");
    private MoneyAmount usd20;
    private MoneyAmount czk151_25;
    private MoneyAmount czk22;
    private MoneyAmount usd1;
    private MoneyAmount gbp1;

    @Before
    public void setup() {
       service.saveExchangeRate("USD, GBP, 10.12");
       service.saveExchangeRate("USD, CZK, 25");
       usd20 = new MoneyAmount(BigDecimal.valueOf(20), Currency.getInstance("USD"));
       usd1 = new MoneyAmount(BigDecimal.valueOf(1), Currency.getInstance("USD"));
       czk151_25 = new MoneyAmount(BigDecimal.valueOf(151.25), Currency.getInstance("CZK"));
       czk22 = new MoneyAmount(BigDecimal.valueOf(22), Currency.getInstance("CZK"));
       gbp1 = new MoneyAmount(BigDecimal.valueOf(1), Currency.getInstance("GBP"));
    }

    @Test
    public void exchangeReverseCzkUsd() {
        String result = service.getAmountStringWithExchange(czk22);
        Assert.assertEquals("CZK 22 (USD 0.88)", result) ;
    }

    @Test
    public void exchangeTest() {
        MoneyAmount exchanged = service.exchange(usd20, czk);
        Assert.assertEquals(BigDecimal.valueOf(500), exchanged.getValue()) ;
        Assert.assertEquals(czk, exchanged.getCurrency());
    }

    @Test
    public void bothWaysExchangeTest() {
        MoneyAmount exchanged = service.exchange(gbp1, usd);
        Assert.assertEquals(BigDecimal.valueOf(0.09881), exchanged.getValue()) ;
        Assert.assertEquals(usd, exchanged.getCurrency());
    }

    @Test
    public void exchangeStringFormattingTest() {
        Assert.assertEquals("CZK 151.25 (USD 6.05)", service.getAmountStringWithExchange(czk151_25));
        Assert.assertEquals("USD 20", service.getAmountStringWithExchange(usd20));
    }

    @Test
    public void exchangeRateSaveTest() {
        MoneyAmount exchanged = service.exchange(usd1, gbp);
        Assert.assertEquals(BigDecimal.valueOf(10.12), exchanged.getValue()) ;
        Assert.assertEquals(gbp, exchanged.getCurrency());
    }

    @Test
    public void sameExchangeTest() {
        MoneyAmount exchanged = service.exchange(czk151_25, czk);
        Assert.assertEquals(BigDecimal.valueOf(151.25), exchanged.getValue()) ;
        Assert.assertEquals(czk, exchanged.getCurrency());
    }

    @Test
    public void registerPaymentTest() throws MoneyParseException {
        Assert.assertEquals(0, service.getMoneyAmounts().size());

        service.registerPayment(new MoneyAmount("USD -20.2"));
        List<MoneyAmount> amounts = service.getMoneyAmounts();
        Assert.assertEquals(1, amounts.size());
        MoneyAmount usdAmount = amounts.get(0);
        Assert.assertEquals(usd, usdAmount.getCurrency());
        Assert.assertEquals(BigDecimal.valueOf(-20.2), usdAmount.getValue());
        service.registerPayment(new MoneyAmount("USD +1.2"));
        Assert.assertEquals(BigDecimal.valueOf(-19.0), service.getMoneyAmounts().get(0).getValue());
    }
}
