package com.accenture.beam.pipelines.teleport.coders;

import org.apache.beam.sdk.coders.DefaultCoder;

import org.apache.avro.reflect.Nullable;

import java.util.Objects;
import com.google.common.base.MoreObjects;

/**
 * The {@link FailsafeElement} class holds the current value and original value of a record within a
 * pipeline. This class allows pipelines to not lose valuable information about an incoming record
 * throughout the processing of that record. The use of this class allows for more robust
 * dead-letter strategies as the original record information is not lost throughout the pipeline and
 * can be output to a dead-letter in the event of a failure during one of the pipelines transforms.
 */
@DefaultCoder(FailsafeElementCoder.class)
public class FailsafeElement<OriginalT, CurrentT> {

    private final OriginalT originalPayload;
    private final CurrentT currentPayload;

    @Nullable private String errorMessage;
    @Nullable private String stacktrace;


    public FailsafeElement(OriginalT originalPayload, CurrentT currentPayload) {
        this.originalPayload = originalPayload;
        this.currentPayload = currentPayload;
    }

    public OriginalT getOriginalPayload() {
        return originalPayload;
    }

    public CurrentT getCurrentPayload() {
        return currentPayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FailsafeElement<?, ?> that = (FailsafeElement<?, ?>) o;
        return Objects.deepEquals(this.originalPayload, that.getOriginalPayload())
                && Objects.deepEquals(this.currentPayload, that.getCurrentPayload())
                && Objects.deepEquals(this.errorMessage, that.getErrorMessage())
                && Objects.deepEquals(this.stacktrace, that.getStacktrace());
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalPayload, currentPayload, errorMessage, stacktrace);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("originalPayload", originalPayload)
                .add("currentPayload", currentPayload)
                .add("errorMessage", errorMessage)
                .add("stacktrace", stacktrace)
                .toString();
    }
}
