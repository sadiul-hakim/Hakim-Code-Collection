import java.text.DecimalFormat;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Author: Sadiul Hakim : https://github.com/sadiul-hakim
 * Profession: Backend Engineer
 * Date: Oct 20, 2024
 */
public final class MathExpression {
    private final double result;

    private MathExpression(Builder builder) {
        this.result = builder.number;
    }

    // Returns final result
    public double get() {
        return result;
    }

    // Return result in long
    public long toLong() {
        try {
            if (Double.isNaN(result)) {
                throw new IllegalArgumentException("Cannot convert NaN to long!");
            }
            return switch (result) {
                case Double.POSITIVE_INFINITY -> Long.MAX_VALUE;
                case Double.NEGATIVE_INFINITY -> Long.MIN_VALUE;
                case double v when v > Long.MAX_VALUE -> Long.MAX_VALUE;
                case double v when v < Long.MIN_VALUE -> Long.MIN_VALUE;
                default -> Math.round(result);
            };
        } catch (Exception ex) {
            return 0;
        }
    }

    public static class Builder {
        private final StringBuilder expression = new StringBuilder();
        private double number;
        private double sideNumber;
        private int openParenthesisIndex;
        private boolean inParenthesis;
        private double memory = 0;

        public Builder() {
            number = 0;
        }

        public Builder(double num) {
            number = num;
            expression.append(num).append(" ");
        }

        public Builder add(double num) {
            if (inParenthesis) {
                sideNumber += num;
            } else {
                number += num;
            }
            expression.append("+").append(" ").append(num).append(" ");
            return this;
        }

        // Takes a number and a condition, only does the operation if condition is true.
        public Builder addIf(double num, BiFunction<Double, Double, Boolean> condition) {
            if (!condition.apply(number, num)) {
                return this;
            }
            if (inParenthesis) {
                sideNumber += num;
            } else {
                number += num;
            }
            expression.append("+").append(" ").append(num).append(" ");
            return this;
        }

        public Builder minus(double num) {
            if (inParenthesis) {
                sideNumber -= num;
            } else {
                number -= num;
            }
            expression.append("-").append(" ").append(num).append(" ");
            return this;
        }

        // Takes a number and a condition, only does the operation if condition is true.
        public Builder minusIf(double num, BiFunction<Double, Double, Boolean> condition) {
            if (!condition.apply(number, num)) {
                return this;
            }
            if (inParenthesis) {
                sideNumber -= num;
            } else {
                number -= num;
            }
            expression.append("-").append(" ").append(num).append(" ");
            return this;
        }

        // Generates a random number and sets to NUMBER
        public Builder rand(long seed) {
            if (number != 0) {
                throw new RuntimeException("Number must be zero for random assignment!");
            }
            Random random = new Random();
            number = random.nextDouble(seed);
            expression.append(number).append(" ");
            return this;
        }

        // Takes PI value and sets to NUMBER
        public Builder pi() {
            if (number != 0) {
                throw new RuntimeException("Number must be zero for PI assignment!");
            }
            number = Math.PI;
            expression.append(number).append(" ");
            return this;
        }

        // Takes E value and sets to NUMBER
        public Builder e() {
            if (number != 0) {
                throw new RuntimeException("Number must be zero for E assignment!");
            }
            number = Math.E;
            expression.append(number).append(" ");
            return this;
        }

        public Builder randomInRange(double min, double max) {
            if (number != 0) {
                throw new RuntimeException("Number must be zero for random assignment!");
            }
            Random random = new Random();
            number = min + (max - min) * random.nextDouble();
            expression.append(number).append(" ");
            return this;
        }

        public Builder toDegrees() {
            if (inParenthesis) {
                sideNumber = Math.toDegrees(sideNumber);
            } else {
                number = Math.toDegrees(number);
            }
            return this;
        }

