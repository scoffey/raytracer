package raytracer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import objects.Material;
import objects.SceneObject;
import objects.Sphere;
import objects.TriangleSet;

import org.web3d.j3d.loaders.X3DLoader;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DLightNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.core.J3DMetadataFloat;
import org.web3d.vrml.renderer.j3d.nodes.core.J3DMetadataSet;
import org.web3d.vrml.renderer.j3d.nodes.core.J3DWorldRoot;
import org.web3d.vrml.renderer.j3d.nodes.geom3d.J3DSphere;
import org.web3d.vrml.renderer.j3d.nodes.group.J3DTransform;
import org.web3d.vrml.renderer.j3d.nodes.navigation.J3DViewpoint;
import org.web3d.vrml.renderer.j3d.nodes.render.J3DCoordinate;
import org.web3d.vrml.renderer.j3d.nodes.render.J3DIndexedTriangleFanSet;
import org.web3d.vrml.renderer.j3d.nodes.render.J3DIndexedTriangleSet;
import org.web3d.vrml.renderer.j3d.nodes.render.J3DIndexedTriangleStripSet;
import org.web3d.vrml.renderer.j3d.nodes.render.J3DTriangleSet;
import org.web3d.vrml.renderer.j3d.nodes.shape.J3DAppearance;
import org.web3d.vrml.renderer.j3d.nodes.shape.J3DMaterial;
import org.web3d.vrml.renderer.j3d.nodes.shape.J3DShape;

import scene.PointLight;
import scene.Scene;
import scene.Transformation;

/**
 * Clase que dado el nombre de un archivo x3d, lo parsea y genera la escena
 * correspondiente. Para utilizarla, invocar al método <code>loadScene</code>
 * con un nombre de archivo x3d, esto retorna la escena construida para ser
 * pasada al ray tracer.
 */
public class SceneLoader {

	/**
	 * Carga un archivo x3d, y construye el objeto <code>Scene</code> para que
	 * pueda ser renderado por el ray tracer.
	 * 
	 * @param fileName Nombre del archivo a parsear.
	 * @return La escena construida.
	 * @throws IOException Si hay algun problema para parsear el archivo.
	 */
	public Scene loadScene(String fileName) throws IOException {
		X3DLoader loader = new X3DLoader(X3DLoader.LOAD_ALL);
		try {
			loader.load(fileName);
		} catch (Exception e) {
			throw new IOException(e.getMessage()); // , e);
		}
		VRMLNode node = loader.getVRMLScene().getRootNode();
		Scene scene = new Scene();
		parseScene(node, scene);
		return scene;
	}

	/**
	 * Dado el nodo raíz de un X3D y una escena vacía, navega a través de los
	 * nodos y construye y agrega todos los objetos necesarios a la escena.
	 * 
	 * @param node Nodo raíz del x3d.
	 * @param scene Escena la cual agregarle los nodos.
	 */
	private void parseScene(VRMLNode node, Scene scene) {
		switch (node.getPrimaryType()) {
		case TypeConstants.ViewpointNodeType:
			parseViewpointNode((J3DViewpoint) node, scene);
			break;
		case TypeConstants.LightNodeType:
			parseLightNode((J3DLightNode) node, scene);
			break;
		case TypeConstants.GroupingNodeType:
			if (node instanceof J3DTransform) {
				parseTransformNode((J3DTransform) node, scene);
			}
			break;
		case TypeConstants.WorldRootNodeType:
			for (VRMLNode v : ((J3DWorldRoot) node).getChildren()) {
				parseScene(v, scene);
			}
		}
	}

