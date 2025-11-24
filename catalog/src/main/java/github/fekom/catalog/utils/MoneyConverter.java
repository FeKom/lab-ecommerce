package github.fekom.catalog.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public final class MoneyConverter {
    private MoneyConverter(){

    }

    public static long toCents(String price) {
        if (price == null || price.isEmpty()) {
            throw new IllegalArgumentException("Price cannot be null or empty");
        }

        if (!price.matches("^[0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{1,2}$|^[0-9]+$")) {
            throw new IllegalArgumentException("Invalid price format. Use format like 1.234,56 or 1234,56 or 5");
        }
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        format.setParseIntegerOnly(false);
        try {
            Number number = format.parse(price);
            BigDecimal tempPrice = new BigDecimal(number.toString());
            return tempPrice.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValueExact();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid price format for Brazilian locale: " + price, e);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Price has too many decimal places for conversion to cents or causes overflow: " + price, e);
        }
    }



    public static String fromCents (long priceInCents){
        NumberFormat formatter = NumberFormat.getInstance(Locale.GERMANY);
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        formatter.setGroupingUsed(true);

        double value = priceInCents / 100.0;
        return formatter.format(value);

    }
}