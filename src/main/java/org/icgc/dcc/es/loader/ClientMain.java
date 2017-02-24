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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.System.err;
import lombok.val;
import org.icgc.dcc.es.loader.core.Indexer;

import java.io.File;

public class ClientMain {

  public static void main(String[] args) {
    val options = new ClientOptions();
    val cli = new JCommander(options);

    try {
      cli.parse(args);
      val esUri = options.esUri;

      val inputDirectory = new File(options.inputDir);
      val configDirectory = new File(options.configDir);
      checkState(inputDirectory.isDirectory() && inputDirectory.canRead(), "Failed to read from %s", inputDirectory);
      checkState(configDirectory.isDirectory() && configDirectory.canRead(), "Failed to read from %s", configDirectory);

      val indexer = new Indexer(esUri, options.indexNamePrefix, inputDirectory, configDirectory, options.concurrency,
          options.skipIndexInit);
      indexer.indexDocuments();
    } catch (ParameterException e) {
      err.println(e.toString());
      usage(cli);
      System.exit(1);
    }
  }

  private static void usage(JCommander cli) {
    val message = new StringBuilder();
    cli.usage(message);
    err.println(message.toString());
  }

}
