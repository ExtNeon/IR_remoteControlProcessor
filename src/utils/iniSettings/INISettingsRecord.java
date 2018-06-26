package utils.iniSettings;

import utils.iniSettings.exceptions.RecordParsingException;

/**
 * Класс представляет запись формата key = value, использующуюся в формате INI
 * Суть - контейнер с двумя полями. Позволяет менять значения key и value.
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
    public INISettingsRecord(String string) throws RecordParsingException {
        if (!string.contains("=")) {
            throw new RecordParsingException();
        }
        string = string.trim();
        key = string.substring(0, string.indexOf('='));
        try {
            value = string.substring(string.indexOf('=') + 1, string.length());
        } catch (IndexOutOfBoundsException e) {
            value = "";
        }
    }

    /**
     * @return Значение ключа записи
     */
    public String getKey() {
        return key;
    }

    /**
     * Устанавливает ключ согласно данной переменной
     * @param key Новый ключ
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return Содержимое записи
     */
    public String getValue() {
        return value;
    }

    /**
     * Устанавливает содерджимое записи согласно новой переменной.
     * @param value Новое содержимое записи
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