	/**
	 * Dado un nodo viewpoint construye el objeto <code>Camera</code>
	 * correspondiente y lo agrega la escena. Levanta la posición, la
	 * orientación y la rotación.
	 * 
	 * @param viewpoint Nodo viewpoint que contiene la camara.
	 * @param scene Escena a la cual setearle la cámara.
	 */
	private void parseViewpointNode(J3DViewpoint viewpoint, Scene scene) {
		VRMLFieldData data;
		Vector3d position = new Vector3d(0, 0, 0);
		AxisAngle4d orientation = new AxisAngle4d(0, 0, 1, 0);
		if ((data = getField(viewpoint, "position")) != null) {
			position = new Vector3d(new Vector3f(data.floatArrayValue));
		}
		if ((data = getField(viewpoint, "orientation")) != null) {
			orientation = new AxisAngle4d(new AxisAngle4f(data.floatArrayValue));
		}
		scene.setCamera(new Camera(position, orientation, viewpoint
				.getFieldOfView()));
	}

	/**
	 * Procesa un nodo transform. Toma la escala, la rotacion y la traslacion, y
	 * la propaga a los objetos que se definan dentro de el, a traves de un
	 * objeto <code>Transform</code>.
	 * 
	 * @param transformNode Nodo de tipo transform del x3d.
	 * @param scene Escena que se está construyendo.
	 */
	private Set<Object> parseTransformNode(J3DTransform transformNode, Scene scene) {
		Set<Object> children = new HashSet<Object>();
		
		Transformation transform = new Transformation();
		transform.rotation = new AxisAngle4d(new AxisAngle4f(transformNode
				.getRotation()));
		transform.translation = new Vector3d(new Vector3f(transformNode
				.getTranslation()));
		transform.scale = new Vector3d(new Vector3f(transformNode.getScale()));
		for (VRMLNode node : transformNode.getChildren()) {
			if (node instanceof J3DShape) {
				J3DShape shapeNode = (J3DShape) node;
				SceneObject object = parseShapeNode(shapeNode);
				if (object != null) {
					object.transform(transform);
					scene.addSceneObject(object);
					children.add(object);
				} else {
					System.err.println("Unsupported shape geometry: "
							+ shapeNode.getGeometry().getVRMLNodeName());
				}
			} else if (node instanceof J3DViewpoint) {
				parseViewpointNode((J3DViewpoint)node, scene);
				scene.getCamera().transform(transform);
				children.add(scene.getCamera());
			} else if (node instanceof J3DTransform) {
				Set<Object> group = parseTransformNode((J3DTransform)node, scene);
				for (Object obj : group) {
					if (obj instanceof SceneObject) {
						((SceneObject)obj).transform(transform);
					} else if (obj instanceof Camera) {
						((Camera)obj).transform(transform);
					}
				}
				children.addAll(group);
			}
		}
		
		return children;
	}

	/**
	 * Procesa un nodo shape. Calcula la posición y las dimensiones a partir del
	 * objeto transform que recibe, que contiene las tranformaciones acumuladas
	 * hasta el momento para la forma a procesar.
	 * 
	 * @param shapeNode Nodo de tipo shape a procesar.
	 * @param transform Transformaciones acumuladas hasta el momento.
	 */
	private SceneObject parseShapeNode(J3DShape shapeNode) {
		SceneObject shape = null;
		VRMLNode geometry = shapeNode.getGeometry();

		if (geometry instanceof J3DSphere) {
			shape = parseSphere((J3DSphere) geometry);
		} else if (geometry instanceof J3DTriangleSet) {
			shape = parseTriangleSet((J3DTriangleSet) geometry);
		} else if (geometry instanceof J3DIndexedTriangleFanSet) {
			shape = parseTriangleSet((J3DIndexedTriangleFanSet) geometry);
		} else if (geometry instanceof J3DIndexedTriangleSet) {
			shape = parseTriangleSet((J3DIndexedTriangleSet) geometry);
		} else if (geometry instanceof J3DIndexedTriangleStripSet) {
			shape = parseTriangleSet((J3DIndexedTriangleStripSet) geometry);
		}

		// Figura no reconocida
		if (shape == null) {
			return null;
		}

		J3DAppearance appearance = (J3DAppearance) shapeNode.getAppearance();
		if (appearance != null) {
			J3DMaterial material = (J3DMaterial) appearance.getMaterial();
			if (material != null) {
				shape.getMaterial().set(parseMaterial(material));
			}
		}
		return shape;
	}

