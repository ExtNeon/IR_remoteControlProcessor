import java.util.ArrayList;

/**
 * Данный класс представляет собой контейнер для монофонической мелодии.
 *
 * @author Малякин Кирилл. 15ИТ20
 */
class MonofonicComposition {
    private ArrayList<MonofonicNote> compositionSheet = new ArrayList<>();
    private int repeatationCount = 1;
    private int maxPlayDuration = 0;

    MonofonicComposition() {
    }

    MonofonicComposition(int frequency, int duration) {
        // ArrayList<MonofonicNote> noteList = new ArrayList<>(1);
        compositionSheet.add(new MonofonicNote(frequency, duration));
    }

    /**
     * @return Массив объектов Monofonic Note в виде ArrayList.
     */
    ArrayList<MonofonicNote> getCompositionSheet() {
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
        // PM:(rep,mxdur)@3#784,100%659,100%523,100%!
        StringBuilder result = new StringBuilder("(");
        result.append(repeatationCount);
        result.append(',');
        result.append(maxPlayDuration);
        result.append(")@");
        result.append(compositionSheet.size());
        result.append('#');
        for (MonofonicNote currentNote : compositionSheet) {
            result.append(currentNote.frequency);
            result.append(',');
            result.append(currentNote.duration);
            result.append('%');
        }
        result.append('!');
        return result.toString();
    }
}
