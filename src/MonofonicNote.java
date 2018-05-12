/**
 * Класс - контейнер для монофонической ноты. Имеет два поля - частоту воспроизведения и длительность воспроизведения ноты.
 *
 * @author Малякин Кирилл. 15ИТ20.
 */
class MonofonicNote {
    int frequency = 0;
    int duration = 0;

    MonofonicNote(int frequency, int duration) {
        this.frequency = frequency;
        this.duration = duration;
    }
}
