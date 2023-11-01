# SCIDAR-ORCID-APP

Jednostavni API koji služi kao dodatak DSpace 7.x. API obezbeđuje:

* vezu sa CSV fajlom sa istraživačima i identifikatorima, sa automatskim osvežavanjem,
* vezu sa SOLR indeksom,
* pretragu istraživača po ORCID-u,
* pretragu istraživača po DSpace authority-ju,
* slanje mejla adminu sa detaljima primedbe.

```
mvn package
java -jar target/scidar-orcid-app.jar
```

UI se nalazi na http://localhost:8081/swagger-ui/index.html.
