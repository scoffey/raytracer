package objects;

import java.util.Collection;

import javax.vecmath.Vector3d;

import raytracer.Ray;
import scene.BoundingBox;
import scene.Intersection;
import scene.Transformation;

/**
 * Interfaz de todos los objetos de la escena.
 */
public interface SceneObject {

	/** @return Propiedades del material del objeto */
	public Material getMaterial();

	/**
	 * Intersecta el rayo con el objeto. Si no se intersectan, retorna null. Si
	 * se intersectan, retorna el punto perteneciente a la superficie del objeto
	 * sobre el que intersectó.
	 * 
	 * @param ray Rayo que se está disparando.
	 * @return Punto de intersección, o null si no intersecta.
	 */
	public Intersection intersectsRay(Ray ray);

	/**
	 * Retorna la normal en un punto de la superficie de este objeto. Es
	 * responsabilidad del usuario que el punto proporcionado pertenezca a la
	 * superficie.
	 * 
	 * @param point Punto en el que se quiere evaluar la normal.
	 * @return La normal en el punto.
	 */
	public Vector3d getNormalAt(Vector3d point);

	/**
	 * Transforma los vértices del objeto de acuerdo a una determinada matriz de
	 * transformación.
	 * 
	 * @param transformationMatrix La matriz a aplicar.
	 */
	public void transform(Transformation t);

	/**
	 * @param bb BoundingBox a intersectar
	 * @return Si el objeto intersecta a la caja (está contenido o la corta)
	 */
	public boolean intersectsBox(BoundingBox bb);

	/**
	 * @return Mínima BoundingBox contenedora del objeto
	 */
	public abstract BoundingBox getBounds();

	/**
	 * @return Conjunto de objetos hijos (e.g.: Triangles en TriangleSets)
	 */
	public Collection<? extends SceneObject> getChildren();

}
