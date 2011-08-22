package scene;

import javax.vecmath.Vector3d;

import objects.AbstractSceneObject;
import objects.SceneObject;

import raytracer.Ray;

/**
 * Esta clase representa el nodo de un Octree, utilizado para disponer
 * eficientemente los objetos de una escena por ubicación.
 * 
 * Un OctreeNode puede ser terminal (cuando contiene lista de SceneObjects) o de
 * ramificación (cuando contiene otros OctreeNodes hijos).
 */

public class OctreeNode extends BoundingBox {

	private static final int MAX_OBJECTS_PER_NODE = 8;
	private static final int MAX_OCTREE_DEPTH = 16;

	private OctreeNode parent, child[];
	private SceneObject obj[];
	private double midx, midy, midz;
	private int depth;

	/**
	 * @param nodeBounds BoundingBox del nodo
	 * @param objects Objetos del nodo
	 * @param bb BoundingBoxes correspondientes a los objetos
	 */
	public OctreeNode(BoundingBox nodeBounds, SceneObject objects[],
			BoundingBox bb[]) {
		this(nodeBounds, objects, bb, null, 0);
	}

	/** Constructor a ser envuelto para el caso del nodo raíz */
	private OctreeNode(BoundingBox nodeBounds, SceneObject objects[],
			BoundingBox bb[], OctreeNode parentNode, int nodeDepth) {
		super(nodeBounds);
		boolean inside[] = new boolean[objects.length];
		int count, i;

		parent = parentNode;
		depth = nodeDepth;

		// Encontrar los objetos contenidos en este nodo
		for (i = 0, count = 0; i < objects.length; i++) {
			if (bb[i].intersects(this))
				if (objects[i].intersectsBox(this)) {
					inside[i] = true;
					count++;
				}
		}

		// Contruir la lista de objetos de este nodo y sus bb
		obj = new AbstractSceneObject[count];
		if (count == 0)
			return;
		BoundingBox objBounds[] = new BoundingBox[count];
		for (i = 0, count = 0; i < objects.length; i++) {
			if (inside[i]) {
				obj[count] = objects[i];
				objBounds[count++] = bb[i];
			}
		}

		// Ramificar este nodo si corresponde
		split(objBounds);
	}

	/**
	 * Ramifica el nodo actual si no tiene muchos obtejos y el octree todavía
	 * nos es muy profundo. En ese caso, construye hasta 8 hijos según las
	 * BoundingBoxes de sus objetos y marca el nodo como no terminal.
	 */
	private void split(BoundingBox objBounds[]) {
		if (obj.length <= MAX_OBJECTS_PER_NODE || depth >= MAX_OCTREE_DEPTH) {
			return;
		}

		boolean splitx, splity, splitz;
		findMidpoints(objBounds);
		splitx = (midx != xmax);
		splity = (midy != ymax);
		splitz = (midz != zmax);
		if (!(splitx || splity || splitz))
			return;

		child = new OctreeNode[8];
		int d = depth + 1;
		// near SW
		BoundingBox bb = new BoundingBox(xmin, midx, ymin, midy, zmin, midz);
		child[0] = new OctreeNode(bb, obj, objBounds, this, d);
		if (splitz) { // far SW
			bb = new BoundingBox(xmin, midx, ymin, midy, zmax, midz);
			child[1] = new OctreeNode(bb, obj, objBounds, this, d);
		}
		if (splity) { // near NW
			bb = new BoundingBox(xmin, midx, ymax, midy, zmin, midz);
			child[2] = new OctreeNode(bb, obj, objBounds, this, d);
			if (splitz) { // far NW
				bb = new BoundingBox(xmin, midx, ymax, midy, zmax, midz);
				child[3] = new OctreeNode(bb, obj, objBounds, this, d);
			}
		}
		if (splitx) { // near SE
			bb = new BoundingBox(xmax, midx, ymin, midy, zmin, midz);
			child[4] = new OctreeNode(bb, obj, objBounds, this, d);
			if (splitz) { // far SE
				bb = new BoundingBox(xmax, midx, ymin, midy, zmax, midz);
				child[5] = new OctreeNode(bb, obj, objBounds, this, d);
			}
			if (splity) { // near NE
				bb = new BoundingBox(xmax, midx, ymax, midy, zmin, midz);
				child[6] = new OctreeNode(bb, obj, objBounds, this, d);
				if (splitz) { // far NE
					bb = new BoundingBox(xmin, midx, ymax, midy, zmax, midz);
					child[7] = new OctreeNode(bb, obj, objBounds, this, d);
				}
			}
		}
		obj = null; // Marcar como nodo de ramificación (no terminal)
	}

	/**
	 * Este método debería ser invocado sobre un nodo terminal octree.
	 * 
	 * @return SceneObjects pertenecientes a este nodo.
	 */
	public SceneObject[] getObjects() {
		if (obj == null)
			throw new RuntimeException("OctreeNode.getObjects "
					+ "invocado sobre un nodo no terminal.");
		return obj;
	}

	/**
	 * Este método debería ser invocado sobre la raíz del octree.
	 * 
	 * @returns OctreeNode terminal que contiene al punto. (Sino null)
	 */
	public OctreeNode findNode(Vector3d pos) {
		OctreeNode current;

		if (!contains(pos))
			return null;
		current = this;
		while (current.obj == null) {
			if (pos.x > current.midx) {
				if (pos.y > current.midy) {
					if (pos.z > current.midz)
						current = current.child[7];
					else
						current = current.child[6];
				} else {
					if (pos.z > current.midz)
						current = current.child[5];
					else
						current = current.child[4];
				}
			} else {
				if (pos.y > current.midy) {
					if (pos.z > current.midz)
						current = current.child[3];
					else
						current = current.child[2];
				} else {
					if (pos.z > current.midz)
						current = current.child[1];
					else
						current = current.child[0];
				}
			}
		}
		return current;
	}

