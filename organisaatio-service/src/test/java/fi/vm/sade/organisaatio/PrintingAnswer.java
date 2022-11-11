package fi.vm.sade.organisaatio;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.function.Consumer;

public class PrintingAnswer<T> implements Answer<T> {

    private final Answer<T> delegate;
    private final Consumer<Object> logger;

    public PrintingAnswer() {
        this(invocation -> null, System.out::println);
    }

    public PrintingAnswer(Answer<T> delegate, Consumer<Object> logger) {
        this.delegate = delegate;
        this.logger = logger;
    }

    @Override
    public T answer(InvocationOnMock invocation) throws Throwable {
        Arrays.stream(invocation.getArguments()).forEach(logger);
        return delegate.answer(invocation);
    }

}
