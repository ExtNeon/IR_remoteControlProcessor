package utils;

import java.util.Scanner;

/**
 * Created by Кирилл on 26.03.2018.
 */
public class ConsoleUtils {

    public static String getEnteredString(String message) {
        System.out.print(message);
        return new Scanner(System.in).nextLine();
    }

    public static int getEnteredIntegerNumber(String message) {
        System.out.print(message);
        return new Scanner(System.in).nextInt();
    }

    /**
     * Данный метод выводит строку в консоль, не мешая вводу пользователя (теоретически)
     *
     * @param text Текст, что будет выведен в консоль
     */
    public static void printToConsole(String text) {
        System.out.print("\r" + text + "\n_> ");
    }
}