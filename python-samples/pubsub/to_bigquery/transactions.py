import json
import csv
from google.cloud import pubsub_v1

project_name = 'tim-acg-pubsub'
topic_name = 'purchases'
file = 'MOCK_DATA.csv'

publisher = pubsub_v1.PublisherClient()
topic_path = publisher.topic_path(project_name, topic_name)

with open(file) as fh:
    rd = csv.DictReader(fh, delimiter=',')
    for row in rd:
        data = json.dumps(dict(row))
        publisher.publish(topic_path, data=data.encode('utf-8'))

