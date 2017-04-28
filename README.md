# ESP8266 Firmware storage service

Web-service to store firmwares for ESP8266 Arduino core, letting them
pull fresh updates over-the-air.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server-headless
	
Uberjar, deploying and environment variables will be documented later.
