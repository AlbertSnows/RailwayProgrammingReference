package co.unruly.control.matchers;

import co.unruly.control.result.Result;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Hamcrest matchers for Result types
 */
public class ResultMatchers {

    /**
     * Matches if the received value is a Success containing the specified value
     * @param expectedValue success value to test against
     * @param <S> success
     * @param <F> failure
     * @return matcher testing if input is a success containing expected value
     */
    @Contract("_ -> new")
    public static <S, F> @NotNull Matcher<Result<S, F>>
    isSuccessOf(S expectedValue) {
        return isSuccessThat(equalTo(expectedValue));
    }

    /**
     * Matches if the received value is a Success matching the specified value
     * @param expectedSuccess success to test against
     * @param <S> success
     * @param <F> failure
     * @return matcher testing if input matches success value
     */
    @Contract("_ -> new")
    public static <S, F> @NotNull Matcher<Result<S, F>>
    isSuccessThat(Matcher<S> expectedSuccess) {
        return new SuccessMatcher<>(expectedSuccess);
    }

    /**
     * Matches if the received value is a Failure containing the specified value
     * @param expectedValue value to test failure against
     * @param <S> success
     * @param <F> failure
     * @return matcher that matches input against failure value
     */
    @Contract("_ -> new")
    public static <S, F> @NotNull Matcher<Result<S, F>>
    isFailureOf(F expectedValue) {
        return isFailureThat(equalTo(expectedValue));
    }

    /**
     * Matches if the received value is a Failure matching the specified value
     * @param expectedFailure failure to test against
     * @param <S> success
     * @param <F> failure
     * @return a matcher for the expected failure
     */
    @Contract("_ -> new")
    public static <S, F> @NotNull Matcher<Result<S, F>>
    isFailureThat(Matcher<F> expectedFailure) {
        return new FailureMatcher<>(expectedFailure);
    }

    static <S, F> void describeTo(@NotNull Result<S, F> result, Description description) {
        result.either(
            success -> description.appendText("A Success containing " + success),
            failure -> description.appendText("A Failure containing " + failure)
        );
    }
}
