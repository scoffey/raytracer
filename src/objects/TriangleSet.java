package objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import javax.vecmath.Vector3d;

import raytracer.Ray;
import raytracer.Util;
import scene.BoundingBox;
import scene.Intersection;
import scene.Transformation;

public class TriangleSet extends AbstractSceneObject {

	private Set<Triangle> triangles;
	private BoundingBox bb;

	public TriangleSet() {
		triangles = new HashSet<Triangle>();
		bb = null;
	}

	@Override
	public Vector3d getNormalAt(Vector3d point) {
		for (Triangle t : triangles) {
			if (t.pointBelongs(point)) {
				return t.getNormalAt(point);
			}
		}
		return null;
	}

	private void setBounds() {
		if (triangles.size() == 0) {
			bb = new BoundingBox(0, 0, 0, 0, 0, 0);
		} else {
			Iterator<Triangle> it = triangles.iterator();
			bb = it.next().getBounds();
			while (it.hasNext()) {
				bb = bb.merge(it.next().getBounds());
			}
		}
	}

	@Override
	public Intersection intersectsRay(Ray ray) {
		double currentDistance, nearestDistance = Double.MAX_VALUE;
		Intersection nearestIntersection = null;

		for (Triangle t : triangles) {
			Intersection currentIntersection = t.intersectsRay(ray);
			if (currentIntersection == null) {
				continue;
			}

			Vector3d aux = new Vector3d(currentIntersection.point);
			aux.sub(ray.position);
			currentDistance = Util.Norm(aux);

			if (nearestIntersection == null
					|| currentDistance < nearestDistance) {
				nearestIntersection = currentIntersection;
				nearestDistance = currentDistance;
			}
		}

		return nearestIntersection;
	}

	@Override
	public void transform(Transformation t) {
		for (Triangle tri : triangles) {
			tri.transform(t);
		}
	}

	public void addTriangle(Vector3d p1, Vector3d p2, Vector3d p3) {
		Triangle t = new Triangle(p1, p2, p3);
		//System.out.println("Adding triangle to TriangleSet: " + t);
		t.material = this.material;
		triangles.add(t);
		bb = null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("TriangleSet(");
		for (Triangle t : triangles) {
			sb.append(t.toString());
			sb.append(", ");
		}
		return sb.substring(0, sb.length() - 2) + ")";
	}

	@Override
	public BoundingBox getBounds() {
		if (bb == null) {
			setBounds();
		}
		return bb;
	}

	@Override
	public Collection<? extends SceneObject> getChildren() {
		return triangles;
	}

}
