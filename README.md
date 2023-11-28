# tallink-gtfs-scraper [![Build project with Gradle](https://github.com/mjaakko/tallink-gtfs-scraper/actions/workflows/build.yml/badge.svg)](https://github.com/mjaakko/tallink-gtfs-scraper/actions/workflows/build.yml)

Application for creating a [GTFS feed](https://gtfs.org/) for Tallink ferries by scraping schedules from Tallink's Booking API.

## Development

* Build Jar file: `./gradlew shadowJar`

## Usage

Either build the Jar file yourself or run the application with Docker. Docker image is available from DockerHub with name `mjaakko/tallink-gtfs-scraper`.

Parameter for the output file (`<output file>`) is optional. If it's omitted, the GTFS feed will be written to a file with name `tallink.zip` in the current working directory

### Docker
```bash
docker run --rm -it mjaakko/tallink-gtfs-scraper <output file>
```

### Jar
```bash
java -jar tallink-gtfs-scraper.jar <output file>
```

