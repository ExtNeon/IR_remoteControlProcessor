import java.util.ArrayList;

/**
 * Данный класс представляет собой контейнер для монофонической мелодии.
 *
 * @author Малякин Кирилл. 15ИТ20
 */
class MonophonicComposition {
    private final ArrayList<MonophonicNote> compositionSheet = new ArrayList<>();
    private int repeatationCount = 1;

    MonophonicComposition() {
    }

    MonophonicComposition(int frequency, int duration) {
        compositionSheet.add(new MonophonicNote(frequency, duration));
    }

    /**
     * @return Массив объектов Monofonic Note в виде ArrayList.
     */
    ArrayList<MonophonicNote> getCompositionSheet() {
        return compositionSheet;
    }

    void setRepeatationCount(int repeatationCount) {
        this.repeatationCount = repeatationCount;
    }


    /**
     * Генерирует "понятную" для парсера библиотеки PatternPlayer форму текущей композиции.
     * Формат: (rep_cnt,max_dur)@notes_cnt#freq_n%dur_n%!
     * rep_cnt - количество воспроизведений мелодии. По умолчанию 1.
     * max_dur - Максимальная длительность композиции
     * notes_cnt - Количество нот
     * freq_n - Частота n-ой ноты
     * dur_n - Длительность воспроизведения n-ой ноты в миллисекундах.
     *
     * @return Отформатированную согласно требованиям парсера библиотеки PatternPlayer монофоническую мелодию в виде строки.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("(");
        result.append(repeatationCount);
        result.append(',');
        int maxPlayDuration = 0;
        result.append(maxPlayDuration);
        result.append(")@");
        result.append(compositionSheet.size());
        result.append('#');
        for (MonophonicNote currentNote : compositionSheet) {
            result.append(currentNote.frequency);
            result.append(',');
            result.append(currentNote.duration);
            result.append('%');
        }
        result.append('!');
        return result.toString();
    }
}
