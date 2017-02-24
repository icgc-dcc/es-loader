# Elasticsearch Loader 

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/abfd287de36b46aeae5b8aa9c9dbf0e8)](https://www.codacy.com/app/icgc-dcc/es-loader?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=icgc-dcc/es-loader&amp;utm_campaign=Badge_Grade)
![](https://api.travis-ci.org/icgc-dcc/es-loader.svg?branch=develop)

Facilitates the creation of Elasticsearch indexes, the loading of type mappings, and indexing of data. 

The loader will parse the mappings files to construct a DAG representation of parent-child relationships to ensure the
 loading of mappings in the correct order. 
 
 Based on the original work done by @btiernay and @slovit

## Build

In this directory run the following in the shell:

```shell
mvn clean package
```
This will produce a JAR file `target/es-loader-<version>.jar`

## Run

To see all the options the utility accepts run the JAR without any arguments:

```shell
java -jar es-loader-0.0.1-SNAPSHOT.jar 
com.beust.jcommander.ParameterException: The following options are required:     --es-uri     --input-dir     --config-dir 
Usage: <main class> [options]
  Options:
        --concurrency
       How many threads to use for index loading.
       Default: 0
  *     --config-dir
       Config directory which contains mappings and settings.
  *     --es-uri
       Elasticsearch URI to index to.
        --file-prefix
       When scanning through contents of the input directory, process only files
       that start with the prefix
        --index-name-prefix
       Create index with the following prefix
  *     --input-dir
       Input directory which contains documents to load.
        --skip-init
       Skip indices initialization.
       Default: false
```

`--es-uri`, `--configdir`, and `--input-dir` are mandatory. The other ones are optional.
