package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Traverse;
import holt.processor.generation.minitest.AbstractStats;

@Traverse(
        name = "CW",
        flowStartType = CountWordsAction.class,
        order = {"count_word_action", "notes", "number_of_words"}
)
@Activator
public class Stats extends AbstractStats {

    public Integer countAllWords() {
        return 0;
    }


}
