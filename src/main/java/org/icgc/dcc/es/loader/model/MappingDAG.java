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

package org.icgc.dcc.es.loader.model;

import com.fasterxml.jackson.databind.JsonNode;
import static java.lang.String.format;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Disjoint DAG due to type hierarchy of mappings
 */
@RequiredArgsConstructor
public class MappingDAG {

  @Getter
  private final Map<String, TypeMapping> roots;

  /**
   * Find root type mapping based on a given type mapping
   * @param typeName type name of an index type
   * @return returns TypeMapping object that is the root of the parent-child relationship.
   */
  public TypeMapping getRoot(String typeName) {
    for (val root : roots.entrySet()) {
      val mapping = root.getValue();
      val found = mapping.applyRecursive((TypeMapping m) -> m.getTypeName().equals(typeName));
      if (found) {
        return mapping;
      }
    }

    throw new RuntimeException(format("Cannot find mapping files required for type: %s", typeName));
  }

  @Data
  public static class TypeMapping {

    String typeName;
    String indexName;
    JsonNode json;
    Set<TypeMapping> children = new HashSet<>();

    public boolean addChild(TypeMapping child) {
      return children.add(child);
    }

    public void applyRecursive(Consumer<TypeMapping> func) {
      if (!children.isEmpty()) {
        for (val child: children) {
          child.applyRecursive(func);
        }
      }

      func.accept(this);
    }

    public boolean applyRecursive(Predicate<TypeMapping> func) {
      boolean found = false;

      if (!children.isEmpty()) {
        for (val child: children) {
          found = found || child.applyRecursive(func);
        }
      }

      return found || func.test(this);
    }

  }

}
