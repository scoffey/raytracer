package scene;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import raytracer.Util;

public class BoundingBox {

	public double xmin, xmax, ymin, ymax, zmin, zmax;

	/** Crea una BoundingBox con las coordenadas límite en cada dirección. */
	public BoundingBox(double x1, double x2, double y1, double y2, double z1,
			double z2) {
		xmin = Math.min(x1, x2);
		xmax = Math.max(x1, x2);
		ymin = Math.min(y1, y2);
		ymax = Math.max(y1, y2);
		zmin = Math.min(z1, z2);
		zmax = Math.max(z1, z2);
	}

	/** Crea una BoundingBox con los puntos de dos esquinas opuestas. */
	public BoundingBox(Vector3d p1, Vector3d p2) {
		xmin = Math.min(p1.x, p2.x);
		xmax = Math.max(p1.x, p2.x);
		ymin = Math.min(p1.y, p2.y);
		ymax = Math.max(p1.y, p2.y);
		zmin = Math.min(p1.z, p2.z);
		zmax = Math.max(p1.z, p2.z);
	}

	/** Create una BoundingBox idéntica a otra. */
	public BoundingBox(BoundingBox b) {
		xmin = b.xmin;
		ymin = b.ymin;
		zmin = b.zmin;
		xmax = b.xmax;
		ymax = b.ymax;
		zmax = b.zmax;
	}

	/** @returns Vector con el ancho, alto y largo de la BoundingBox. */
	public Vector3d getSize() {
		return new Vector3d(xmax - xmin, ymax - ymin, zmax - zmin);
	}

	/** @returns Punto medio de la BoundingBox. */
	public Vector3d getCenter() {
		return new Vector3d((xmax + xmin) / 2.0, (ymax + ymin) / 2.0,
				(zmax + zmin) / 2.0);
	}

	/** @returns Arreglo con los puntos de las 8 esquinas del BoundingBox. */
	public Vector3d[] getCorners() {
		return new Vector3d[] { new Vector3d(xmin, ymin, zmin),
				new Vector3d(xmin, ymin, zmax), new Vector3d(xmin, ymax, zmin),
				new Vector3d(xmin, ymax, zmax), new Vector3d(xmax, ymin, zmin),
				new Vector3d(xmax, ymin, zmax), new Vector3d(xmax, ymax, zmin),
				new Vector3d(xmax, ymax, zmax) };
	}

	/** @returns Nueva BoundingBox contenedora de esta y otra. */
	public BoundingBox merge(BoundingBox b) {
		return new BoundingBox(Math.min(xmin, b.xmin), Math.max(xmax, b.xmax),
				Math.min(ymin, b.ymin), Math.max(ymax, b.ymax), Math.min(zmin,
						b.zmin), Math.max(zmax, b.zmax));
	}

	/** Extiende esta BoundingBox (la modifica) para que abarque a otra. */
	public void extend(BoundingBox b) {
		if (b.xmin < xmin)
			xmin = b.xmin;
		if (b.ymin < ymin)
			ymin = b.ymin;
		if (b.zmin < zmin)
			zmin = b.zmin;
		if (b.xmax > xmax)
			xmax = b.xmax;
		if (b.ymax > ymax)
			ymax = b.ymax;
		if (b.zmax > zmax)
			zmax = b.zmax;
	}

	/** @returns Determina si el punto está contenido en la BoundingBox. */
	public final boolean contains(Vector3d p) {
		if (p.x < xmin || p.x > xmax || p.y < ymin || p.y > ymax || p.z < zmin
				|| p.z > zmax)
			return false;
		return true;
	}

	/** @returns Determina si esta BoundingBox se intersecta con otra. */
	public final boolean intersects(BoundingBox b) {
		if (xmin > b.xmax || xmax < b.xmin || ymin > b.ymax || ymax < b.ymin
				|| zmin > b.zmax || zmax < b.zmin)
			return false;
		return true;
	}

	/** @returns Distancia entre un punto y el punto más cercano en la BoundingBox. */
	public final double distanceToPoint(Vector3d p) {
		double x, y, z;

		if (p.x < xmin)
			x = xmin - p.x;
		else if (p.x > xmax)
			x = p.x - xmax;
		else
			x = 0.0;
		if (p.y < ymin)
			y = ymin - p.y;
		else if (p.y > ymax)
			y = p.y - ymax;
		else
			y = 0.0;
		if (p.z < zmin)
			z = zmin - p.z;
		else if (p.z > zmax)
			z = p.z - zmax;
		else
			z = 0.0;
		return Math.sqrt(x * x + y * y + z * z);
	}

	/** Extiende esta BoundingBox en una distancia fija en cada dirección. */
	public final void outset(double dist) {
		xmin -= dist;
		ymin -= dist;
		zmin -= dist;
		xmax += dist;
		ymax += dist;
		zmax += dist;
	}

	/** @returns Nueva BoundingBox desplazada por los delta dados en cada dirección. */
	public final BoundingBox translate(double dx, double dy, double dz) {
		return new BoundingBox(xmin + dx, xmax + dx, ymin + dy, ymax + dy, zmin
				+ dz, zmax + dz);
	}

	/**
	 * Aplica una transformación (matriz m) a cada una de las 8 esquinas
	 * de la BoundingBox y genera una nueva que la contenga.
	 * 
	 * @return Nueva BoundingBox que contiene la transformación aplicada.
	 */
	public final BoundingBox transformAndOutset(Matrix4d m) {
		double newxmin, newxmax, newymin, newymax, newzmin, newzmax;

		// Convertir las esquinas a Vector4d para luego multiplicarlas con m
		Vector4d p, corners[];
		Vector3d tmp[] = getCorners();
		corners = new Vector4d[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			corners[i] = new Vector4d(tmp[i].x, tmp[i].y, tmp[i].z, 1.0);
		}
		
		p = Util.MultiplyMatrixAndVector(m, corners[0]);
		newxmin = newxmax = p.x;
		newymin = newymax = p.y;
		newzmin = newzmax = p.z;
		for (int i = 1; i < 8; i++) {
			p = Util.MultiplyMatrixAndVector(m, corners[i]);
			if (p.x < newxmin)
				newxmin = p.x;
			if (p.x > newxmax)
				newxmax = p.x;
			if (p.y < newymin)
				newymin = p.y;
			if (p.y > newymax)
				newymax = p.y;
			if (p.z < newzmin)
				newzmin = p.z;
			if (p.z > newzmax)
				newzmax = p.z;
		}
		return new BoundingBox(newxmin, newxmax, newymin, newymax, newzmin,
				newzmax);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "BoundingBox(xmin=" + xmin + ", xmax=" + xmax + ", ymin=" + ymin
				+ ", ymax=" + ymax + ", zmin=" + zmin + ", zmax=" + zmax + ")";
	}

}
