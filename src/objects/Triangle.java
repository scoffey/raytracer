package objects;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import raytracer.Ray;
import raytracer.Util;
import scene.BoundingBox;
import scene.Intersection;
import scene.OctreeScene;
import scene.Transformation;

public class Triangle extends AbstractSceneObject {

	public Vector3d p1, p2, p3; // los 3 puntos que determinan el triángulo
	private Vector3d normal; // normal del triángulo
	private Vector3d p2mp1, p3mp2, p1mp3; // vectores de los lados
	
	public Triangle(Vector3d p1, Vector3d p2, Vector3d p3) {
		super();
		this.p1 = new Vector3d(p1);
		this.p2 = new Vector3d(p2);
		this.p3 = new Vector3d(p3);
		// por eficiencia se mantienen calculados algunos atributos
		setTriangleAttrs();
	}

	private void setTriangleAttrs() {
		Vector3d v1 = new Vector3d(p2);
		v1.sub(p1);
		Vector3d v2 = new Vector3d(p3);
		v2.sub(p1);
		normal = new Vector3d();
		normal.cross(v1, v2);
		normal.normalize();
		
		p2mp1 = new Vector3d(p2);
		p2mp1.sub(p1);
		
		p3mp2 = new Vector3d(p3);
		p3mp2.sub(p2);
		
		p1mp3 = new Vector3d(p1);
		p1mp3.sub(p3);
	}

	public Vector3d getNormalAt(Vector3d point) {
		return getNormal();
	}

	public Vector3d getNormal() {
		return new Vector3d(normal);
	}

	public Intersection intersectsRay(Ray ray) {
		return checkIntersection(ray, Double.MAX_VALUE);
	}

	@Override
	public void transform(Transformation t) {
		Matrix4d m = t.getTransformationMatrix(true);

		Vector4d aux = new Vector4d(p1.x, p1.y, p1.z, 1);
		aux = Util.MultiplyMatrixAndVector(m, aux);
		p1.set(aux.x, aux.y, aux.z);

		aux = new Vector4d(p2.x, p2.y, p2.z, 1);
		aux = Util.MultiplyMatrixAndVector(m, aux);
		p2.set(aux.x, aux.y, aux.z);

		aux = new Vector4d(p3.x, p3.y, p3.z, 1);
		aux = Util.MultiplyMatrixAndVector(m, aux);
		p3.set(aux.x, aux.y, aux.z);

		setTriangleAttrs();
	}

	public boolean pointBelongs(Vector3d point) {
		Vector3d aux = new Vector3d(point);
		aux.sub(p1);
		
		// Si no pertenece al plano, return false
		if (Math.abs(aux.dot(normal)) > OctreeScene.TOLERANCE) {
			return false;
		}
		
		Vector3d aux2 = new Vector3d(point);
		aux2.sub(p1);
		aux2.cross(p2mp1, aux2);
		if (aux2.dot(normal) < 0) {
			return false;
		}
		
		aux2 = new Vector3d(point);
		aux2.sub(p2);
		aux2.cross(p3mp2, aux2);
		if (aux2.dot(normal) < 0) {
			return false;
		}
		
		aux2 = new Vector3d(point);
		aux2.sub(p3);
		aux2.cross(p1mp3, aux2);
		if (aux2.dot(normal) < 0) {
			return false;
		}
		
		return true;
	}

	@Override
	public String toString() {
		return "Triangle(p1=" + p1 + ", p2=" + p2 + ", p3=" + p3 + ", normal="
				+ normal + ")";
	}

	public boolean intersectsBox(BoundingBox bb) {
		// Si algún vértice está contenido en la caja, intersecta
		if (bb.contains(p1) || bb.contains(p2) || bb.contains(p3))
			return true;

		// Si algún eje intersecta la caja, intersecta
		if (edgeIntersectsBox(p1, p2, bb) || edgeIntersectsBox(p2, p3, bb)
				|| edgeIntersectsBox(p3, p1, bb))
			return true;

		// Todavía puede ser que el triángulo seccione la caja sin que sus
		// ejes la intesecten (una esquina, todo un plano de la caja, etc.)
		// Para eso, chequear si alguna de las 4 diagonales de la caja
		// intersectan el triángulo.
		Ray r = new Ray(new Vector3d(), new Vector3d());
		Vector3d orig = r.position, dir = r.direction;
		double len;

		orig.set(bb.xmin, bb.ymin, bb.zmin);
		dir.set(bb.xmax - bb.xmin, bb.ymax - bb.ymin, bb.zmax - bb.zmin);
		len = dir.length();
		dir.scale(1.0 / len);
		if (checkIntersection(r, len) != null)
			return true;
		orig.set(bb.xmax, bb.ymin, bb.zmin);
		dir.set(bb.xmin - bb.xmax, bb.ymax - bb.ymin, bb.zmax - bb.zmin);
		len = dir.length();
		dir.scale(1.0 / len);
		if (checkIntersection(r, len) != null)
			return true;
		orig.set(bb.xmin, bb.ymax, bb.zmin);
		dir.set(bb.xmax - bb.xmin, bb.ymin - bb.ymax, bb.zmax - bb.zmin);
		len = dir.length();
		dir.scale(1.0 / len);
		if (checkIntersection(r, len) != null)
			return true;
		orig.set(bb.xmin, bb.ymin, bb.zmax);
		dir.set(bb.xmax - bb.xmin, bb.ymax - bb.ymin, bb.zmin - bb.zmax);
		len = dir.length();
		dir.scale(1.0 / len);
		if (checkIntersection(r, len) != null)
			return true;
		return false;
	}

