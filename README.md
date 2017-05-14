# ESP8266 Firmware storage service

Web-service to store firmwares for ESP8266 Arduino core, letting them
pull fresh updates over-the-air.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

	lein ring server-headless

Also you can build `jar` archive with:

	lein uberjar

Then launch it with

	java -jar target/otaservice-standalone.jar

## Environment variables

- DATABASE_URL : pretty self-explanatory (migrations use PostgreSQL syntax)
- SECRET : required for Signed JWT token (changing it will invalidate all Auth tokens)

## Example

	DATABASE_URL=jdbc:postgresql://localhost/otaservice \
	SECRET=soprivate \
	lein ring server-headless

At `localhost:3000/api` there is Swagger-UI with all API methods.


## TODO
- UI
- Manage DB connection lifecycle
- tests??
- forgot the last one
