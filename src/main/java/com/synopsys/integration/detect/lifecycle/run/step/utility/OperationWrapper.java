package com.synopsys.integration.detect.lifecycle.run.step.utility;

import java.io.IOException;
import java.util.function.Consumer;

import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.lifecycle.OperationException;
import com.synopsys.integration.detect.workflow.status.Operation;
import com.synopsys.integration.exception.IntegrationException;

public class OperationWrapper {
    public void wrapped(Operation operation, OperationFunction supplier) throws OperationException {
        wrapped(operation, () -> { //To reduce duplication, calling the supplier with a return type but throwing away the returned result.
            supplier.execute();
            return true;
        });
    }

    public <T> T wrapped(Operation operation, OperationSupplier<T> supplier) throws OperationException {
        return wrapped(operation, supplier, () -> {}, (e) -> {});
    }

    public <T> T wrappedWithCallbacks(Operation operation, OperationSupplier<T> supplier, Runnable successConsumer, Consumer<Exception> errorConsumer) throws OperationException {
        return wrapped(operation, supplier, successConsumer, errorConsumer);
    }

    public <T> T wrapped(Operation operation, OperationSupplier<T> supplier, Runnable successConsumer, Consumer<Exception> errorConsumer) throws OperationException {
        try {
            T value = supplier.execute();
            operation.success();
            successConsumer.run();
            return value;
        } catch (InterruptedException e) {
            operation.error(e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            errorConsumer.accept(e);
            throw new OperationException(e);
        } catch (OperationException e) {
            operation.error(e);
            errorConsumer.accept(e);
            throw e;
        } catch (Exception e) {
            operation.error(e);
            errorConsumer.accept(e);
            throw new OperationException(e);
        } finally {
            operation.finish();
        }
    }

    @FunctionalInterface
    public interface OperationSupplier<T> {
        T execute() throws OperationException, DetectUserFriendlyException, IntegrationException, InterruptedException, IOException; //basically all known detect exceptions.
    }

    @FunctionalInterface
    public interface OperationFunction {
        void execute() throws OperationException, DetectUserFriendlyException, IntegrationException, InterruptedException, IOException; //basically all known detect exceptions.
    }
}
