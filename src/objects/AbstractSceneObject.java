package objects;

import java.util.Collection;

import javax.vecmath.Vector3d;

import raytracer.Ray;
import scene.BoundingBox;
import scene.Intersection;
import scene.Transformation;

/**
 * Superclase de todos los objetos de la escena. Contiene los atributos comunes
 * a todos los objetos, y las operaciones definidas como métodos abstractos para
 * que cada uno los implemente.
 */
public abstract class AbstractSceneObject implements SceneObject {

	public Material material = new Material();
	
	public Material getMaterial() {
		return material;
	}

	public abstract Vector3d getNormalAt(Vector3d point);

	public abstract void transform(Transformation t);

	public abstract BoundingBox getBounds();

	public boolean intersectsBox(BoundingBox bb) {
		return bb.intersects(getBounds());
	}

	public abstract Intersection intersectsRay(Ray ray);

	public Collection<? extends SceneObject> getChildren() {
		return null;
	}

}
