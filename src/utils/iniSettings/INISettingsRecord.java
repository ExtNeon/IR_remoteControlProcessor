package utils.iniSettings;

/**
 * Класс представляет запись формата key = value, использующуюся в формате INI
 * Суть - контейнер с двумя полями. Позволяет менять значения key и value.
 * @author Малякин Кирилл. 15ИТ20.
 */
public class INISettingsRecord {
    private String key, value;

    public INISettingsRecord(String key, String value) {
        this.key = key;
        this.value = value;
    }

    String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return key + '=' + value;
    }
}
