package com.accenture.beam.examples;

import org.apache.beam.sdk.transforms.DoFn;

public class WordCount {

    /**
     * New Concepts:
     *
     * Applying ParDo with an explicit DoFn
     * Creating Composite Transforms
     * Using Parameterizable PipelineOptions
     */


    static class ExtractWordsFn extends DoFn<String, String> {

        @ProcessElement
        public void processElement(@Element String element, OutputReceiver<String> receiver) {

        }

    }



}
