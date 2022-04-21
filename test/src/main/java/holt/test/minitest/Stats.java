package holt.test.minitest;

import holt.processor.annotation.Activator;
import holt.processor.annotation.Output;
import holt.processor.annotation.Traverse;
import holt.processor.generation.minitest.AbstractStats;

@Traverse(
        name = "CW",
        startTypes = {
                @Output(type = CountWordsAction.class),
                @Output(type = CountWordsActionPolicy.class)
        },
        order = {"count_word_action", "notes", "number_of_words"}
)
@Activator
public class Stats extends AbstractStats {

    public Integer countAllWords() {
        return 0;
    }


}
