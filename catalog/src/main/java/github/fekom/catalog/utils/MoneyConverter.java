package github.fekom.catalog.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyConverter {
    private MoneyConverter(){

    }


    public static long toCents(String priceString) {
        if (priceString == null || priceString.isBlank()) {
            throw new IllegalArgumentException("price string cannot be null or empty");
        }

        long parsedPriceInCents;
        try {
            String cleanedPrice = priceString.replace(",", "."); // Padroniza para ponto decimal

            BigDecimal tempPrice = new BigDecimal(cleanedPrice)
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP); // Arredonda para o inteiro mais próximo

            parsedPriceInCents = tempPrice.longValueExact(); // Garante que não há casas decimais após multiplicação

            if (parsedPriceInCents < 0) {
                throw new IllegalArgumentException("Price must be non-negative.");
            }
            return parsedPriceInCents;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format: " + priceString, e);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Price has too many decimal places for conversion to cents: " + priceString, e);
        }
    }

    public static String fromCents (long priceInCents){
        return String.format("%.2f", priceInCents / 100.0);
    }
}