import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static class Transaction {
        final int fromId;
        final int toId;
        final int amount;

        Transaction(int fromId, int toId, int amount) {
            this.fromId = fromId;
            this.toId = toId;
            this.amount = amount;
        }
    }

    static class Account {
        private double balance;
        private final ReentrantLock lock = new ReentrantLock();

        public Account(double balance) {
            this.balance = balance;
        }

        public boolean transferTo(Account target, int amount) {
            lock.lock();
            try {
                if (balance < amount) {
                    return false; // Недостаточно средств
                }
                balance -= amount;
                target.addAmount(amount);
                return true;
            } finally {
                lock.unlock();
            }
        }

        public void addAmount(int amount) {
            lock.lock();
            try {
                balance += amount;
            } finally {
                lock.unlock();
            }
        }

        public double getBalance() {
            return balance;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Читаем количество пользователей
        System.out.println("Введите количество пользователей:");
        int n = scanner.nextInt();
        if (n <= 0) {
            System.out.println("Ошибка: количество пользователей должно быть > 0.");
            return;
        }

        // Создаем счета пользователей
        Account[] accounts = new Account[n];
        System.out.println("Введите начальные балансы пользователей:");
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account(scanner.nextDouble());
        }

        // Читаем количество транзакций
        System.out.println("Введите количество транзакций:");
        int m = scanner.nextInt();
        if (m < 0) {
            System.out.println("Ошибка: количество транзакций не может быть отрицательным.");
            return;
        }

        Transaction[] transactions = new Transaction[m];

        // Читаем транзакции
        System.out.println("Введите транзакции в формате  'user_id баланс to_user_id':");

        for (int i = 0; i < m; i++) {
            int fromId = scanner.nextInt();
            int amount = scanner.nextInt();
            int toId = scanner.nextInt();

            if (fromId < 0 || fromId >= n || toId < 0 || toId >= n) {
                System.out.println("Ошибка: некорректные ID пользователей.");
                return;
            }

            if (amount < 0) {
                System.out.println("Ошибка: сумма перевода должна быть положительной.");
                return;
            }

            transactions[i] = new Transaction(fromId, toId, amount);
        }

        // Создаем пул потоков
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Обрабатываем транзакции
        for (Transaction transaction : transactions) {
            executor.execute(() -> {
                Account fromAccount = accounts[transaction.fromId];
                Account toAccount = accounts[transaction.toId];

                if (!fromAccount.transferTo(toAccount, transaction.amount)) {
                    System.out.printf("Ошибка: недостаточно средств у пользователя %d для перевода %d на счет %d%n",
                            transaction.fromId, transaction.amount, transaction.toId);
                }
            });
        }

        // Завершаем выполнение потоков
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println("Ошибка при завершении потоков.");
        }

        // Выводим итоговые балансы
        for (int i = 0; i < n; i++) {
            System.out.printf("User %d final balance: %.2f%n", i, accounts[i].getBalance());
        }

        scanner.close();
    }
}
