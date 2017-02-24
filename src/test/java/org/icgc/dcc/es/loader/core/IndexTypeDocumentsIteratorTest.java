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

import lombok.val;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import java.io.File;

public class IndexTypeDocumentsIteratorTest {

  private static final String INDEX_TYPE_NAME = "case-ssm-child";
  private static final File INPUT_DIR = new File("src/test/resources/fixtures/input/gdc-r1-gene/" + INDEX_TYPE_NAME);

  IndexTypeDocumentsIterator documentsIterator = new IndexTypeDocumentsIterator(INPUT_DIR);

  @Test
  public void testHasNext() throws Exception {
    assertThat(documentsIterator.hasNext()).isTrue();
    assertThat(documentsIterator.hasNext()).isTrue();
    assertThat(documentsIterator.hasNext()).isTrue();
    assertThat(documentsIterator.hasNext()).isTrue();
    documentsIterator.next();
    assertThat(documentsIterator.hasNext()).isFalse();
    assertThat(documentsIterator.hasNext()).isFalse();
    assertThat(documentsIterator.hasNext()).isFalse();
    assertThat(documentsIterator.hasNext()).isFalse();
  }

  @Test
  public void testNext() throws Exception {
    val actualDoc = documentsIterator.next();
    assertThat(actualDoc.getId()).isEqualTo("ID1");
    assertThat(actualDoc.getParentId()).isEqualTo("ENSG00000104904");
    assertThat(actualDoc.getSource().toString()).isEqualTo("{\"value\":1}");
    assertThat(actualDoc.getType().getIndexType()).isEqualTo(INDEX_TYPE_NAME);
  }

}
