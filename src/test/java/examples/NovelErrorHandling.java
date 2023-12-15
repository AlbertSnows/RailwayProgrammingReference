package examples;

import co.unruly.control.result.Result;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static co.unruly.control.result.Resolvers.collapse;
import static co.unruly.control.result.Result.failure;
import static co.unruly.control.result.Result.success;
import static co.unruly.control.result.Transformers.attempt;
import static co.unruly.control.result.Transformers.onSuccess;
import static java.lang.String.format;

@SuppressWarnings("unused")
public class NovelErrorHandling {

    public static String
    novelSales(@NotNull Author author,
               @NotNull Publisher publisher,
               @NotNull Editor editor,
               @NotNull Retailer retailer) {
        return author.getIdea()
                .then(attempt(publisher::getAdvance))
                .then(attempt(author::writeNovel))
                .then(onSuccess(editor::editNovel))
                .then(onSuccess(publisher::publishNovel))
                .then(onSuccess(retailer::sellNovel))
                .then(onSuccess(sales -> format("%s sold %d copies", sales.novel, sales.copiesSold)))
                .then(collapse());
    }

    public static class Author {
        private final Result<Idea, String> idea;
        private final int skill;
        private final int lifestyleCosts;

        public Author(Result<Idea, String> idea, int skill, int lifestyleCosts) {
            this.idea = idea;
            this.skill = skill;
            this.lifestyleCosts = lifestyleCosts;
        }

        public Result<Idea, String> getIdea() {
            return idea;
        }

        public Result<Manuscript, String> writeNovel(@NotNull Advance advance) {
            if(advance.amount > lifestyleCosts) {
                int happiness = advance.amount - lifestyleCosts;
                return success(new Manuscript(advance.idea.title, happiness * skill));
            } else {
                return failure("Ran out of money, went back to work at Tescos");
            }
        }
    }

    public record Publisher(int qualityThreshold, int generosity) {

        public Result<Advance, String> getAdvance(@NotNull Idea idea) {
                if (idea.appeal >= qualityThreshold) {
                    return success(new Advance(idea.appeal * generosity, idea));
                } else {
                    return failure("This novel wouldn't sell");
                }
            }

            @Contract("_ -> new")
            public @NotNull Novel publishNovel(@NotNull Manuscript manuscript) {
                return new Novel(manuscript.title, manuscript.quality);
            }
        }

    public static class Editor {
        public Manuscript editNovel(@NotNull Manuscript manuscript) {
            return new Manuscript(manuscript.title, manuscript.quality + 3);
        }
    }

    public static class Retailer {
        private final int customerCount;

        public Retailer(int customerCount) {
            this.customerCount = customerCount;
        }

        public Sales sellNovel(Novel novel) {
            return new Sales(novel, novel.quality * customerCount);
        }
    }

    public record Idea(String title, int appeal) {
    }

    public record Advance(int amount, Idea idea) {
    }

    public record Manuscript(String title, int quality) {
    }

    public record Novel(String title, int quality) {
    }

    public static class Sales {
        private final Novel novel;
        private final int copiesSold;

        public Sales(Novel novel, int copiesSold) {

            this.novel = novel;
            this.copiesSold = copiesSold;
        }
    }
}
