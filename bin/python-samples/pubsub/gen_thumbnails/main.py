from wand.image import Image
from google.cloud import storage

client = storage.Client()
thumbnail_bucket = 'tim-acloud-guru-thumbnails'


def gen_thumbnail(data, context):
    # Get attributes from JSON payload
    bucket = data['attributes']['bucketId']
    image = data['attributes']['objectId']
    thumbnail = 'thumbnail -' + image

    # Get the image from GCS
    bucket = client.get_bucket(bucket)
    blob = bucket.get_blob(image)
    imagedata = blob.download_as_string()

    #  Create a new image object and resample it
    newimage = Image(blob=imagedata)
    newimage.sample(300, 300)

    # Upload the resampled image to the thumbnails bucket
    bucket = client.get_bucket(thumbnail_bucket)
    newblob = bucket.blob(thumbnail)
    newblob.upload_from_string(newimage.make_blob())
