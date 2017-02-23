package org.icgc.dcc.es.loader.service;

import com.fasterxml.jackson.databind.JsonNode;
import static com.google.common.base.Preconditions.checkState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.icgc.dcc.es.loader.model.MappingDAG;
import org.icgc.dcc.es.loader.model.MappingDAG.TypeMapping;
import org.icgc.dcc.es.loader.util.InputDirs;

import java.io.File;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class IndexService {

  @NonNull
  private final Client client;
  @NonNull
  private final MappingDAG mappingDAG;
  @NonNull
  private final Map<String, JsonNode> settings;


  public void initIndexFromArchiveNames(@NonNull File inputDir, String indexNamePrefix) {
    val indexNames = InputDirs.getIndexNamesFromArchiveFiles(inputDir, indexNamePrefix);
    log.info("Index names: {}", indexNames);

    for (val indexName : indexNames) {
      createIndexAndMappings(indexName);
    }
  }

  private void createIndexAndMappings(String indexName) {
    val setting = settings.get(indexName);

    val indicesClient = client.admin().indices();
    log.info("Creating index '{}'", indexName);
    createIndex(indicesClient, indexName, setting.toString());
    log.info("Created index '{}'", indexName);

    for (val rootMapping : mappingDAG.getRoots().values()) {
      if (rootMapping.getIndexName().equals(indexName)) {
        rootMapping.applyRecursive((TypeMapping m) -> {
          String indexTypeName = m.getTypeName();
          String indexTypeMapping = m.getJson().toString();
          createMapping(indicesClient, indexName, indexTypeName, indexTypeMapping);
        });
      }
    }
  }

  private static void createMapping(IndicesAdminClient indicesClient, String indexName, String indexTypeName,
                                    String indexTypeMapping) {
    val created = indicesClient
        .preparePutMapping(indexName)
        .setType(indexTypeName)
        .setSource(indexTypeMapping)
        .execute()
        .actionGet()
        .isAcknowledged();

    checkState(created, "Failed to create mapping for index '%s' and type '%s'", indexName, indexTypeName);
  }

  private static void createIndex(IndicesAdminClient indicesClient, String indexName, String indexSettings) {
    val created = indicesClient
        .prepareCreate(indexName)
        .setSettings(indexSettings)
        .execute()
        .actionGet()
        .isAcknowledged();

    checkState(created, "Failed to create index '%s'", indexName);
  }

}
