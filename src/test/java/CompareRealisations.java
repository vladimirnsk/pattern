import org.example.pool.ThreadPool;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CompareRealisations {

    /*
     * Операция создания потока операционной системы достаточно дорогое, и по этому плохо подходит для короткоживущих задач.
     * Потоки пула подойдут, если у нас легковесные и короткоживущие треды, которые не проводят много сложных вычислений.
     * Если мы порождаем пачку потоков, значение которых сильно превосходит количество ядер процессора из-за чего
     * планировщик будет вынужден постоянно переключать контекст (особенно, если потоки не связаны с операциями ввода-вывода)
     */

    class SimpleNumberFounder implements Runnable {
        private List<Boolean> matrix = new ArrayList<>();  // Булев массив для хранения простоты чисел
        private int n;  // Верхняя граница диапазона чисел

        public SimpleNumberFounder(int n) {
            this.n = n;
        }

        @Override
        public void run() {
            // инициализация булевого массива
            for (int i = 0; i < n; i++) {
                matrix.add(true);
            }
            matrix.set(2, true);

            // Алгоритм "Решето Эратосфена" для поиска простых чисел
            for (int i = 2; i < n; i++) {
                if (matrix.get(i)) {
                    for (int j = i + i; j < n; j += i) {
                        matrix.set(j, false);
                    }
                }
            }

            // Фильтрация и подсчет простых чисел
            matrix.stream()
                    .filter(i -> i)
                    .count();
        }
    }

    @Test
    void compare() throws InterruptedException {
        int N_THREADS = 1000;  // Количество создаваемых потоков
        int LIST_SIZE = 90000;  // Размер списка чисел для каждого потока
        List<Thread> threads = new ArrayList<>();  // Список для хранения потоков

        // Создание и запуск обычных потоков
        for (int i = 0; i < N_THREADS; i++) {
            threads.add(new Thread(new SimpleNumberFounder(LIST_SIZE)));
        }

        Instant start = Instant.now();  // Замер времени начала выполнения потоков
        for (var x : threads) {
            x.start();  // Запуск потока
        }

        for (var i : threads) {
            i.join();  // Ожидание завершения потоков
        }
        Instant finish = Instant.now();  // Замер времени завершения выполнения потоков
        long timeElapsed = Duration.between(start, finish).toMillis();  // Вычисление времени выполнения

        // Чтобы избежать ошибки, связанной с переполнением кучи
        threads.clear();  // Очистка списка потоков

        List<Runnable> interfaces = new ArrayList<>();  // Список задач для пула потоков

        // Создание задач для пула потоков
        for (int i = 0; i < N_THREADS; i++) {
            interfaces.add(new SimpleNumberFounder(LIST_SIZE));
        }

        var pool = new ThreadPool(Runtime.getRuntime().availableProcessors());  // Создание пула потоков
        Instant startPool = Instant.now();  // Замер времени начала выполнения пула потоков
        interfaces.forEach(pool::execute);  // Выполнение задач в пуле
        pool.joinPool();  // Ожидание завершения всех задач в пуле
        Instant finishPool = Instant.now();  // Замер времени завершения выполнения пула потоков

        // Вывод результатов сравнения времени выполнения обычных потоков и пула потоков
        System.out.println("elapsed time in threads (ms): " + timeElapsed);
        System.out.println("elapsed time in thread pool(ms): " + Duration.between(startPool, finishPool).toMillis());
    }
}
