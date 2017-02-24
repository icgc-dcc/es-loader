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


  public void initIndexFromArchiveNames(@NonNull File inputDir, String indexNamePrefix, String filePrefix) {
    val indexNames = InputDirs.getIndexNamesFromArchiveFiles(inputDir, filePrefix);
    log.info("Index names: {}", indexNames);

    for (val indexName : indexNames) {
      createIndexAndMappings(indexName, indexNamePrefix);
    }
  }

  private void createIndexAndMappings(String indexName, String indexNamePrefix) {
    val setting = settings.get(indexName);

    val indicesClient = client.admin().indices();
    log.info("Creating index '{}'", indexNamePrefix + indexName);
    createIndex(indicesClient, indexNamePrefix + indexName, setting.toString());
    log.info("Created index '{}'", indexNamePrefix + indexName);

    for (val rootMapping : mappingDAG.getRoots().values()) {
      if (rootMapping.getIndexName().equals(indexName)) {
        rootMapping.applyRecursive((TypeMapping m) -> {
          String indexTypeName = m.getTypeName();
          String indexTypeMapping = m.getJson().toString();
          createMapping(indicesClient, indexNamePrefix + indexName, indexTypeName, indexTypeMapping);
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
