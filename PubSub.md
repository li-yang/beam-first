### Additional Resources:
- Codelabs: https://codelabs.developers.google.com/
- Big Data & Machine Learning Blog: https://cloud.google.com/blog/products/ai-machine-learning
- Handson with guide: https://cloud.google.com/blog/products/data-analytics/guide-to-common-cloud-dataflow-use-case-patterns-part-1

### Demo: Cloud Pub/Sub Client Libraries
- updated git repo: https://github.com/googleapis/python-pubsub.git
  ```
     python3 publisher.py playground-s-11-bcc4c00a create LabTopic
:~/python-pubsub/samples/snippets (playground-s-11-bcc4c00a)$ python3 subscriber.py playground-s-11-bcc4c00a create LabTopic LabSub
  ```
- bord.py to consume rest api and mock publish message




### Demo: Loosely Coupled Services with Cloud Pub/Sub
#### create function to generate thumbnail from image via cloud functions topic-triggered. 
```
    (venv) cloud_user_p_21800717@cloudshell:~/gen_thumbnail (playground-s-11-5e11d869)$ gcloud functions deploy gen_thumbnail --runtime python37 --trigger-topic newImage
```
### Demo: Stream Data through Cloud Pub/Sub to BigQuery
- create simulated data for shopping transaction
- publish data to pub/sub
- process data through Cloud Dataflow
- view data in BigQuery
- generate fake data: https://mockaroo.com/

``` sudo pip3 install google-cloud-pubsub
    python3 transactions.py
```
Demo Steps: Stream Data through Cloud Pub/Sub to BigQuery
##################################################################
1. Create a bucket matching your project name (e.g. gs://playground-s-11-78f5b410)
2. Leave other settings as default

#UPDATED STEP#:
3. Go to IAM and copy the compute service account name to a text editor (e.g. 526273785053-compute@developer.gserviceaccount.com)
#UPDATED STEP#


4. Create a Pub/Sub topic named "purchases"
5. Copy the default subscription name to a text editor (e.g. projects/playground-s-11-78f5b410/subscriptions/purchases-sub)
6. Go to Big Query and create a dataset
7. Create a table and add the schema, and copy the table ID (e.g. playground-s-11-78f5b410:trends.purchases)

UPDATED STEPS:
8. Create a data-flow job with max workers=1, number of workers = 1, Machine type n1-standard-1
9. The job will create, but then throw a credentials issue (this is because the Dataflow API had not finished enabling before the job tried to run)
10. Click on CLONE and then create another copy - the second job will run correctly.
#UPDATED STEPS#

11. Open Cloud Shell, run sudo pip3 install google-cloud-pubsub and the open the Shell editor, to create transactions.py and copy in MOCK_DATA.csv as per the lesson video
12. Run python3 transactions.py
13. Use Big Query to query the data which will now have been synced into the purchases table.
```      
SELECT country, count(country) as purchases
FROM 'trends.purchases'
GROUP BY country;

```
- find the most expensive item has been bought in Germany
```
SELECT product, amount
FROM 'trends.purchases'
WHERE date > DATE(2019,10,15)
AND country = 'Germany'
ORDER BY amount DESC;
```
