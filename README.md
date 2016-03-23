# hyrax-server

## Usage

### Upload image to database
#### @POST - /upload 

> image - main image

> details - JSON with location,time (timestamp)

> face [ ] - faces in image

### Search face in database
#### @POST - /search 

> eigenface - FaceRecognizer XML save

> train_width - train image width

> train_height - train image height

### Get all images in database
#### @GET - /images

### Get image with id
#### @GET - /images/{id} 



