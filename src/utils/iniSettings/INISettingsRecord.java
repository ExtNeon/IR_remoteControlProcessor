package utils.iniSettings;

import utils.iniSettings.exceptions.RecordParsingException;

/**
 * Класс представляет запись формата key = value, использующуюся в формате INI
 * Суть - контейнер с двумя полями. Позволяет менять значения key и value.
 *
 * @author Малякин Кирилл. 15ИТ20.
 */
public class INISettingsRecord {
    private String key, value;

    /**
     * Создаёт экземпляр записи, используя готовые значения ключа и содержимого.
     *
     * @param key   Ключ записи
     * @param value Содержимое записи
     */
    public INISettingsRecord(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Создаёт экземпляр записи, используя готовую строку в формате key=value.
     *
     * @param string Строка в формате key=value
     * @throws RecordParsingException В случае, если формат строки отличается от ожидаемого.
     */
    INISettingsRecord(String string) throws RecordParsingException {
        if (!string.contains("=")) {
            throw new RecordParsingException();
        }
        string = string.trim();
        key = string.substring(0, string.indexOf('=')).trim();
        try {
            value = string.substring(string.indexOf('=') + 1, string.length()).trim();
        } catch (IndexOutOfBoundsException e) {
            value = "";
        }
    }

    /**
     * @return Значение ключа записи
     */
    String getKey() {
        return key;
    }

    /**
     * @return Содержимое записи
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
