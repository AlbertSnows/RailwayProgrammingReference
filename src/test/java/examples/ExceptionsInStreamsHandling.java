package examples;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static co.unruly.control.result.Recover.ifType;
import static co.unruly.control.result.Introducers.tryTo;
import static co.unruly.control.result.Resolvers.ifFailed;
import static co.unruly.control.result.Transformers.*;

@SuppressWarnings({"unused", "NewClassNamingConvention"})
public class ExceptionsInStreamsHandling {

    @Test
    public void handling_exceptions_with_result_example() {
        List<Integer> customerAges = Stream.of("Bob", "Bill")
            .map(tryTo(this::findCustomerByName))
            .peek(onSuccessDo(this::sendEmailUpdateTo))
            .map(onSuccess(Customer::age))
            .map(recover(ifType(NoCustomerWithThatName.class, error -> {
                log("Customer not found :(@");
                return -1;
            })))
            .map(recover(ifType(IOException.class, error -> -2)))
            .map(ifFailed(__ -> -127))
            .toList();
    }

    @Test
    public void handling_multiple_exceptions_with_result_example() {

        List<Integer> customerValues = Stream.of("Bob", "Bill")
            .map(tryTo(this::findCustomerByName))
            .peek(onSuccessDo(this::sendEmailUpdateTo))
            .map(onSuccessTry(Customer::calculateValue))
            .map(recover(ifType(NoCustomerWithThatName.class, error -> {
                log("Customer not found :(");
                return -1;
            })))
            .map(recover(ifType(IOException.class, error -> -2)))
            .map(ifFailed(__ -> -127))
            .toList();
    }


    static class CustomerNotFound extends Exception {}
    static class NoCustomerWithThatName extends CustomerNotFound {}

    public Customer findCustomerByName(String name) {
        return customer;
    }

    private void sendEmailUpdateTo(Customer potentialCustomer) {
        email(customer.emailAddress(), customer.name());
    }

    private void email(String email, String name) {

    }

    private void log(String s) {

    }

    public interface Customer {
        default String emailAddress(){ return "";}
        default String name() { return ""; }
        default int age() { return 0; }

        default int calculateValue() { return 0; }
        class CostUnknown extends Exception {}
    }

    static Customer customer = new Customer(){};


}
