# tallink-gtfs-scraper

Application for scraping Tallink's booking API to create GTFS feed containing schedules for Tallink ferries

## Usage

Jar: 
```bash
java -jar tallink-gtfs-scraper.jar <output file>
```

Docker:
```bash
docker run --rm -it mjaakko/tallink-gtfs-scraper <output file>
```

Output file parameter is optional. If it's omitted, file named `tallink.zip` will be created