        public Builder max(double num) {
            if (inParenthesis) {
                sideNumber = Math.max(sideNumber, num);
            } else {
                number = Math.max(number, num);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("max( ").append(expressionSoFar).append(", ").append(num).append(" ").append(") ");
            return this;
        }

        public Builder min(double num) {
            if (inParenthesis) {
                sideNumber = Math.min(sideNumber, num);
            } else {
                number = Math.min(number, num);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("min( ").append(expressionSoFar).append(", ").append(num).append(" ").append(") ");
            return this;
        }

        public Builder multiply(double num) {
            if (inParenthesis) {
                sideNumber *= num;
            } else {
                number *= num;
            }
            expression.append("*").append(" ").append(num).append(" ");
            return this;
        }

        // Takes a number and a condition, only does the operation if condition is true.
        public Builder multiplyIf(double num, BiFunction<Double, Double, Boolean> condition) {
            if (!condition.apply(number, num)) {
                return this;
            }
            if (inParenthesis) {
                sideNumber *= num;
            } else {
                number *= num;
            }
            expression.append("*").append(" ").append(num).append(" ");
            return this;
        }

        public Builder divide(double num) {
            if (num == 0) {
                return this;
            }
            if (inParenthesis) {
                sideNumber /= num;
            } else {
                number /= num;
            }
            expression.append("/").append(" ").append(num).append(" ");
            return this;
        }

        // Takes a number and a condition, only does the operation if condition is true.
        public Builder divideIf(double num, BiFunction<Double, Double, Boolean> condition) {
            if (num == 0) {
                return this;
            }
            if (!condition.apply(number, num)) {
                return this;
            }
            if (inParenthesis) {
                sideNumber /= num;
            } else {
                number /= num;
            }
            expression.append("/").append(" ").append(num).append(" ");
            return this;
        }

        public Builder mod(double num) {
            if (inParenthesis) {
                sideNumber %= num;
            } else {
                number %= num;
            }
            expression.append("%").append(" ").append(num).append(" ");
            return this;
        }

        // Takes a number and a condition, only does the operation if condition is true.
        public Builder modIf(double num, BiFunction<Double, Double, Boolean> condition) {
            if (!condition.apply(number, num)) {
                return this;
            }
            if (inParenthesis) {
                sideNumber %= num;
            } else {
                number %= num;
            }
            expression.append("%").append(" ").append(num).append(" ");
            return this;
        }

        public Builder pow(double num) {
            if (inParenthesis) {
                sideNumber = Math.pow(sideNumber, num);
            } else {
                number = Math.pow(number, num);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("pow( ").append(expressionSoFar).append(", ").append(num).append(" ").append(") ");
            return this;
        }

        public Builder sqrt() {
            if (inParenthesis) {
                sideNumber = Math.sqrt(sideNumber);
            } else {
                number = Math.sqrt(number);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("sqrt( ").append(expressionSoFar).append(" ) ");
            return this;
        }

        public Builder round() {
            if (inParenthesis) {
                sideNumber = Math.round(sideNumber);
            } else {
                number = Math.round(number);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("round( ").append(expressionSoFar).append(" ) ");
            return this;
        }

        public Builder floor() {
            if (inParenthesis) {
                sideNumber = Math.floor(sideNumber);
            } else {
                number = Math.floor(number);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("floor( ").append(expressionSoFar).append(" ) ");
            return this;
        }

        public Builder ceil() {
            if (inParenthesis) {
                sideNumber = Math.ceil(sideNumber);
            } else {
                number = Math.ceil(number);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("ceil( ").append(expressionSoFar).append(" ) ");
            return this;
        }

        public Builder abs() {
            if (inParenthesis) {
                sideNumber = Math.abs(sideNumber);
            } else {
                number = Math.abs(number);
            }
            String expressionSoFar = expression.toString();
            expression.setLength(0);
            expression.append("abs( ").append(expressionSoFar).append(" ) ");
            return this;
        }

        public Builder cbrt() {
            if (inParenthesis) {
                sideNumber = Math.cbrt(sideNumber);
            } else {
                number = Math.cbrt(number);
            }
            return this;
        }

        public Builder log() {
            if (inParenthesis) {
                sideNumber = Math.log(sideNumber);
            } else {
                number = Math.log(number);
            }
            return this;
        }

        public Builder log10() {
            if (inParenthesis) {
                sideNumber = Math.log10(sideNumber);
            } else {
                number = Math.log10(number);
            }
            return this;
        }

        public Builder sin() {
            if (inParenthesis) {
                sideNumber = Math.sin(sideNumber);
            } else {
                number = Math.sin(number);
            }
            return this;
        }

        public Builder cos() {
            if (inParenthesis) {
                sideNumber = Math.cos(sideNumber);
            } else {
                number = Math.cos(number);
            }
            return this;
        }

        public Builder tan() {
            if (inParenthesis) {
                sideNumber = Math.tan(sideNumber);
            } else {
                number = Math.tan(number);
            }
            return this;
        }

        public Builder sinh() {
            if (inParenthesis) {
                sideNumber = Math.sinh(sideNumber);
            } else {
                number = Math.sinh(number);
            }
            return this;
        }

        public Builder cosh() {
            if (inParenthesis) {
                sideNumber = Math.cosh(sideNumber);
            } else {
                number = Math.cosh(number);
            }
            return this;
        }

        public Builder tanh() {
            if (inParenthesis) {
                sideNumber = Math.tanh(sideNumber);
            } else {
                number = Math.tanh(number);
            }
            return this;
        }

        public Builder exp() {
            if (inParenthesis) {
                sideNumber = Math.exp(sideNumber);
            } else {
                number = Math.exp(number);
            }
            return this;
        }

        public Builder toRadians() {
            if (inParenthesis) {
                sideNumber = Math.toRadians(sideNumber);
            } else {
                number = Math.toRadians(number);
            }
            return this;
        }

        // Remembers the NUMBER
        public Builder remember() {
            memory = number;
            return this;
        }

        // Recalls the NUMBER
        public Builder recall(boolean cleanMemory) {
            number = memory;
            if (cleanMemory) {
                memory = 0;
            }
            return this;
        }

        // Recalls the NUMBER on condition
        public Builder recallIf(Function<Double, Boolean> condition, boolean cleanMemory) {
            if (!condition.apply(number)) {
                return this;
            }
            number = memory;
            if (cleanMemory) {
                memory = 0;
            }
            return this;
        }

        // Replaces NUMBER with given number
        public Builder set(double num) {
            if (number != 0) {
                throw new RuntimeException("Number must be zero to set!");
            }
            number = num;
            expression.append(num).append(" ");
            return this;
        }

        // Replaces NUMBER with given number on condition
        public Builder setIf(double num, BiFunction<Double, Double, Boolean> condition) {
            if (number != 0) {
                throw new RuntimeException("Number must be zero to set!");
            }
            if (condition.apply(number, num)) {
                number = num;
            }
            expression.append(num).append(" ");
            return this;
        }

        // Prints current NUMBER
        public Builder print() {
            System.out.println("MathBuilder Result :: " + number);
            return this;
        }

        public Builder printExpression() {
            System.out.println(expression);
            return this;
        }

        public Builder openParenthesis(double num) {
            sideNumber = num;
            inParenthesis = true;
            expression.append("(").append(" ").append(num).append(" ");
            openParenthesisIndex = expression.lastIndexOf("(");
            return this;
        }

        public Builder closeParenthesisAndPlus() {
            number += sideNumber;
            inParenthesis = false;
            sideNumber = 0;
            expression.append(")").append(" ");
            expression.insert(openParenthesisIndex, "+ ");
            openParenthesisIndex = 0;
            return this;
        }

        public Builder closeParenthesisAndMinus() {
            number -= sideNumber;
            inParenthesis = false;
            sideNumber = 0;
            expression.append(")").append(" ");
            expression.insert(openParenthesisIndex, "- ");
            openParenthesisIndex = 0;
            return this;
        }

        public Builder closeParenthesisAndMultiply() {
            number *= sideNumber;
            inParenthesis = false;
            sideNumber = 0;
            expression.insert(openParenthesisIndex, "* ");
            openParenthesisIndex = 0;
            expression.append(")").append(" ");
            return this;
        }

        public Builder closeParenthesisAndDivide() {
            number /= sideNumber;
            inParenthesis = false;
            sideNumber = 0;
            expression.insert(openParenthesisIndex, "/ ");
            openParenthesisIndex = 0;
            expression.append(")").append(" ");
            return this;
        }

        public Builder format(String format) {
            DecimalFormat formater = new DecimalFormat(format);
            String num = formater.format(number);
            number = Double.parseDouble(num);
            return this;
        }

        public Builder format(int decimalPlace) {
            String pattern = "."
                    + "#".repeat(decimalPlace);
            DecimalFormat formater = new DecimalFormat(pattern);
            String num = formater.format(number);
            number = Double.parseDouble(num);
            return this;
        }

        public MathExpression build() {
            return new MathExpression(this);
        }
    }
}
