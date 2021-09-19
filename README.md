Example Pipelines
The examples included in this module serve to demonstrate the basic functionality of Apache Beam, and act as starting points for the development of more complex pipelines.

Word Count
A good starting point for new users is our set of word count examples, which computes word frequencies. This series of four successively more detailed pipelines is described in detail in the accompanying walkthrough.

MinimalWordCount is the simplest word count pipeline and introduces basic concepts like Pipelines, PCollections, ParDo, and reading and writing data from external storage.

WordCount introduces best practices like PipelineOptions and custom PTransforms.

DebuggingWordCount demonstrates some best practices for instrumenting your pipeline code.

WindowedWordCount shows how to run the same pipeline over either unbounded PCollections in streaming mode or bounded PCollections in batch mode.

Running Examples
See Apache Beam WordCount Example for information on running these examples.

Beyond Word Count
After you've finished running your first few word count pipelines, take a look at the cookbook directory for some common and useful patterns like joining, filtering, and combining.

The complete directory contains a few realistic end-to-end pipelines.

See the other examples as well. This directory includes a Java 8 version of the MinimalWordCount example, as well as a series of examples in a simple 'mobile gaming' domain. This series introduces some advanced concepts. Finally, the following are user contributed examples:

CryptoRealTime Create an unbounded streaming source/reader and manage basic watermarking, checkpointing and record id for data ingestion, stream trading data from exchanges with BEAM + Medium post

##ACloudGuru: AI Platform Hands On Part 1

In these two hands on demonstrations, we will go over the process of training a pre-packaged machine learnng model on AI Platform, deploying the trained model, and running predictions against it.

All commands used in these two demonstrations are below.

If you want a set of scripts to automate much of the process, the scripts and a PDF file of the commands we used can be found at the following public link:

https://console.cloud.google.com/storage/browser/gcp-course-exercise-scripts/data-engineer/ai-platform

If you want to download all scripts and files in a terminal environment, use the below command:

gsutil cp gs://acg-gcp-course-exercise-scripts/data-engineer/ai-platform/\* .

(The period at the end is required to copy to your current location).

Full guide below:

### Set region environment variable

```
REGION=us-central1
```

### Download and unzip ML github data for demo

```
wget https://github.com/GoogleCloudPlatform/cloudml-samples/archive/master.zip

unzip master.zip
```

### Navigate to the cloudml-samples-master > census > estimator directory.

### ALL Commands must be run from this directory

```
cd ~/cloudml-samples-master/census/estimator
```

### **Develop and validate trainer on local machine**

### Get training data from public GCS bucket

```
mkdir data

gsutil -m cp gs://cloud-samples-data/ml-engine/census/data/* data/
```

### Set path variables for local file paths

### These will change later when we use AI Platform

```
TRAIN_DATA=$(pwd)/data/adult.data.csv

EVAL_DATA=$(pwd)/data/adult.test.csv
```

### Run sample requirements.txt to ensure we're using same version of TF as sample

```
sudo pip install -r ~/cloudml-samples-master/census/requirements.txt
```

### **Run a local trainer**

### Specify output directory, set as variable

```
MODEL_DIR=output
```

### Best practice is to delete contents of output directory in case

### data remains from previous training run

```
rm -rf $MODEL_DIR/*
```

### Run local training using gcloud

```
gcloud ai-platform local train --module-name trainer.task --package-path trainer/ \
--job-dir $MODEL_DIR -- --train-files $TRAIN_DATA --eval-files $EVAL_DATA \
--train-steps 1000 --eval-steps 100
```

### To see the result
``` 
tensorboard --logdir=$MODEL_DIR --port=8080
```

### Run trainer on GCP AI Platform - single instance

### Create regional Cloud Storage bucket used for all output and staging

```
gsutil mb -l $REGION gs://$DEVSHELL_PROJECT_ID-aip-demo
```

### Upload training and test/eval data to bucket

```
cd ~/cloudml-samples-master/census/estimator

gsutil cp -r data gs://$DEVSHELL_PROJECT_ID-aip-demo/data
```

### Set data variables to point to storage bucket files

```
TRAIN_DATA=gs://$DEVSHELL_PROJECT_ID-aip-demo/data/adult.data.csv

EVAL_DATA=gs://$DEVSHELL_PROJECT_ID-aip-demo/data/adult.test.csv
```

### Copy test.json to storage bucket

```
gsutil cp ../test.json gs://$DEVSHELL_PROJECT_ID-aip-demo/data/test.json
```

### Set TEST\_JSON to point to the same storage bucket file

```
TEST_JSON=gs://$DEVSHELL_PROJECT_ID-aip-demo/data/test.json
```

### Set variables for job name and output path

```
JOB_NAME=census_single_1

OUTPUT_PATH=gs://$DEVSHELL_PROJECT_ID-aip-demo/$JOB_NAME
```

### Submit a single process job to AI Platform

### Job name is JOB\_NAME (census\_single\_1)

### Output path is our Cloud storage bucket/job\_name

### Training and evaluation/test data is in our Cloud Storage bucket

```
gcloud ai-platform jobs submit training $JOB_NAME \
--job-dir $OUTPUT_PATH \
--runtime-version 1.15 \
--module-name trainer.task \
--package-path trainer/ \
--region $REGION \
-- \
--train-files $TRAIN_DATA \
--eval-files $EVAL_DATA \
--train-steps 1000 \
--eval-steps 100 \
--verbosity DEBUG
```

