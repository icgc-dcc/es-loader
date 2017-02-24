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
  private final String filePrefix;
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
    for (val archiveFileName : docSource.getArchiveFileNames(filePrefix)) {
      @Cleanup
      val indexDocumentsIterator = docSource.getTarArchiveIndexDocumentsIterator(archiveFileName);
      val indexName = TarArchiveNames.getIndexName(archiveFileName);
      loadIndexDocuments(indexDocumentsIterator, indexNamePrefix + indexName);
    }

  }

  private void initializeIndex(MappingDAG mappingDAG, Map<String, JsonNode> settings) {
    log.info("Initializing indices...");
    val client = createClient(esUri);
    val indexService = new IndexService(client, mappingDAG, settings);
    indexService.initIndexFromArchiveNames(inputDir, indexNamePrefix, filePrefix);
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

