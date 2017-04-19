# Airports

This is a RESTful API that allows you to perform two basics operations on a database of countries, airports and runways.

1. **Query** all the runways at each import for a given country. The input can be the country name (i.e. Venezuela) or country ISO code (i.e. VE).
2. **Get a report** which contains the following information:
    - Top 10 countries with highest number of airports.
    - Top 10 countries with lowest number of airports.
    - Type of runways per country.
    - Top 10 most common runway identifications.
    
## Running the code

You'll need `sbt 0.13.15`. Just run:
    
```
sbt compile
sbt run
```
    
This will start the server. It will listen on [http://0.0.0.0:1234](http://0.0.0.0:1234).
    
If you want to run the tests, run:
    
```
sbt test
```
    
## API:
 
 #### Getting the report
 
 Endpoint:
 
 ```
 GET /v1/report
 ```
 
 This will return something like [this.](http://www.google.com)
 
 
 #### Querying
 
 Endpoint:
 
 
```
GET /v1/query/{countryReference}[?referenceIsCode=<true|false>]
```

Where:
    
* countryReference is a **path** parameter that can be either the country name or its ISO code.
* referenceIsCode is a **query** parameter that indicates if contryReference should be interpreted as a ISO code (true) or as a name (false). Defaults to true.

Example: 

```
GET /v1/query/Netherlands?referenceIsCode=false

or

GET /v1/query/NL (same as GET /v1/query/NL?referenceIsCode=true)

```

This will return [this.]()

#### Swagger

For more interactivity, you can play with the Swagger UI specific to this API at [http://0.0.0.0:1234/swagger/](http://0.0.0.0:1234/swagger/)