### Can view streaming logs/output with gcloud ai-platform jobs stream-logs $JOB\_NAME

### When complete, inspect output path with gsutil ls -r $OUTPUT\_PATH

### **Run distributed training on AI Platform**

### Create variable for distributed job name

```
cd ~/cloudml-samples-master/census/estimator

JOB_NAME=census_dist_1
```

### Set new output path to Cloud Storage location using new JOB\_NAME variable

```
OUTPUT_PATH=gs://$DEVSHELL_PROJECT_ID-aip-demo/$JOB_NAME
```

### Submit a distributed training job

### The '--scale-tier STANDARD\_1' option is the new item that initiates distributed scaling

```
gcloud ai-platform jobs submit training $JOB_NAME \
--job-dir $OUTPUT_PATH \
--runtime-version 1.15 \
--module-name trainer.task \
--package-path trainer/ \
--region $REGION \
--scale-tier STANDARD_1 \
-- \
--train-files $TRAIN_DATA \
--eval-files $EVAL_DATA \
--train-steps 1000 \
--verbosity DEBUG \
--eval-steps 100
```

### Prediction phase (testing it out)

### Deploy a model for prediction, setting variables in the process

```
cd ~/cloudml-samples-master/census/estimator

MODEL_NAME=census
```

### Create the ML Engine model

```
gcloud ai-platform models create $MODEL_NAME --regions=$REGION
```

### Set the job output we want to use. This example uses census\_dist\_1

`OUTPUT_PATH=gs://$DEVSHELL_PROJECT_ID-aip-demo/census_dist_1`

##### CHANGE census\_dist\_1 to use a different output from previous

## IMPORTANT - Look up and set full path for export trained model binaries

### gsutil ls -r $OUTPUT\_PATH/export

### Look for directory $OUTPUT\_PATH/export/census/ and copy/paste timestamp value (without colon) into the below command

`MODEL_BINARIES=gs://$DEVSHELL_PROJECT_ID-aip-demo/census_dist_1/export/census/<timestamp>` ###CHANGE ME!

### Create version 1 of your model

```
gcloud ai-platform versions create v1 \
--model $MODEL_NAME \
--origin $MODEL_BINARIES \
--runtime-version 1.15
```

### Send an online prediction request to our deployed model using test.json file

### Results come back with a direct response

```
gcloud ai-platform predict \
--model $MODEL_NAME \
--version v1 \
--json-instances \
../test.json
```

### Send a batch prediction job using same test.json file

### Results are exported to a Cloud Storage bucket location

### Set job name and output path variables

```
JOB_NAME=census_prediction_1

OUTPUT_PATH=gs://$DEVSHELL_PROJECT_ID-aip-demo/$JOB_NAME
```

### Submit the prediction job

```
gcloud ai-platform jobs submit prediction $JOB_NAME \
--model $MODEL_NAME \
--version v1 \
--data-format TEXT \
--region $REGION \
--input-paths $TEST_JSON \
--output-path $OUTPUT_PATH/predictions
```

### View results in web console at

`gs://$DEVSHELL_PROJECT_ID-aip-demo/$JOB_NAME/predictions/`


## datalab
In this lesson, we will go hands-on with Datalab, which is actually quite fun and interesting.

To create a Datalab notebook using Cloud Shell, type the below command:

`datalab create (instance-name)`

To connect to a Datalab notebook, type:

`datalab connect (instance-name)`

## Cloud Composer
This lesson will be a hands on tour of Cloud Composer in action. The commands we used in this lesson are duplicated below:

Create GCS bucket to output Dataproc results (using Project ID shell variable)

- gsutil mb -l us-central1 gs://output-$DEVSHELL_PROJECT_ID

Set our Cloud Composer variables necessary for our workflow. Project ID will again be represented by the shell variable to auto-resolve to your unique Project ID:

- gcloud composer environments run my-environment --location us-central1 variables -- --set gcp_project $DEVSHELL_PROJECT_ID

- gcloud composer environments run my-environment --location us-central1 variables -- --set gcs_bucket gs://output-$DEVSHELL_PROJECT_ID

- gcloud composer environments run my-environment --location us-central1 variables -- --set gce_zone us-central1-c


Upload the example DAG file to the DAG folder for Cloud Composer. A copy of the DAG file can be found at this link.

- Direct link for local download: https://storage.googleapis.com/la-gcloud-course-resources/data-engineer/cloud-composer/quickstart.py

- Public GCS location: gs://acg-gcloud-course-resources/data-engineer/cloud-composer/quickstart.py

- GitHub link: https://github.com/GoogleCloudPlatform/python-docs-samples/blob/b80895ed88ba86fce223df27a48bf481007ca708/composer/workflows/quickstart.py

### Additional Resources:
-- Codelabs: https://codelabs.developers.google.com/
-- Big Data & Machine Learning Blog: https://cloud.google.com/blog/products/ai-machine-learning
-- Handson with guide: https://cloud.google.com/blog/products/data-analytics/guide-to-common-cloud-dataflow-use-case-patterns-part-1



#### Demo: Loosely Coupled Services with Cloud Pub/Sub
