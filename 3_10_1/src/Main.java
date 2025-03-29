import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.lang.Math;

public class Main {

    public static void main(String[] args) {
        double a = 3.0;
        double b = 4.0;
        double c = 10.0;
        double d = 16.0;

        CompletableFuture<Double> sumOfSquares = CompletableFuture.supplyAsync(() -> {
            sleep(5000);
            double result = Math.pow(a, 2) + Math.pow(b, 2);
            System.out.println("Сумма квадратов: " + result);
            return result;
        });

        CompletableFuture<Double> logarithm = CompletableFuture.supplyAsync(() -> {
            sleep(15000);
            if (c <= 0) {
                System.out.println("Ошибка: логарифм определён только для положительных чисел.");
                return Double.NaN;
            }
            double result = Math.log(c);
            System.out.println("Натуральный логарифм: " + result);
            return result;
        });

        CompletableFuture<Double> sqrtResult = CompletableFuture.supplyAsync(() -> {
            sleep(10000);
            if (d < 0) {
                System.out.println("Ошибка: квадратный корень определён только для неотрицательных чисел.");
                return Double.NaN;
            }
            double result = Math.sqrt(d);
            System.out.println("Квадратный корень: " + result);
            return result;
        });

        CompletableFuture<Double> finalResult = sumOfSquares
                .thenCombine(logarithm, (sum, log) -> {
                    if (Double.isNaN(sum) || Double.isNaN(log)) {
                        System.out.println("Ошибка: невозможно выполнить вычисления из-за некорректных значений.");
                        return Double.NaN;
                    }
                    return sum * log;
                })
                .thenCombine(sqrtResult, (tempResult, sqrt) -> {
                    if (Double.isNaN(tempResult) || Double.isNaN(sqrt)) {
                        System.out.println("Ошибка: невозможно выполнить вычисления из-за некорректных значений.");
                        return Double.NaN;
                    }
                    return tempResult / sqrt;
                });

        try {
            double result = finalResult.get();
            if (!Double.isNaN(result)) {
                System.out.println("Результат формулы: " + result);
            } else {
                System.out.println("Результат не может быть вычислен из-за ошибок.");
            }
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Ошибка выполнения.");
        }
    }

    private static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