	/**
	 * @return Nodo terminal vecino por donde pasa un rayo. (Sino null)
	 */
	public OctreeNode findNextNode(Ray r) {
		double t1, t2, tmax = Double.MAX_VALUE;
		Vector3d orig = r.position, dir = r.direction;
		OctreeNode current;

		if (parent == null)
			return null;

		// Buscar el último punto por donde pasa el rayo dentro de este nodo.
		if (dir.x != 0.0) {
			t1 = (xmin - orig.x) / dir.x;
			t2 = (xmax - orig.x) / dir.x;
			if (t1 < t2) {
				if (t2 < tmax)
					tmax = t2;
			} else {
				if (t1 < tmax)
					tmax = t1;
			}
		}
		if (dir.y != 0.0) {
			t1 = (ymin - orig.y) / dir.y;
			t2 = (ymax - orig.y) / dir.y;
			if (t1 < t2) {
				if (t2 < tmax)
					tmax = t2;
			} else {
				if (t1 < tmax)
					tmax = t1;
			}
		}
		if (dir.z != 0.0) {
			t1 = (zmin - orig.z) / dir.z;
			t2 = (zmax - orig.z) / dir.z;
			if (t1 < t2) {
				if (t2 < tmax)
					tmax = t2;
			} else {
				if (t1 < tmax)
					tmax = t1;
			}
		}

		// Meterlo dentro del nodo
		Vector3d nextPos = new Vector3d(orig.x + dir.x * tmax, orig.y + dir.y
				* tmax, orig.z + dir.z * tmax);
		nextPos.x += (dir.x > 0.0 ? OctreeScene.TOLERANCE
				: -OctreeScene.TOLERANCE);
		nextPos.y += (dir.y > 0.0 ? OctreeScene.TOLERANCE
				: -OctreeScene.TOLERANCE);
		nextPos.z += (dir.z > 0.0 ? OctreeScene.TOLERANCE
				: -OctreeScene.TOLERANCE);

		// Subir por el octree hasta encontrar un nodo que lo contenga
		current = parent;
		while (!current.contains(nextPos)) {
			current = current.parent;
			if (current == null)
				return null;
		}

		// Ahora volver a bajar por el octree hasta encontrar un nodo terminal
		while (current.obj == null) {
			if (nextPos.x > current.midx) {
				if (nextPos.y > current.midy) {
					if (nextPos.z > current.midz)
						current = current.child[7];
					else
						current = current.child[6];
				} else {
					if (nextPos.z > current.midz)
						current = current.child[5];
					else
						current = current.child[4];
				}
			} else {
				if (nextPos.y > current.midy) {
					if (nextPos.z > current.midz)
						current = current.child[3];
					else
						current = current.child[2];
				} else {
					if (nextPos.z > current.midz)
						current = current.child[1];
					else
						current = current.child[0];
				}
			}
		}
		return current;
	}

	/**
	 * Este método debería ser invocado sobre la raíz del octree.
	 * 
	 * @return Nodo terminal que contiene el punto por donde pasa un rayo
	 *         proveniente desde afuera de este nodo. (Sino null)
	 */
	public OctreeNode findFirstNode(Ray r) {
		double t1, t2, tmin = -Double.MAX_VALUE, tmax = Double.MAX_VALUE;
		Vector3d orig = r.position, dir = r.direction;

		// Encontrar (si existe) el punto donde el rayo ingresa al nodo
		if (dir.x == 0.0) {
			if (orig.x < xmin || orig.x > xmax)
				return null;
		} else {
			t1 = (xmin - orig.x) / dir.x;
			t2 = (xmax - orig.x) / dir.x;
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
			if (tmin > tmax || tmax < 0.0)
				return null;
		}
		if (dir.y == 0.0) {
			if (orig.y < ymin || orig.y > ymax)
				return null;
		} else {
			t1 = (ymin - orig.y) / dir.y;
			t2 = (ymax - orig.y) / dir.y;
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
			if (tmin > tmax || tmax < 0.0)
				return null;
		}
		if (dir.z == 0.0) {
			if (orig.z < zmin || orig.z > zmax)
				return null;
		} else {
			t1 = (zmin - orig.z) / dir.z;
			t2 = (zmax - orig.z) / dir.z;
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
			if (tmin > tmax || tmax < 0.0)
				return null;
		}

		// Meterlo dentro del nodo
		tmin += OctreeScene.TOLERANCE;
		Vector3d nextPos = new Vector3d(orig.x + dir.x * tmin, orig.y + dir.y
				* tmin, orig.z + dir.z * tmin);

		// Devolver el nodo terminal que contiene el punto
		return findNode(nextPos);
	}

	/** Setea los puntos medios del nodo. */
	private void findMidpoints(BoundingBox objBounds[]) {
		// TODO: La eficiencia del octree se puede mejorar si estos puntos
		// medios no se setean por la mitad de la BoundingBox sino en planos
		// otros planos tomados convenientemente.
		Vector3d size = getSize();
		midx = size.x / 2;
		midy = size.y / 2;
		midz = size.z / 2;
	}

}