	private SceneObject parseSphere(J3DSphere sphereNode) {
		VRMLFieldData data;
		Sphere sphere = new Sphere();
		data = getField(sphereNode, "radius");
		if (data != null) {
			sphere.radius = data.floatValue;
		}
		return sphere;
	}

	private SceneObject parseTriangleSet(J3DTriangleSet triangleSetNode) {
		TriangleSet triangleSet = new TriangleSet();
		for (VRMLNodeType node : triangleSetNode.getComponents()) {
			if (node instanceof J3DCoordinate) {
				J3DCoordinate coordinate = (J3DCoordinate) node;
				float[] points = coordinate.getPointRef();
				for (int i = 0; i < points.length; i += 9) {
					Vector3d p1 = new Vector3d(points[i], points[i + 1],
							points[i + 2]);
					Vector3d p2 = new Vector3d(points[i + 3], points[i + 4],
							points[i + 5]);
					Vector3d p3 = new Vector3d(points[i + 6], points[i + 7],
							points[i + 8]);
					triangleSet.addTriangle(p1, p2, p3);
				}
			}
		}
		return triangleSet;
	}

	private Vector3d getIndexedPoint(float[] points, int i)
			throws IndexOutOfBoundsException {
		int j = 3 * i;
		if (!(j >= 0 && j + 2 < points.length)) {
			throw new IndexOutOfBoundsException("No triangle coords at index "
					+ j);
		}
		return new Vector3d(points[j], points[j + 1], points[j + 2]);
	}

