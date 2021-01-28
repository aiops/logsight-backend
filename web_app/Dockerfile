# docker build -t model_training .

# set base image (host OS)
FROM python:3.6

# set the working directory in the container
WORKDIR /code

#COPY . .
# copy the dependencies file to the working directory
COPY requirements.txt .

# install dependencies
RUN pip install -r requirements.txt

# copy the content of the local src directory to the working directory
COPY . .

# command to run on container start
ENTRYPOINT [ "python3" ]
