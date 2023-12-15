package co.unruly.control.result;

import org.junit.Test;

import java.util.Optional;
import java.util.function.Function;

import static co.unruly.control.result.Match.match;
import static co.unruly.control.result.Match.matchValue;
import static co.unruly.control.result.Recover.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertTrue;

public class MatchTest {

    @Test
    public void canMatchOnTypeWithFlowTyping() {
        Function<A, String> matchByType = match(
                ifType(B.class, B::messageForB),
                ifType(C.class, C::messageForC)
        ).otherwise(A::message);
        var bMatch = matchByType.apply(new B("Ketchup"));
        assertTrue(is("Cheese").matches(matchByType.apply(new A("Cheese"))));
        assertTrue(is("I'm a B and I say Ketchup").matches(bMatch));
        assertTrue(is("I'm a C and I say Pickles").matches(matchByType.apply(new C("Pickles"))));
    }

    @Test
    public void canMatchOnValue() {
        Function<Integer, String> matchByType = match(
                ifEquals(4, x -> x + " sure looks like a 4 to me!"),
                ifEquals(7, x -> x + " looks like one of them gosh-darned 7s?")
        ).otherwise(x -> "I have no idea what a " + x + " is though...");
        var nineMatch = matchByType.apply(7);
        assertTrue(is("I have no idea what a 3 is though...").matches(matchByType.apply(3)));
        assertTrue(is("4 sure looks like a 4 to me!").matches(matchByType.apply(4)));
        assertTrue(is("I have no idea what a 6 is though...").matches(matchByType.apply(6)));
        assertTrue(is("7 looks like one of them gosh-darned 7s?").matches(nineMatch));
    }

    @Test
    public void canMatchOnTest() {
        Function<Integer, String> matchByType = match(
                ifIs((Integer x) -> x % 2 == 0, x -> x + ", well, that's one of those even numbers"),
                ifIs(x -> x < 0, x -> x + " is one of those banker's negative number thingies")
        ).otherwise(x -> x + " is a regular, god-fearing number for god-fearing folks");

        assertTrue(is("2, well, that's one of those even numbers").matches(matchByType.apply(2)));
        assertTrue(is("-6, well, that's one of those even numbers").matches(matchByType.apply(-6)));
        assertTrue(is("3 is a regular, god-fearing number for god-fearing folks").matches(matchByType.apply(3)));
        assertTrue(is("-9 is one of those banker's negative number thingies").matches(matchByType.apply(-9)));
    }

    @Test
    public void canMatchOnTestPassingArgument() {
        String matchByResult = matchValue(4,
                ifIs((Integer x) -> x % 2 == 0, x -> x + ", well, that's one of those even numbers"),
                ifIs(x -> x < 0, x -> x + " is one of those banker's negative number thingies")
        ).otherwise(x -> x + " is a regular, god-fearing number for god-fearing folks");

        assertTrue(is("4, well, that's one of those even numbers").matches(matchByResult));
    }

    @Test
    public void canOperateOverAListOfOptionalProviders() {
        String cheese = matchValue(new Things(null, "Cheese!", "Bacon!"),
                ifPresent(Things::a),
                ifPresent(Things::b),
                ifPresent(Things::c)
        ).otherwise(__ -> "Ketchup!");

        assertTrue(is("Cheese!").matches(cheese));
    }


    @Test
    public void usesDefaultIfNoOptionalProvidersProvideAValue() {
        String cheese = matchValue(new Things(null, null, null),
                ifPresent(Things::a),
                ifPresent(Things::b),
                ifPresent(Things::c)
        ).otherwise(__ -> "Ketchup!");

        assertTrue(is("Ketchup!").matches(cheese));
    }

    @Test
    public void useMatchToCalculateFactorial() {
        assertTrue(is(1).matches(factorial(0)));
        assertTrue(is(1).matches(factorial(1)));
        assertTrue(is(720).matches(factorial(6)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void factorialOfNegativeNumberThrowsIllegalArgumentException() {
        factorial(-1);
    }

    private static int factorial(int number) {
        return matchValue(number,
            ifIs(n -> n < 0, n -> { throw new IllegalArgumentException("Cannot calculate factorial of a negative number"); }),
            ifEquals(0, n -> 1)
        ).otherwise(n -> n * factorial(n-1));
    }

    static class A {
        private final String msg;


        A(String msg) {
            this.msg = msg;
        }

        String message() {
            return msg;
        }
    }

    static class B extends A {

        B(String msg) {
            super(msg);
        }

        String messageForB() {
            return "I'm a B and I say " + message();
        }
    }

    static class C extends A {

        C(String msg) {
            super(msg);
        }

        String messageForC() {
            return "I'm a C and I say " + message();
        }
    }

    static class Things {
        final String a;
        final String b;
        final String c;


        Things(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        Optional<String> a() {
            return Optional.ofNullable(a);
        }


        Optional<String> b() {
            return Optional.ofNullable(b);
        }


        Optional<String> c() {
            return Optional.ofNullable(c);
        }
    }

}
