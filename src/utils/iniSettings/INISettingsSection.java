package utils.iniSettings;

import utils.iniSettings.exceptions.AlreadyExistsException;
import utils.iniSettings.exceptions.NotFoundException;

import java.util.ArrayList;

/**
 * Created by Кирилл on 27.03.2018.
 */
public class INISettingsSection {
    private String sectionName;
    private ArrayList<INISettingsField> fields = new ArrayList<>();

    public INISettingsSection(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public ArrayList<INISettingsField> getFields() {
        return fields;
    }

    public void addField(INISettingsField newField) throws AlreadyExistsException {
        for (INISettingsField selectedField : fields) {
            if (selectedField.getKey().equals(newField.getKey())) {
                throw new AlreadyExistsException();
            }
        }
        fields.add(newField);
    }

    public INISettingsField getFieldByKey(String key) throws NotFoundException {
        for (INISettingsField selectedField : fields) {
            if (selectedField.getKey().equals(key)) {
                return selectedField;
            }
        }
        throw new NotFoundException();
    }
}
