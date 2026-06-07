package streamflix.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Nodo del árbol de decisión para el sistema de recomendación.
 * Cada nodo representa una condición de decisión o una hoja con recomendaciones.
 * 
 * Estructura del árbol:
 * - Nodos internos: contienen un atributo y un umbral para la decisión
 * - Nodos hoja: contienen las películas recomendadas para ese perfil
 */
public class DecisionTreeNode {
    private String attribute;        // Atributo de decisión (ej: "age", "genre", "rating")
    private String condition;        // Condición legible (ej: "age <= 25")
    private double threshold;        // Umbral numérico para la decisión
    private String categoryValue;    // Valor categórico para la decisión
    private DecisionTreeNode left;   // Subárbol izquierdo (condición verdadera)
    private DecisionTreeNode right;  // Subárbol derecho (condición falsa)
    private boolean isLeaf;          // Indica si es un nodo hoja
    private List<String> recommendedGenres; // Géneros recomendados (solo en hojas)
    private String label;            // Etiqueta descriptiva del nodo
    private int depth;               // Profundidad en el árbol
    private double giniIndex;        // Índice Gini para la pureza del nodo

    // Constructor para nodo interno
    public DecisionTreeNode(String attribute, String condition, double threshold) {
        this.attribute = attribute;
        this.condition = condition;
        this.threshold = threshold;
        this.isLeaf = false;
        this.recommendedGenres = new ArrayList<>();
        this.depth = 0;
    }

    // Constructor para nodo hoja
    public DecisionTreeNode(List<String> recommendedGenres, String label) {
        this.isLeaf = true;
        this.recommendedGenres = recommendedGenres;
        this.label = label;
        this.depth = 0;
    }

    // Getters y Setters
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public String getCategoryValue() { return categoryValue; }
    public void setCategoryValue(String categoryValue) { this.categoryValue = categoryValue; }

    public DecisionTreeNode getLeft() { return left; }
    public void setLeft(DecisionTreeNode left) { this.left = left; }

    public DecisionTreeNode getRight() { return right; }
    public void setRight(DecisionTreeNode right) { this.right = right; }

    public boolean isLeaf() { return isLeaf; }
    public void setLeaf(boolean leaf) { isLeaf = leaf; }

    public List<String> getRecommendedGenres() { return recommendedGenres; }
    public void setRecommendedGenres(List<String> recommendedGenres) { this.recommendedGenres = recommendedGenres; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public double getGiniIndex() { return giniIndex; }
    public void setGiniIndex(double giniIndex) { this.giniIndex = giniIndex; }

    @Override
    public String toString() {
        if (isLeaf) {
            return String.format("Hoja[label='%s', generos=%s]", label, recommendedGenres);
        }
        return String.format("Nodo[attr='%s', condicion='%s', umbral=%.1f]", attribute, condition, threshold);
    }
}
