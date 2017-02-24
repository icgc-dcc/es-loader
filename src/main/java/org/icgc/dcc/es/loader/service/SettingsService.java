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