	private SceneObject parseTriangleSet(J3DIndexedTriangleSet setNode) {
		TriangleSet triangleSet = new TriangleSet();
		VRMLNode[] components = setNode.getComponents();

		// Coordenadas
		J3DCoordinate coordinates = (J3DCoordinate) components[0];
		float[] points = new float[coordinates.getNumPoints()];
		coordinates.getPoint(points);

		// Puntos
		int[] indexes = setNode.getFieldValue(setNode.getFieldIndex("index")).intArrayValue;
		for (int i = 0; i < indexes.length; i += 3) {
			try {
				Vector3d p1 = getIndexedPoint(points, indexes[i]);
				Vector3d p2 = getIndexedPoint(points, indexes[i + 1]);
				Vector3d p3 = getIndexedPoint(points, indexes[i + 2]);
				triangleSet.addTriangle(p1, p2, p3);
				//triangleSet.addTriangle(p1, p3, p2);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}

		return triangleSet;
	}

	private SceneObject parseTriangleSet(J3DIndexedTriangleFanSet setNode) {
		TriangleSet triangleSet = new TriangleSet();
		VRMLNode[] components = setNode.getComponents();

		// Coordenadas
		J3DCoordinate coordinates = (J3DCoordinate) components[0];
		float[] points = new float[coordinates.getNumPoints()];
		coordinates.getPoint(points);

		// Puntos
		int[] indexes = setNode.getFieldValue(setNode.getFieldIndex("index")).intArrayValue;
		Vector3d p1 = getIndexedPoint(points, indexes[0]);
		Vector3d p2 = getIndexedPoint(points, indexes[1]);
		for (int i = 2; i < indexes.length; i++) {
			try {
				if (indexes[i] == -1) {
					if (i + 2 < indexes.length) {
						p1 = getIndexedPoint(points, indexes[i + 1]);
						p2 = getIndexedPoint(points, indexes[i + 2]);
						i += 2;
					}
				} else {
					Vector3d p3 = getIndexedPoint(points, indexes[i]);
					triangleSet.addTriangle(p1, p2, p3);
					//triangleSet.addTriangle(p1, p3, p2);
					p2 = p3;
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}

		return triangleSet;
	}

	private SceneObject parseTriangleSet(J3DIndexedTriangleStripSet setNode) {
		TriangleSet triangleSet = new TriangleSet();
		VRMLNode[] components = setNode.getComponents();

		// Coordenadas
		J3DCoordinate coordinates = (J3DCoordinate) components[0];
		float[] points = new float[coordinates.getNumPoints()];
		coordinates.getPoint(points);

		// Puntos
		int[] indexes = setNode.getFieldValue(setNode.getFieldIndex("index")).intArrayValue;
		Vector3d p1 = getIndexedPoint(points, indexes[0]);
		Vector3d p2 = getIndexedPoint(points, indexes[1]);
		for (int i = 2; i < indexes.length; i++) {
			try {
				if (indexes[i] == -1) {
					if (i + 2 < indexes.length) {
						p1 = getIndexedPoint(points, indexes[i + 1]);
						p2 = getIndexedPoint(points, indexes[i + 2]);
						i += 2;
				}
				} else {
					Vector3d p3 = getIndexedPoint(points, indexes[i]);
					triangleSet.addTriangle(p1, p2, p3);
					//triangleSet.addTriangle(p1, p3, p2);
					p1 = p2;
					p2 = p3;
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		}

		return triangleSet;
	}

	/**
	 * @param material
	 * @param shape
	 */
	private Material parseMaterial(J3DMaterial material) {
		Material m = new Material();
		// Material Data
		m.diffuseColor = new Vector3d(new Vector3f(material.getDiffuseColor()));
		m.specularColor = new Vector3d(
				new Vector3f(material.getSpecularColor()));
		m.ambientIntensity = material.getAmbientIntensity();
		m.transparency = material.getTransparency();
		m.shininess = material.getShininess();
		
		// Material Metadata
		J3DMetadataSet metadata = (J3DMetadataSet) material.getMetadataObject();
		if (metadata != null) {
			VRMLFieldData data = metadata.getFieldValue(metadata
					.getFieldIndex("value"));
			VRMLNode[] metadataNodes = data.nodeArrayValue;
			for (int i = 0; i < metadataNodes.length; i++) {
				J3DMetadataFloat f = (J3DMetadataFloat) metadataNodes[i];
				VRMLFieldData d = f.getFieldValue(f.getFieldIndex("value"));
				if (f.getName().equals("reflection")) {
					m.reflectionIndex = d.floatArrayValue[0];
				} else if (f.getName().equals("refraction")) {
					m.refractionIndex = d.floatArrayValue[0];
				} else if (f.getName().equals("diffuse")) {
					m.diffuseIndex = d.floatArrayValue[0];
				} else if (f.getName().equals("specular")) {
					m.specularIndex = d.floatArrayValue[0];
				}
			}
		}
		return m;
	}

	/**
	 * Procesa un nodo de tipo luz. Levanta la ubicacion y el color, crea el
	 * objeto point light y lo agrega a la escena.
	 * 
	 * @param node Nodo de tipo luz a procesar.
	 * @param scene Escena a la cual agregarle la luz creada.
	 */
	private void parseLightNode(J3DLightNode node, Scene scene) {
		PointLight light = new PointLight();
		VRMLFieldData data = getField(node, "location");
		light.setColor(new Vector3d(new Vector3f(node.getColor())));
		if (data != null) {
			light.setPosition(new Vector3d(new Vector3f(data.floatArrayValue)));
		}
		if ((data = getField(node, "on")) != null) {
			if (!data.booleanValue)
				return;
		}
		if ((data = getField(node, "radio")) != null) {
			light.setRadio(data.floatValue);
		}
		if ((data = getField(node, "attenuation")) != null) {
			light.setAttenuation(data.floatArrayValue);
		}
		scene.addLight(light);
	}

	/**
	 * Obtiene un campo de un nodo vrml. Si el campo no existe retorna null.
	 * 
	 * @param node Nodo en el cual se está buscando un campo.
	 * @param field Nombre del campo buscado.
	 * @return El nodo data en caso de que el campo exista, null en caso
	 *         contrario.
	 */
	private VRMLFieldData getField(J3DVRMLNode node, String field) {
		for (int i = 0; i < node.getNumFields(); i++) {
			VRMLFieldDeclaration declaration = node.getFieldDeclaration(i);
			if (declaration != null && declaration.getName().equals(field)) {
				return node.getFieldValue(i);
			}
		}
		return null;
	}
}
