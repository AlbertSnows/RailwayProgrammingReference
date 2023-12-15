package co.unruly.control;

import co.unruly.control.pair.Pair;
import co.unruly.control.result.Result;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static co.unruly.control.result.Resolvers.split;

/**
 * interface to allow us to work with results and lists
 */
public interface Lists {

    /**
     * @param results a list of result types
     * @param <S> success type
     * @param <F> fail type
     * @return a result with success as a list of the successes or a list of failures as the failures
     */
    static <S, F> Result<List<S>, List<F>> successesOrFailures(@NotNull List<Result<S, F>> results) {
        Pair<List<S>, List<F>> successesAndFailures = results.stream().collect(split());
        if(successesAndFailures.right().isEmpty()) {
            return Result.success(successesAndFailures.left());
        } else {
            return Result.failure(successesAndFailures.right());
        }
    }
}
