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

import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.val;
import org.icgc.dcc.dcc.common.es.impl.IndexDocumentType;
import org.icgc.dcc.dcc.common.es.model.IndexDocument;

public abstract class AbstractIndexDocumentIterator implements IndexDocumentsIterator {

  protected static final String ID_SEPARATOR = ".";
  protected static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();
  protected static final ObjectMapper NO_CLOSE_MAPPER = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, false);

  protected IndexDocument createIndexDocument(String fileName, IndexDocumentType indexType, ObjectNode source) {
    return fileName.contains(ID_SEPARATOR) ?
        createParentChildDocument(fileName, indexType, source) :
        new IndexDocument(fileName, source, indexType);
  }

  private static IndexDocument createParentChildDocument(String fileName, IndexDocumentType indexType, ObjectNode source) {
    val separatorPosition = fileName.indexOf(ID_SEPARATOR);
    val parentId = fileName.substring(0, separatorPosition);
    val childId = fileName.substring(separatorPosition + 1);

    return new IndexDocument(childId, source, indexType, parentId);
  }

}