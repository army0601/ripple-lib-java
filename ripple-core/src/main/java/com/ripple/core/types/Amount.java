package com.ripple.core.types;

import com.ripple.core.fields.TypedFields;
import com.ripple.core.serialized.BinaryParser;
import com.ripple.core.serialized.BytesTree;
import com.ripple.core.fields.Field;
import com.ripple.core.serialized.SerializedType;
import com.ripple.core.serialized.TypeTranslator;
import com.ripple.core.types.uint.UInt64;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Amount extends Number implements SerializedType, Comparable<Amount>

{
    public static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;
    protected BigDecimal value; // When native the value is in `drops`

    UInt64 mantissa = null;
    /*
    TODO: Consider deleting these;
    static public UInt64 cMinValue = new UInt64("1000000000000000");
    static public UInt64 cMaxValue = new UInt64("9999999999999999");
    static public UInt64 cMaxNative = new UInt64("9000000000000000000");
    static public UInt64 cMaxNativeN = new UInt64("100000000000000000");
    */
    static public UInt64 cNotNative = new UInt64("8000000000000000", 16);
    static public UInt64 cPosNative = new UInt64("4000000000000000", 16);


    private static BigDecimal asDrops(String s) {
        return new BigDecimal(s.replace(",", "")).scaleByPowerOfTen(6);
    }

    public static final BigDecimal MAX_DROPS = asDrops("100,000,000,000.0");
    public static final BigDecimal MIN_DROPS = asDrops("0.000,001");

    private AccountID issuerAccount;

    private Currency currency;

    public Amount(BigDecimal newValue, String currency, AccountID issuer, boolean nativeSource) {
        this(newValue, Currency.fromString(currency), issuer, nativeSource);
    }

    public Amount(BigDecimal value, Currency currency, AccountID issuer, boolean nativeSource) {
        isNative = nativeSource;
        this.currency = currency;
        this.setValue(value);
        // done AFTER set value which sets some default values
        issuerAccount = issuer;
    }

    public int getOffset() {
        return offset;
    }

    private int offset;

    public String currencyString() {
        return currency.toString();
    }

    public void currency(String currency) {
        this.currency = Currency.fromString(currency);
    }

    public String issuerString() {
        if (issuerAccount == null) {
            return "";
        }
        return issuerAccount.toString();
    }

    private byte[] issuerBytes() {
        return issuerAccount.bytes();
    }

    public void issuer(String issuer) {
        if (issuer != null) {
            // blows up if issuer is shitty
            issuerAccount = AccountID.fromString(issuer);
        }
    }

    public UInt64 mantissa() {
        if (mantissa == null) {
            mantissa = calculateMantissa();
        }
        return mantissa;
    }

    /**
     *
     * @return a postive value for the mantissa
     */
    private UInt64 calculateMantissa() {
        if (isNative) {
            return new UInt64(bigIntegerDrops().abs());
        } else {
            return new UInt64(bigIntegerIOUMantissa());
        }
    }

    private BigInteger bigIntegerIOUMantissa() {
        return scaledExact(-offset).abs();
    }

    private BigInteger bigIntegerDrops() {
        return value.toBigIntegerExact();
    }

    private BigInteger scaledExact(int n) {
        return value.scaleByPowerOfTen(n).toBigIntegerExact();
    }

    public void setValue(BigDecimal value) {
        this.value = value.stripTrailingZeros();
        initialize();
    }

    private void initialize() {
        if (isNative) {
            issuerAccount = AccountID.ZERO;
            checkXRPBounds(value);
            offset = 0;
        } else {
            if (value.precision() > 16) {
                throw new PrecisionError("Overflow Error!");
            }
            issuerAccount = AccountID.ONE;
            offset = calculateOffset();
        }
    }

    int calculateOffset() {
        return -16 + value.precision() - value.scale();
    }

    public boolean isZero() {
        return value.signum() == 0;
    }

    public Amount add(Amount augend) {
        return newValue(value.add(augend.value));
    }

    public Amount subtract(Amount subtrahend) {
        return newValue(value.subtract(subtrahend.value));
    }

    public Amount multiply(Amount multiplicand) {
        return newValue(value.multiply(multiplicand.value, MATH_CONTEXT), true);
    }

    public Amount divide(Amount divisor) {
        return newValue(value.divide(divisor.value, MATH_CONTEXT), true);
    }

    private static BigDecimal round(boolean nativeSrc, BigDecimal value) {
        int i = value.precision() - value.scale();
        return value.setScale(nativeSrc ? 0 : 16 - i, MATH_CONTEXT.getRoundingMode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Amount) {
            return equals((Amount) obj);
        }
        return super.equals(obj);
    }

    public boolean equals(Amount amt) {
        return equalValue(amt) &&
                currency.equals(amt.currency) &&
                issuerAccount.equals(amt.issuerAccount);
    }

    public Amount add(Number augend) {
        return newValue(value.add(BigDecimal.valueOf(augend.longValue())));
    }

    public Amount subtract(Number subtrahend) {
        return newValue(value.subtract(BigDecimal.valueOf(subtrahend.longValue())));
    }

    public Amount multiply(Number multiplicand) {
        return newValue(value.multiply(BigDecimal.valueOf(multiplicand.longValue())), true);
    }

    public Amount divide(Number divisor) {
        return newValue(value.divide(BigDecimal.valueOf(divisor.longValue())), true);
    }

    public Amount negate() {
        return newValue(value.negate());
    }

    public Amount abs() {
        return newValue(value.abs());
    }

    public Amount min(Amount val) {
        return (compareTo(val) <= 0 ? this : val);
    }

    public Amount max(Amount val) {
        return (compareTo(val) >= 0 ? this : val);
    }

    private Amount newValue(BigDecimal newValue) {
        return newValue(newValue, false);
    }

    private Amount newValue(BigDecimal newValue, boolean round) {
        if (round) {
            newValue = round(isNative, newValue);
        }
        return new Amount(newValue, currency, issuerAccount, isNative);
    }

    public BigInteger toBigInteger() {
        return value.toBigIntegerExact();
    }

    public AccountID issuer() {
        return issuerAccount;
    }

    public boolean equalsExceptIssuer(Amount amt) {
        return equalValue(amt) &&
                currencyString().equals(amt.currencyString());
    }

    private boolean equalValue(Amount amt) {
        return compareTo(amt) == 0;
    }

    public String toTextFull() {
        if (!isNative) {
            return stringRepr();
        } else {
            return String.format("%s/XRP", valueText());
        }
    }

    @Override
    public String toString() {
        return toTextFull();
    }

    public static TypedFields.AmountField amountField(final Field f) {
        return new TypedFields.AmountField() {
            @Override
            public Field getField() {
                return f;
            }
        };
    }

    static public TypedFields.AmountField Amount = amountField(Field.Amount);
    static public TypedFields.AmountField Balance = amountField(Field.Balance);
    static public TypedFields.AmountField LimitAmount = amountField(Field.LimitAmount);
    static public TypedFields.AmountField TakerPays = amountField(Field.TakerPays);
    static public TypedFields.AmountField TakerGets = amountField(Field.TakerGets);
    static public TypedFields.AmountField LowLimit = amountField(Field.LowLimit);
    static public TypedFields.AmountField HighLimit = amountField(Field.HighLimit);
    static public TypedFields.AmountField Fee = amountField(Field.Fee);
    static public TypedFields.AmountField SendMax = amountField(Field.SendMax);
    static public TypedFields.AmountField MinimumOffer = amountField(Field.MinimumOffer);
    static public TypedFields.AmountField RippleEscrow = amountField(Field.RippleEscrow);

    public static class Translator extends TypeTranslator<Amount> {
        @Override
        public Amount fromString(String s) {
            return com.ripple.core.types.Amount.fromString(s);
        }

        @Override
        public Amount fromParser(BinaryParser parser, Integer hint) {
            BigDecimal value;
            byte[] mantissa = parser.read(8);
            byte b1 = mantissa[0], b2 = mantissa[1];

            boolean     isIOU         = (b1 & 0x80) != 0;
            boolean     isPositive    = (b1 & 0x40) != 0;
            int         sign          = isPositive ? 1 : -1;

            if (isIOU) {
                mantissa[0]      =  0;
                Currency curr    =  Currency.translate.fromParser(parser);
                AccountID issuer =  AccountID.translate.fromParser(parser);
                int offset       =  ((b1 & 0x3F) << 2) + ((b2 & 0xff) >> 6) - 97;
                mantissa[1]     &=  0x3F;

                value = new BigDecimal(new BigInteger(sign, mantissa), -offset);
                return  new Amount(value, curr, issuer, false);
            } else {
                mantissa[0] &= 0x3F;
                value = new BigDecimal(new BigInteger(sign, mantissa));
                return  new Amount(value);
            }
        }

        @Override
        public String toString(com.ripple.core.types.Amount obj) {
            return obj.stringRepr();
        }

        @Override
        public Amount fromJSONObject(JSONObject jsonObject) {
            try {
                String valueString = jsonObject.getString("value");
                String issuerString = jsonObject.getString("issuer");
                String currencyString = jsonObject.getString("currency");

                return new Amount(new BigDecimal(valueString), currencyString, issuerString);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object toJSON(Amount obj) {
            if (obj.isNative) {
                return obj.toDropsString();
            } else {
                return toJSONObject(obj);
            }
        }


        @Override
        public JSONObject toJSONObject(Amount obj) {
            try {
                JSONObject out = new JSONObject();
                out.put("currency", obj.currencyString());
                out.put("value", obj.valueText());
                out.put("issuer", obj.issuerString());
                return out;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void toBytesTree(Amount obj, BytesTree to) {
            UInt64 man = obj.mantissa();

            if (obj.isNative) {
                if (!obj.isNegative()) {
                    man = man.or(cPosNative);
                }
                to.add(man.toByteArray());
            } else {
                int offset = obj.getOffset();
                UInt64 value;

                if (obj.isZero()) {
                    value = cNotNative;
                } else if (obj.isNegative()) {
                    value = man.or(new UInt64(512 +   0 + 97 + offset).shiftLeft(64 - 10));
                } else {
                    value = man.or(new UInt64(512 + 256 + 97 + offset).shiftLeft(64 - 10));
                }

                to.add(value.toByteArray());
                to.add(obj.currency.bytes());
                to.add(obj.issuerBytes());
            }
        }
    }

    private boolean isPositive() {
        return value.signum() == 1;
    }

    public boolean isNegative() {
        return value.signum() == -1;
    }

    static public Translator translate = new Translator();

    @Override
    public int intValue() {
        return value.intValueExact();
    }

    @Override
    public long longValue() {
        return value.longValueExact();
    }

    @Override
    public float floatValue() {
        return value.floatValue();
    }

    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    // Private constructors
    private Amount(BigDecimal value, String currency, String issuer) {
        this(value, currency);
        if (issuer != null) {
            this.issuer(issuer);
        }
    }

    private Amount(BigDecimal value, String currency) {
        isNative = false;
        this.currency(Currency.normalizeIOUCode(currency));
        this.setValue(value);
    }

    public boolean isNative;

    private Amount(BigDecimal value) {
        isNative = true;
        currency = Currency.XRP_CURRENCY;
        this.setValue(value);
    }

    public static Amount fromString(String val) {
        if (val.contains("/")) {
            return fromIOUString(val);
        } else if (val.contains(".")) {
            return fromXrpString(val);
        } else {
            return fromDropString(val);
        }
    }

    @Deprecated
    private static Amount fromXrpString(String valueString) {
        BigDecimal val = new BigDecimal(valueString);

        return new Amount(val.scaleByPowerOfTen(6));
    }

    public String toDropsString() {
        if (!isNative) {
            throw new RuntimeException("Amount is not native");
        }
        return bigIntegerDrops().toString();
    }

    public String stringRepr() {
        if (isNative) {
            return toDropsString();
        } else {
            if (issuerAccount != null) {
                return String.format("%s/%s/%s", valueText(), currencyString(), issuerString());
            } else {
                return String.format("%s/%s", valueText(), currencyString());
            }
        }
    }

    /**
     * @return A String containing the value as a decimal number (in XRP scale when native)
     *
     */
    private String valueText() {
        return value.signum() == 0 ? "0" : (isNative ? value.scaleByPowerOfTen(-6) : value).toPlainString();
    }

    private static void checkLowerDropBound(BigDecimal val) {
        if (val.scale() > 0) {
            throw new RuntimeException("XRP string is log of bounds");
        }
    }

    private static IllegalArgumentException getIllegalArgumentException(BigDecimal abs, String sized, BigDecimal bound) {
        return new IllegalArgumentException(abs.toPlainString() + " is " + sized + " than bound " + bound);
    }

    private static void checkUpperBound(BigDecimal val) {
        if (val.compareTo(MAX_DROPS) == 1) {
            throw getIllegalArgumentException(val, "bigger", MAX_DROPS);
        }
    }

    /**
     * PRVIATE !! Doesn't do checking!!
     */
    public static Amount fromDropString(String val) {
        BigDecimal drops = new BigDecimal(val);
        checkDropsValueWhole(val);
        return new Amount(drops);
    }

    static void checkXRPBounds(BigDecimal value) {
        value = value.abs();
        checkLowerDropBound(value);
        checkUpperBound(value);
    }

    public static void checkDropsValueWhole(String drops) {
        boolean contains = drops.contains(".");
        if (contains) {
            throw new RuntimeException("Drops string contains floating point is decimal");
        }
    }

    private static Amount fromIOUString(String val) {
        String[] split = val.split("/");
        if (split.length == 1) {
            throw new RuntimeException("IOU string must be in the form number/currencyString or number/currencyString/issuerString");
        } else if (split.length == 2) {
            return new Amount(new BigDecimal(split[0]), split[1]);
        } else {
            return new Amount(new BigDecimal(split[0]), split[1], split[2]);
        }
    }

    public int compareTo(Amount amount) {
        return value.compareTo(amount.value);
    }

    public static class PrecisionError extends RuntimeException {
        public PrecisionError(String s) {
            super(s);
        }
    }
}
