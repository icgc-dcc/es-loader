package org.icgc.dcc.es.loader.core;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.dcc.common.es.DocumentWriterConfiguration;
import org.icgc.dcc.dcc.common.es.DocumentWriterFactory;
import static org.icgc.dcc.dcc.common.es.TransportClientFactory.createClient;
import org.icgc.dcc.dcc.common.es.core.DocumentWriter;
import org.icgc.dcc.es.loader.model.MappingDAG;
import org.icgc.dcc.es.loader.service.IndexService;
import org.icgc.dcc.es.loader.service.MappingDAGService;
import org.icgc.dcc.es.loader.service.SettingsService;
import org.icgc.dcc.es.loader.util.TarArchiveNames;

import java.io.File;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class Indexer {

  @NonNull
  private final String esUri;
  private final String indexNamePrefix;
  @NonNull
  private final File inputDir;
  @NonNull
  private final File configDir;
  private final int concurrency;
  private final boolean skipIndexInit;

  @SneakyThrows
  public void indexDocuments() {

    val dagService = new MappingDAGService(configDir);
    val mappingDAG = dagService.readMappings();

    val settingsService = new SettingsService(configDir);
    val settings = settingsService.readSettings();

    val docSource = new DocumentSource(inputDir);
    if (!skipIndexInit) {
      initializeIndex(mappingDAG, settings);
    }

    // Read from an archive
    for (val archiveFileName : docSource.getArchiveFileNames(indexNamePrefix)) {
      @Cleanup
      val indexDocumentsIterator = docSource.getTarArchiveIndexDocumentsIterator(archiveFileName);
      val indexName = TarArchiveNames.getIndexName(archiveFileName);
      loadIndexDocuments(indexDocumentsIterator, indexName);
    }

  }

  private void initializeIndex(MappingDAG mappingDAG, Map<String, JsonNode> settings) {
    log.info("Initializing indices...");
    val client = createClient(esUri);
    val indexService = new IndexService(client, mappingDAG, settings);
    indexService.initIndexFromArchiveNames(inputDir, indexNamePrefix);
    client.close();
    log.info("Finished indices initialization.");
  }

  @SneakyThrows
  private void loadIndexDocuments(IndexDocumentsIterator indexDocumentsIterator, String indexName) {
    log.info("Indexing documents for index '{}'...", indexName);
    @Cleanup
    val docWriter = createDocWriter(indexName);
    while (indexDocumentsIterator.hasNext()) {
      docWriter.write(indexDocumentsIterator.next());
    }
    log.info("Finished indexing index '{}' documents", indexName);
  }

  @SneakyThrows
  private void loadIndexDocuments(DocumentSource docSource, String indexName) {
    val indexTypes = docSource.getIndexTypes(indexName);
    @Cleanup
    val docWriter = createDocWriter(indexName);
    log.info("'{}' index types: {}", indexTypes);
    for (val indexType : indexTypes) {
      val docsIterator = docSource.getIndexDocumentsIterator(indexName, indexType);
      log.info("Loading documents for '{}' and index {} type", indexName, indexType);
      while (docsIterator.hasNext()) {
        docWriter.write(docsIterator.next());
      }
      log.info("Finished loading documents for index '{}' and type '{}'", indexName, indexType);
    }

    log.info("Finished loading documents for index '{}'", indexName);
  }

  private DocumentWriter createDocWriter(String indexName) {
    val config = createWriterConfiguration(indexName);

    return DocumentWriterFactory.createDocumentWriter(config);
  }

  private DocumentWriterConfiguration createWriterConfiguration(String indexName) {
    return new DocumentWriterConfiguration()
        .esUrl(esUri)
        .indexName(indexName)
        .threadsNum(concurrency);
  }

}

