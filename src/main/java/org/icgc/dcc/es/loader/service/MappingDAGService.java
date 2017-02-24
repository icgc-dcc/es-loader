/*
 * Copyright (c) 2017 The Ontario Institute for Cancer Research. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.icgc.dcc.es.loader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.es.loader.model.MappingDAG;
import org.icgc.dcc.es.loader.model.MappingDAG.TypeMapping;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class MappingDAGService {

  /**
   * Constants
   */
  private static final ObjectMapper YAML_OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final String MAPPING_MATCH = "mapping";

  /**
   * Dependencies
   */
  private final File configDir;

  @SneakyThrows
  public MappingDAG readMappings() {
    val files = Files.list(Paths.get(configDir.getAbsolutePath()));

    val mappingFiles = files
        .map(Path::toUri)
        .map(File::new)
        .filter(this::isMappingFile)
        .collect(toList());

    val graph = constructDAG(mappingFiles);
    logGraph(graph);
    return graph;
  }

  private boolean isMappingFile(File file) {
    return file.getName().contains(MAPPING_MATCH);
  }

  private MappingDAG constructDAG(List<File> mappingFiles) throws RuntimeException {
    log.info("Constructing Mapping DAG");

    val vertices = new ConcurrentHashMap<String, TypeMapping>();
    val edges = new HashMap<String, String>();
    for (val file: mappingFiles) {
      val typeName = file.getName().split("\\.")[1];
      val indexName = file.getName().split("\\.")[0];
      val json = readYaml(file);

      val typeMapping = new TypeMapping();
      typeMapping.setTypeName(typeName);
      typeMapping.setIndexName(indexName);
      typeMapping.setJson(json);

      vertices.put(typeName, typeMapping);

      val parent = json.path("_parent").path("type");
      if (!parent.isMissingNode()) {
        edges.put(typeName, json.path("_parent").path("type").textValue());
      }
    }

    for (val edge : edges.entrySet()) {
      val child = vertices.get(edge.getKey());
      val parent = vertices.get(edge.getValue());

      if (!parent.addChild(child)) {
        throw new RuntimeException(
            format("Cannot form parent-child relationship between mappings: %s , %s", edge.getKey(), edge.getValue()));
      }
    }

    for (val vertex : vertices.entrySet()) {
      val typeMapping = vertex.getValue();

      val children = typeMapping.getChildren();
      for (val child : children) {
        vertices.remove(child.getTypeName());
      }
    }

    return new MappingDAG(vertices);
  }

  private static void logGraph(@NonNull MappingDAG graph) {
    for (val root : graph.getRoots().entrySet()) {
      printVertex(root.getValue(), "");
    }
  }

  private static void printVertex(@NonNull TypeMapping mapping, @NonNull String prefix) {
    log.info(prefix + mapping.getTypeName());
    for (val child: mapping.getChildren()) {
      printVertex(child, prefix + "\t- ");
    }
  }

  @SneakyThrows
  private JsonNode readYaml(@NonNull File file) {
    return YAML_OBJECT_MAPPER.readTree(file);
  }

}

