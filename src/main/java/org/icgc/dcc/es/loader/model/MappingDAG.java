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
