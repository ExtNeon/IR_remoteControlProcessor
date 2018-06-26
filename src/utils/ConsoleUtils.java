package utils;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Сборка утилит для работы с вводом - выводом из/в коммандную строку.
 */
public class ConsoleUtils {

    /**
     * Выводит пользователю пояняющее сообщение, и возвращает введённую им строку.
     *
     * @param message Сообщение, которое будет выведено.
     * @return Введённая пользователем строка
     */
    public static String getEnteredString(String message) {
        System.out.print(message);
        return new Scanner(System.in).nextLine();
    }

    /**
     * Выводит пользователю пояняющее сообщение, и возвращает введённое им целое число.
     *
     * @param message Сообщение, которое будет выведено.
     * @return Введённое пользователем число.
     */
    public static int getEnteredIntegerNumber(String message) {
        System.out.print(message);
        for (; ; ) {
            try {
                return new Scanner(System.in).nextInt();
            } catch (InputMismatchException e) {
                System.err.println("Ошибка ввода, повторите попытку...");
            }
        }
    }
}
