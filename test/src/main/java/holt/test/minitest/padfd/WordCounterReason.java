package holt.test.minitest.padfd;

import holt.processor.annotation.Activator;
import holt.processor.annotation.FlowThrough;
import holt.processor.annotation.Output;
import holt.processor.generation.minitest.WordCounterReasonRequirements;
import holt.test.minitest.data.CountWordsAction;
import holt.test.minitest.data.CountWordsActionPolicy;
import holt.test.minitest.data.CountedWordsPolicy;
import holt.test.minitest.data.Note;
import holt.test.minitest.data.NotePolicy;

import java.util.Map;

@FlowThrough(
        traverse = "CW",
        functionName = "CW",
        output = @Output(type = CountedWordsPolicy.class)
)
@Activator(instantiateWithReflection = true)
public class WordCounterReason implements WordCounterReasonRequirements {
    @Override
    public CountedWordsPolicy CW(Map<CountWordsAction, CountWordsActionPolicy> input0, Map<Note, NotePolicy> input1) {
        return new CountedWordsPolicy();
    }
}
