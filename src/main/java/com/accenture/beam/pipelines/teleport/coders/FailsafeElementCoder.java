package com.accenture.beam.pipelines.teleport.coders;

import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.coders.CustomCoder;
import org.checkerframework.checker.initialization.qual.Initialized;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.UnknownKeyFor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The {@link FailsafeElementCoder} encodes and decodes {@link FailsafeElement} objects.
 *
 * <p>This coder is necessary until Avro supports parameterized types (<a
 * href="https://issues.apache.org/jira/browse/AVRO-1571">AVRO-1571</a>) without requiring to
 * explicitly specifying the schema for the type.
 *
 * @param <OriginalT> The type of the original payload to be encoded.
 * @param <CurrentT> The type of the current payload to be encoded.
 */
public class FailsafeElementCoder<OriginalT, CurrentT> extends CustomCoder<FailsafeElement<OriginalT, CurrentT>> {
    @Override
    public void encode(FailsafeElement<OriginalT, CurrentT> value, @UnknownKeyFor @NonNull @Initialized OutputStream outStream) throws @UnknownKeyFor@NonNull@Initialized CoderException, @UnknownKeyFor@NonNull@Initialized IOException {

    }

    @Override
    public FailsafeElement<OriginalT, CurrentT> decode(@UnknownKeyFor @NonNull @Initialized InputStream inStream) throws @UnknownKeyFor@NonNull@Initialized CoderException, @UnknownKeyFor@NonNull@Initialized IOException {
        return null;
    }
}
