FROM openjdk:11-jre-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
		python \
		python-setuptools \
		python-pip \
	&& rm -rf /var/lib/apt/lists/* \
  && pip install awscli \
  && pip install boto3 \
  && rm -rf ~/.cache/pip

ENV JAVA_HOME=/usr/lib/jvm/java-1.8-openjdk \
    USER=appuser
ENV HOME=/home/$USER
RUN useradd $USER

## Copy configuration overrides and run script
## Copy application itself
## PREREQUISITE:
##    Docker cannot access contents outside, so this must be run when building:
##    $ cp -p "../target/$(ls -t ../target/*.jar | grep -v /orig | head -1)" app.jar
COPY GetPropertiesFromParameterStore.py runapp.sh app.jar wordlist $HOME/

## Create directory for holding application logs and configure permissions
## Configure permissions
RUN chmod 755 $HOME/*.sh && \
    chown -R $USER $HOME

## Expose application port
EXPOSE 8080
EXPOSE 3002

USER $USER
WORKDIR /home/$USER
CMD ["./runapp.sh"]
