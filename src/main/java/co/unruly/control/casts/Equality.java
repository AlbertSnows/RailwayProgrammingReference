package co.unruly.control.casts;

import java.util.function.BiPredicate;

import static co.unruly.control.Piper.pipe;
import static co.unruly.control.result.Introducers.exactCastTo;
import static co.unruly.control.result.Resolvers.ifFailed;
import static co.unruly.control.result.Transformers.onSuccess;

/**
 * Equality testing interface
 */
public interface Equality {

    /**
     * @param self the object checking
     * @param other the object to be checked
     * @param equalityChecker the predicate function
     * @param <T> the types of both
     * @return whether both are equal in terms of
     * 1) exact cast
     * 2) the equality checker
     * 3) do not result in failure
     */
    @SuppressWarnings("unchecked")
    static <T> boolean areEqual(T self, Object other, BiPredicate<T, T> equalityChecker) {
        if(self==other) {
            return true;
        }

        if(other==null) {
            return false;
        }

        return pipe(other)
            .then(exactCastTo((Class<T>)self.getClass()))
            .then(onSuccess(o -> equalityChecker.test(self, o)))
            .then(ifFailed(__ -> false))
            .resolve();
    }
}
