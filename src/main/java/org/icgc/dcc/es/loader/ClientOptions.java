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

  @Parameter(names = { "--index-name-prefix" }, description = "Create index with the following prefix")
  public String indexNamePrefix;

  @Parameter(names = { "--file-prefix" }, description = "When scanning through contents of the input directory, process only files that start with the prefix")
  public String filePrefix;

  @Parameter(names = { "--concurrency" }, description = "How many threads to use for index loading.")
  public int concurrency = 0;

  @Parameter(names = { "--skip-init" }, arity = 1, description = "Skip indices initialization.")
  public boolean skipIndexInit = false;

}
