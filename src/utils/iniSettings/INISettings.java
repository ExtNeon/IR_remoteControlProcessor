package utils.iniSettings;

import utils.iniSettings.exceptions.AlreadyExistsException;
import utils.iniSettings.exceptions.NotFoundException;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Created by Кирилл on 27.03.2018.
 */
public class INISettings {
    private ArrayList<INISettingsSection> sections = new ArrayList<>();

    public INISettings() {
    }

    public void addSection(INISettingsSection newSection) throws AlreadyExistsException {
        for (INISettingsSection selectedSection : sections) {
            if (selectedSection.getSectionName().equals(newSection.getSectionName())) {
                throw new AlreadyExistsException();
            }
        }
        sections.add(newSection);
    }

    public void updateSection(INISettingsSection section) {
        try {
            sections.set(sections.indexOf(getSectionByName(section.getSectionName())), section);
        } catch (NotFoundException e) {
            sections.add(section);
        }
    }

    public INISettingsSection getSectionByName(String sectionName) throws NotFoundException {
        for (INISettingsSection selectedSection : sections) {
            if (selectedSection.getSectionName().equals(sectionName)) {
                return selectedSection;
            }
        }
        throw new NotFoundException();
    }

    public ArrayList<INISettingsSection> getSections() {
        return sections;
    }

    public void saveToFile(String fileName) throws IOException {
        try (BufferedWriter textFile = new BufferedWriter(new FileWriter(fileName))) {
            String iniFileText = this.toString();
            textFile.write(iniFileText);
            textFile.flush();
        }
    }

    public void loadFromFile(String fileName) throws IOException {
        try (BufferedReader textFile = new BufferedReader(new FileReader(fileName))) {
            String readedText = textFile.lines().collect(Collectors.joining("\n"));
            importFromText(readedText);
        }
    }

    // TODO: 27.03.2018 Сделай его менее ужасным
    public void importFromText(String text) {
        ArrayList<String> lines = splitTextIntoLines(text);
        sections = new ArrayList<>();
        while (lines.size() > 0) {
            int startIndex = -1, lastIndex = lines.size();
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).startsWith("[")) {
                    if (startIndex > 0) {
                        lastIndex = i;
                        break;
                    } else {
                        startIndex = i;
                    }
                }
            }
            if (startIndex < 0) {
                break;
            }
            sections.add(new INISettingsSection(lines.get(startIndex).substring(1, lines.get(startIndex).length() - 1).trim()));
            for (int i = startIndex + 1; i < lastIndex; i++) {
                try {
                    if (lines.get(i).contains("=")) {
                        sections.get(sections.size() - 1).addField(new INISettingsField(lines.get(i).split("=")[0].trim(), lines.get(i).split("=")[1].trim()));
                    }
                } catch (AlreadyExistsException e) {
                    e.printStackTrace();
                }
            }
            for (int i = startIndex; i < lastIndex; i++) {
                lines.remove(startIndex);
            }
        }
    }

    private ArrayList<String> splitTextIntoLines(String text) {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder builder = new StringBuilder(text);
        while (builder.indexOf("\n") != -1) {
            lines.add(builder.substring(0, builder.indexOf("\n")));
            builder.delete(0, builder.indexOf("\n") + 1);
        }
        lines.add(builder.toString());
        return lines;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (INISettingsSection currentSection : sections) {
            builder.append('[');
            builder.append(currentSection.getSectionName());
            builder.append("]\n");
            for (INISettingsField currentField : currentSection.getFields()) {
                builder.append(currentField.getKey());
                builder.append('=');
                builder.append(currentField.getValue());
                builder.append('\n');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
