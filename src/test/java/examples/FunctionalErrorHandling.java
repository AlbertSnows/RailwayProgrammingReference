package examples;

import co.unruly.control.result.Result;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static co.unruly.control.result.Combiners.combineWith;
import static co.unruly.control.result.Resolvers.ifFailed;
import static co.unruly.control.result.Result.failure;
import static co.unruly.control.result.Result.success;
import static co.unruly.control.result.Transformers.attempt;
import static co.unruly.control.result.Transformers.onSuccess;

@SuppressWarnings({"unused", "NewClassNamingConvention"})
public class FunctionalErrorHandling {

    @Test
    public void howToMakeBreakfast() {
        final Fridge fridge = new Fridge();
        final Bread bread = new Bread();

        // I don't know whether the eggs are good or not...
        Result<Eggs, Garbage> eggs
            = fridge.areEggsOff()
            ? success(fridge.getEggs())
            : failure(new Garbage(fridge.getEggs()));

        // I'm also terrible at cooking, and can ruin eggs by burning
        // or undercooking them, but salting them isn't a problem
        Result<ScrambledEggs, Garbage> scrambledEggs
            = eggs.then(attempt(Eggs::scramble))
            .then(onSuccess(Condiments::salt));


        // I can reliably turn bread into toast, too
        Result<Toast, Garbage> toast
            = success(bread, Garbage.class).then(onSuccess(Bread::toast));

        // I am however good enough to put the eggs on toast
        Result<ScrambledEggsOnToast, Garbage> eggsOnToast = scrambledEggs.then(combineWith(toast)).using(ScrambledEggsOnToast::new);

        @SuppressWarnings("unused")
        Breakfast breakfast = eggsOnToast.then(ifFailed(__ -> new BowlOfCornflakes()));
    }

    private static class Fridge {

        public boolean areEggsOff() {
            return false;
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull Eggs getEggs() {
            return new Eggs();
        }
    }

    private static class Bread {

        @Contract(value = " -> new", pure = true)
        public @NotNull Toast toast() {
            return new Toast();
        }
    }

    private static class Garbage {

        public Garbage(Eggs eggs) {

        }
    }

    private static class Toast {

    }

    private static class ScrambledEggs {

    }

    private static class Eggs {

        @Contract(" -> new")
        public @NotNull Result<ScrambledEggs, Garbage> scramble() {
            return success(new ScrambledEggs());
        }
    }

    private static class Condiments {
        public static ScrambledEggs salt(ScrambledEggs unsalted) {
            return unsalted;
        }
    }

    private record ScrambledEggsOnToast(ScrambledEggs eggs, Toast toast) implements Breakfast {
    }

    private static class BowlOfCornflakes implements Breakfast {

    }

    private interface Breakfast {

    }

}
