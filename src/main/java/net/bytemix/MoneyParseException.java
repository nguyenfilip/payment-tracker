package net.bytemix;

/**
 * Created by Filip Nguyen on 31.5.17.
 */
public class MoneyParseException extends Exception {

    public MoneyParseException() {

    }

    public MoneyParseException(String s) {
        super(s);
    }

    public MoneyParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public MoneyParseException(Throwable throwable) {
        super(throwable);
    }

    public MoneyParseException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