	private boolean edgeIntersectsBox(Vector3d p1, Vector3d p2, BoundingBox bb) {
		double t1, t2, tmin = -Double.MAX_VALUE, tmax = Double.MAX_VALUE;
		double dirx = p2.x - p1.x, diry = p2.y - p1.y, dirz = p2.z - p1.z;
		double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
		if (dirx == 0.0) {
			if (p1.x < bb.xmin || p1.x > bb.xmax)
				return false;
		} else {
			t1 = (bb.xmin - p1.x) * len / dirx;
			t2 = (bb.xmax - p1.x) * len / dirx;
			if (t1 < t2) {
				if (t1 > tmin)
					tmin = t1;
				if (t2 < tmax)
					tmax = t2;
			} else {
				if (t2 > tmin)
					tmin = t2;
				if (t1 < tmax)
					tmax = t1;
			}
			if (tmin > tmax || tmin > len || tmax < 0.0)
				return false;
		}
		if (diry == 0.0) {
			if (p1.y < bb.ymin || p1.y > bb.ymax)
				return false;
		} else {
			t1 = (bb.ymin - p1.y) * len / diry;
			t2 = (bb.ymax - p1.y) * len / diry;
			if (t1 < t2) {
				if (t1 > tmin)
					tmin = t1;
				if (t2 < tmax)
					tmax = t2;
			} else {
				if (t2 > tmin)
					tmin = t2;
				if (t1 < tmax)
					tmax = t1;
			}
			if (tmin > tmax || tmin > len || tmax < 0.0)
				return false;
		}
		if (dirz == 0.0) {
			if (p1.z < bb.zmin || p1.z > bb.zmax)
				return false;
		} else {
			t1 = (bb.zmin - p1.z) * len / dirz;
			t2 = (bb.zmax - p1.z) * len / dirz;
			if (t1 < t2) {
				if (t1 > tmin)
					tmin = t1;
				if (t2 < tmax)
					tmax = t2;
			} else {
				if (t2 > tmin)
					tmin = t2;
				if (t1 < tmax)
					tmax = t1;
			}
			if (tmin > tmax || tmin > len || tmax < 0.0)
				return false;
		}
		return true;
	}

	private Intersection checkIntersection(Ray r, double withinDistance) {
		Vector3d normal = new Vector3d(this.normal);
		
		// Si no pertenece al plano del triángulo, no hay intersección
		double auxDot = normal.dot(r.direction);
		if (auxDot < 0) {
			normal.scale(-1);
		}
		if (normal.dot(r.direction) < OctreeScene.TOLERANCE) {
			return null;
		}
		
		// Sino, calcular el punto de intersección en el plano
		Vector3d a = new Vector3d(p1);
		a.sub(r.position);
		double t = a.dot(normal) / r.direction.dot(normal);
		if (!(t >= 0 && t < withinDistance)) {
			return null;
		}
		Vector3d pointOfIntersection = new Vector3d(r.direction);
		pointOfIntersection.scale(t);
		pointOfIntersection.add(r.position);
		
		// Determinar si el punto de intersección pertenece al triángulo
		if (pointBelongs(pointOfIntersection)) {
			Intersection i = new Intersection(pointOfIntersection, normal, t);
			return i;
		}
		return null;
	}

	public BoundingBox getBounds() {
		Vector3d min = new Vector3d(p1);
		Vector3d max = new Vector3d(p1);
		findEndpoints(p2, min, max);
		findEndpoints(p3, min, max);
		return new BoundingBox(min, max);
	}

	private void findEndpoints(Vector3d p, Vector3d min, Vector3d max) {
		min.x = Math.min(min.x, p.x);
		min.y = Math.min(min.y, p.y);
		min.z = Math.min(min.z, p.z);
		max.x = Math.max(max.x, p.x);
		max.y = Math.max(max.y, p.y);
		max.z = Math.max(max.z, p.z);
	}

}
