package scene;

import java.util.Arrays;
import java.util.Collection;

import objects.SceneObject;

import raytracer.Ray;

public class OctreeScene extends Scene {

	private OctreeNode rootNode;

	public static final double TOLERANCE = 1e-12;

	public OctreeScene(Scene s) {
		super(s.getObjects(), s.getLights(), s.getCamera());
		buildTree();
	}

	private void buildTree() {
		// Crear el Octree
		SceneObject[] obj = getAllLeafObjects();
		BoundingBox objBounds[] = new BoundingBox[obj.length];
		BoundingBox sceneBounds = getBounds(obj, objBounds);
		rootNode = new OctreeNode(sceneBounds, obj, objBounds);
	}

	/** Encuentra las BoundingBoxes de cada objeto y de toda al escena. */
	private BoundingBox getBounds(SceneObject[] objects,
			BoundingBox objBounds[]) {
		double xmin, xmax, ymin, ymax, zmin, zmax;

		xmin = ymin = zmin = Double.MAX_VALUE;
		xmax = ymax = zmax = -Double.MAX_VALUE;

		for (int i = 0; i < objects.length; i++) {
			objBounds[i] = objects[i].getBounds();
			xmin = Math.min(xmin, objBounds[i].xmin);
			xmax = Math.max(xmax, objBounds[i].xmax);
			ymin = Math.min(ymin, objBounds[i].ymin);
			ymax = Math.max(ymax, objBounds[i].ymax);
			zmin = Math.min(zmin, objBounds[i].zmin);
			zmax = Math.max(zmax, objBounds[i].zmax);
		}
		xmin -= TOLERANCE;
		ymin -= TOLERANCE;
		zmin -= TOLERANCE;
		xmax += TOLERANCE;
		ymax += TOLERANCE;
		zmax += TOLERANCE;

		return new BoundingBox(xmin, xmax, ymin, ymax, zmin, zmax);
	}

	@Override
	public SceneObject getFirstIntersectedObject(Ray ray,
			Intersection intersection) {
		OctreeNode node = rootNode.findFirstNode(ray);
		while (node != null) {
			Collection<SceneObject> objects = Arrays.asList(node.getObjects());
			SceneObject obj = super.getFirstIntersectedObject(ray,
					intersection, objects);
			if (obj != null)
				return obj;
			node = node.findNextNode(ray);
		}
		return null;
	}

}
