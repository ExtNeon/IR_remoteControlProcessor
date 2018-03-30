package utils.iniSettings;

/**
 * Created by Кирилл on 27.03.2018.
 */
public class INISettingsField {
    private String key, value;

    public INISettingsField(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
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
}
