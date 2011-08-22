package scene;

import javax.vecmath.Vector3d;

public class Intersection {

	/** Punto de intersección */
	public Vector3d point;

	/** Normal de la superficie intersectada en el punto de intersección */
	public Vector3d normal;

	/** Distancia desde el origen del rayo hasta el punto de intersección. */
	public double distance;
	
	public Intersection() {
		point = new Vector3d();
		normal = new Vector3d();
	}

	public Intersection(Vector3d point, Vector3d normal, double distance) {
		this.point = new Vector3d(point);
		this.normal = new Vector3d(normal);
		this.distance = distance;
	}

	@Override
	public String toString() {
		return "Intersection(point=" + point.toString() + ", normal="
				+ normal.toString() + ", distance=" + distance + ")";
	}

}
