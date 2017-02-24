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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

@Slf4j
public class IndexTypeDocumentsIterator extends AbstractIndexDocumentIterator {

  private final IndexDocumentType indexType;
  private final Iterator<Path> filesIterator;

  @SneakyThrows
  public IndexTypeDocumentsIterator(File indexTypeDir) {
    log.debug("Creating IndexDocumentsIterator for index type directory '{}'", indexTypeDir);
    val indexDirPath = Paths.get(indexTypeDir.getAbsolutePath());
    val dirStream = Files.walk(indexDirPath).filter(Files::isRegularFile);
    val indexTypeName = indexTypeDir.getName();
    log.debug("Resolved index type name to '{}'", indexTypeName);

    this.indexType = () -> indexTypeName;
    this.filesIterator = dirStream.iterator();
  }

  @Override
  public boolean hasNext() {
    return filesIterator.hasNext();
  }

  @Override
  @SneakyThrows
  public IndexDocument next() {
    val docPath = filesIterator.next();
    val docFile = docPath.toFile();

    log.debug("Reading {}", docFile);
    val source = (ObjectNode) DEFAULT_MAPPER.readTree(docFile);
    val fileName = docFile.getName();

    return createIndexDocument(fileName, indexType, source);
  }

}
