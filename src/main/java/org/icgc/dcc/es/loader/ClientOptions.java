package org.icgc.dcc.es.loader;

import com.beust.jcommander.Parameter;
import lombok.ToString;

@ToString
public class ClientOptions {

  @Parameter(names = { "--es-uri" }, required = true, description = "Elasticsearch URI to index to.")
  public String esUri;

  @Parameter(names = { "--input-dir" }, required = true, description = "Input directory which contains documents to load.")
  public String inputDir;

  @Parameter(names = { "--config-dir" }, required = true, description = "Config directory which contains mappings and settings.")
  public String configDir;

  @Parameter(names = { "--index-name-prefix" }, description = "When scaning through contents of the input directory, process only files that start with the prefix")
  public String indexNamePrefix;

  @Parameter(names = { "--concurrency" }, description = "How many threads to use for index loading.")
  public int concurrency = 0;

  @Parameter(names = { "--skip-init" }, arity = 1, description = "Skip indices initialization.")
  public boolean skipIndexInit = false;

}
