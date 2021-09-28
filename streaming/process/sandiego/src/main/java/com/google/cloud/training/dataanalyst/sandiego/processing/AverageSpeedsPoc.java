package com.google.cloud.training.dataanalyst.sandiego.processing;

import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableFieldSchema;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.values.PCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * this build a pipeline get data from pub/sub topic and save data to BigQuery dataset
 */
public class AverageSpeedsPoc {

    public interface MyOptions extends DataflowPipelineOptions{

    }


    /**
     * set up pipeline via cmd line
     */
    public void setupPipeline(String[] args) {
        // Create and set your PipelineOptions.
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        options.setStreaming(true);

        // Create the Pipeline with the specified options.
        Pipeline p = Pipeline.create(options);

        String topic = "projects/" + options.getProject() + "/topics/sandiego";
        String avgSpeedTable = options.getProject() + ":demos.average_speeds";

        // Build the table schema for the output table.
        List<TableFieldSchema> fields = new ArrayList<>();
        fields.add(new TableFieldSchema().setName("timestamp").setType("TIMESTAMP"));
        fields.add(new TableFieldSchema().setName("lat").setType("FLOAT"));
        fields.add(new TableFieldSchema().setName("long").setType("FLOAT"));
        fields.add(new TableFieldSchema().setName("highway").setType("STRING"));
        fields.add(new TableFieldSchema().setName("direction").setType("STRING"));
        fields.add(new TableFieldSchema().setName("lane").setType("INTEGER"));
        fields.add(new TableFieldSchema().setName("speed").setType("FLOAT"));
        fields.add(new TableFieldSchema().setName("sensorId").setType("STRING"));





    }

}
