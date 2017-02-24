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

import com.fasterxml.jackson.databind.node.ObjectNode;
import static com.google.common.base.Preconditions.checkState;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;
import static org.icgc.dcc.es.loader.util.TarArchiveNames.getDocumentId;
import static org.icgc.dcc.es.loader.util.TarArchiveNames.getDocumentType;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

@Slf4j
public class TarArchiveDocumentsIterator extends AbstractIndexDocumentIterator implements Closeable {

  private final TarArchiveInputStream tarInputStream;

  private IndexDocument nextDocument;

  public TarArchiveDocumentsIterator(@NonNull File archive) {
    val absolutePath = archive.getAbsolutePath();
    checkState(archive.canRead(), "Failed to read %s", absolutePath);
    log.info("Creating tar archive documents iterator for file '{}'", absolutePath);

    this.tarInputStream = readArchiveStream(archive);
  }

  @Override
  public boolean hasNext() {
    if (nextDocument == null) {
      nextDocument = readNextDocument();
    }

    return nextDocument != null;
  }

  @Override
  public IndexDocument next() {
    // Read next document if it was not read.
    hasNext();
    val next = nextDocument;
    nextDocument = null;

    return next;
  }

  @Override
  public void close() throws IOException {
    if (tarInputStream != null) {
      tarInputStream.close();
    }
  }

  @SneakyThrows
  private IndexDocument readNextDocument() {
    val entry = readNextTarEntry();
    if (entry == null) {
      return null;
    }

    val entryName = entry.getName();
    val fileName = getDocumentId(entryName);
    val source = (ObjectNode) NO_CLOSE_MAPPER.readTree(tarInputStream);
    final IndexDocumentType indexType = () -> getDocumentType(entryName);

    return createIndexDocument(fileName, indexType, source);
  }

  @SneakyThrows
  private TarArchiveEntry readNextTarEntry() {
    TarArchiveEntry entry;
    while ((entry = tarInputStream.getNextTarEntry()) != null && entry.isDirectory()) {
      // read until get a non-directory entry
    }

    return entry;
  }

  @SneakyThrows
  private static TarArchiveInputStream readArchiveStream(File archive) {
    val inputStream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(archive)));

    return new TarArchiveInputStream(inputStream);
  }

}
