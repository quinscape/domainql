package de.quinscape.domainql.scalar;

import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * A byte scalar implementation.
 */
public class ByteScalar
{
    private ByteScalar()
    {
        // no instances
    }


    public static GraphQLScalarType newScalar()
    {
        return GraphQLScalarType.newScalar()
            .name("Byte")
            .description("8-bit integer")
            .coercing(new Coercing())
            .build();
    }


    public static class Coercing
        implements graphql.schema.Coercing<Byte, Byte>
    {
        private static final BigInteger BYTE_MAX = BigInteger.valueOf(Byte.MAX_VALUE);
        private static final BigInteger BYTE_MIN = BigInteger.valueOf(Byte.MIN_VALUE);

        private Byte convertImpl(Object input) {
            if (input instanceof Byte) {
                return (Byte) input;
            } else if (isNumberIsh(input)) {
                BigDecimal value;
                try {
                    value = new BigDecimal(input.toString());
                } catch (NumberFormatException e) {
                    return null;
                }
                try {
                    return value.byteValueExact();
                } catch (ArithmeticException e) {
                    return null;
                }
            } else {
                return null;
            }

        }

        @Override
        public Byte serialize(Object input) {
            Byte result = convertImpl(input);
            if (result == null) {
                throw new CoercingSerializeException(
                    "Expected type 'Byte' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Byte parseValue(Object input) {
            Byte result = convertImpl(input);
            if (result == null) {
                throw new CoercingParseValueException(
                    "Expected type 'Byte' but was '" + typeName(input) + "'."
                );
            }
            return result;
        }

        @Override
        public Byte parseLiteral(Object input) {
            if (!(input instanceof IntValue)) {
                throw new CoercingParseLiteralException(
                    "Expected AST type 'IntValue' but was '" + typeName(input) + "'."
                );
            }
            BigInteger value = ((IntValue) input).getValue();
            if (value.compareTo(BYTE_MIN) < 0 || value.compareTo(BYTE_MAX) > 0) {
                throw new CoercingParseLiteralException(
                    "Expected value to be in the Byte range but it was '" + value.toString() + "'"
                );
            }
            return value.byteValue();
        }

    }

    static boolean isNumberIsh(Object input)
    {
        return input instanceof Number || input instanceof String;
    }


    static String typeName(Object input)
    {
        if (input == null)
        {
            return "null";
        }

        return input.getClass().getSimpleName();
    }

}
