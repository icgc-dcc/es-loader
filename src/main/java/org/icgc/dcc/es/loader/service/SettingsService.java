package org.icgc.dcc.es.loader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import static org.icgc.dcc.common.core.util.stream.Collectors.toImmutableMap;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SettingsService {

  /**
   * Constants
   */
  private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final String SETTINGS_MATCH = "settings";

  /**
   * Dependencies
   */
  private final File configDir;

  @SneakyThrows
  public Map<String, JsonNode> readSettings() {
    val files = Files.list(Paths.get(configDir.getAbsolutePath()));

    val settingsMap = files
        .map(Path::toUri)
        .map(File::new)
        .filter(this::isSettingsFile)
        .collect(toImmutableMap(this::indexNameFromFile, this::readYaml));

    return settingsMap;
  }

  private boolean isSettingsFile(File file) {
    return file.getName().contains(SETTINGS_MATCH);
  }

  private String indexNameFromFile(File file) {
    return file.getName().split("\\.")[0];
  }

  @SneakyThrows
  private JsonNode readYaml(@NonNull File file) {
    return YAML_OBJECT_MAPPER.readTree(file);
  }

}
