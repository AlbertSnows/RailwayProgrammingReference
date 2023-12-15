package co.unruly.control.result;

import org.junit.Test;

import static co.unruly.control.Piper.pipe;
import static co.unruly.control.matchers.ResultMatchers.isFailureOf;
import static co.unruly.control.matchers.ResultMatchers.isSuccessOf;
import static co.unruly.control.result.Introducers.castTo;
import static org.junit.Assert.assertTrue;

public class CastsTest {

    @Test
    public void castingToCorrectTypeYieldsSuccess() {
        final Object helloWorld = "Hello World";

        Result<String, Object> cast = pipe(helloWorld)
                .resolveWith(castTo(String.class));

        assertTrue(isSuccessOf("Hello World").matches(cast));
    }

    @Test
    public void castingToIncorrectTypeYieldsFailure() {
        final Object helloWorld = "Hello World";

        Result<Integer, Object> cast = pipe(helloWorld)
                .resolveWith(castTo(Integer.class));

        assertTrue(isFailureOf("Hello World").matches(cast));
    }
}
