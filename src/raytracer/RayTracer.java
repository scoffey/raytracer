package raytracer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import objects.Material;
import objects.SceneObject;

import scene.Intersection;
import scene.PointLight;
import scene.Scene;

/**
 * Clase que implementa el algoritmo principal de ray tracing. A través del constructor se
 * especifican los parámetros, y luego se obtiene la imagen generada a partir del método render.
 */
public class RayTracer {

	/** Maxima cantidad de niveles en la recursion de getColor */
	private static final int MAX_LEVELS = 10;

	/** Margen de error para comparar doubles. */
	private static final double EPSILON_EQUALS = 0.000000000001;

	/** Escena que se quiere renderear. */
	private Scene scene;

	/** Tamaño de la imagen a generar. */
	private Dimension imageSize;

	/** Parámetro de antialiasing. */
	private int antialiasing = 1;

	/** Parámetro de penumbras. */
	private int shadow;

	/**
	 * Crea un nuevo ray tracer con los parámetros dados. Le setea a la cámara el tamaño de la
	 * imagen a generar para que pueda construir los rayos.
	 * 
	 * @param scene Escena que se quiere renderear.
	 * @param imageSize Tamaño de la imagen a generar.
	 * @param antialiasing Parámetro de antialiasing (puede ser null).
	 * @param shadow Parámetro de penumbra (puede ser null).
	 */
	public RayTracer(Scene scene, Dimension imageSize, int antialiasing, int shadow) {
		super();
		this.scene = scene;
		this.antialiasing = antialiasing;
		this.shadow = shadow;
		this.imageSize = imageSize;
	}

	/**
	 * Realiza el rendering de la escena provista en el constructor.
	 * 
	 * @param showProgress Flag que indica si se debe mostrar el progreso en pantalla.
	 * @return La imagen generada.
	 */
	public BufferedImage render(boolean showProgress) {
		BufferedImage image = new BufferedImage(imageSize.width, imageSize.height,
				BufferedImage.TYPE_INT_RGB);
		if (showProgress) {
			for (int i = 0; i < 80; i++) {
				System.out.print('-');
			}
			System.out.print("\n");
		}
		for (int i = 0; i < imageSize.height; i++) {
			for (int j = 0; j < imageSize.width; j++) {
				if (showProgress
						&& (i * imageSize.width + j) % (imageSize.width * imageSize.height / 80) == 0) {
					System.out.print('*');
				}
				SceneObject intersected, lastIntersected = null;
				Ray ray = constructRayThroughPixel(i, j);
				Vector3d color = new Vector3d();
				intersected = getColor(ray, 0, scene.getCamera().position, color, 1);
				if (intersected != lastIntersected) {
					lastIntersected = intersected;
					color.set(0, 0, 0);
					for (int m = -(antialiasing / 2); m <= antialiasing / 2; m++) {
						for (int n = -(antialiasing / 2); n <= antialiasing / 2; n++) {
							Vector3d colorAcum = new Vector3d();
							Ray antialiasRay = constructRayThroughPixel(i, j, m, n);
							getColor(antialiasRay, 0, scene.getCamera().position, colorAcum, 1);
							color.add(colorAcum);
						}
					}
					color.scale(1.0 / (antialiasing * antialiasing));
				}
				image.setRGB(j, i, new Color((float) color.x, (float) color.y, (float) color.z)
						.getRGB());
			}
		}
		System.out.print("\n");
		return image;
	}

	/**
	 * Calcula el color que es reflejado por un rayo en la escena. Se llama recursivamente para los
	 * reflejos, incrementando el contador currentLevel en cada nivel de la recursión. Recibe la
	 * ubicación del viewer para la parte de especularidad. En la primera invocación va a ser
	 * (0,0,0) ya que es la ubicación de la cámara, pero luego será el punto de intersección de cada
	 * rayo, para ir calculando la especularidad recursivamente.
	 * 
	 * @param ray Rayo disparado.
	 * @param currentLevel Nivel actual de la recursion. Invocarlo la primera vez con 0.
	 * @param viewerPosition Posicion del observador. En la primera invocacion es el origen.
	 * @param color Parámetro de salida con el color encontrado en el pixel.
	 * @param currentRefraction Índice de refracción del medio actual.
	 * @return El primer objeto intersectado (puede ser null).
	 */
	private SceneObject getColor(Ray ray, int currentLevel, Vector3d viewerPosition,
			Vector3d color, double currentRefraction) {

		if (currentLevel > MAX_LEVELS) {
			color.set(new double[] { 0, 0, 0 });
			return null;
		}
		Intersection intersection = new Intersection();
		SceneObject intersectedObject = scene.getFirstIntersectedObject(ray, intersection);
		if (intersectedObject == null) {
			color.set(new double[] { 0, 0, 0 });
			return null;
		}
		Material material = intersectedObject.getMaterial();
		double nShiny = material.shininess * 128.0;
		
		Vector3d ambientIntensity = new Vector3d(1, 1, 1);
		Vector3d specularIntensity = new Vector3d(0, 0, 0);
		Vector3d diffuseIntensity = new Vector3d(0, 0, 0);

		for (PointLight light : scene.getLights()) {

			Ray lightRay;
			Vector3d lightPosition, lightDirection;

			/* Calcular la shade de la luz. */
			double shade = 0;
			for (int i = 0; i < shadow; i++) {
				lightPosition = new Vector3d(light.getPosition());
				if (shadow > 1) {
					lightPosition.x += (Math.random() - 1) * light.getRadio();
					lightPosition.y += (Math.random() - 1) * light.getRadio();
					lightPosition.z += (Math.random() - 1) * light.getRadio();
				}

				lightDirection = new Vector3d(intersection.point);
				lightDirection.sub(lightPosition);

				lightRay = new Ray(lightPosition, lightDirection);

				Intersection lightIntersection = new Intersection();
				SceneObject lightIntersectedObject = scene.getFirstIntersectedObject(lightRay,
						lightIntersection);
				if (lightIntersectedObject == null
						|| !lightIntersectedObject.equals(intersectedObject)
						|| !lightIntersection.point.epsilonEquals(intersection.point,
								EPSILON_EQUALS)) {
					continue;
				}
				shade += 1.0 / shadow;
			}
			if (shade < EPSILON_EQUALS) {
				continue;
			}

			/* Calcular el rayo posta para las cuentas. */
			lightDirection = new Vector3d(intersection.point);
			lightDirection.sub(light.getPosition());
			lightRay = new Ray(light.getPosition(), lightDirection);

			/* Calcular término especular. */
			Ray lightReflectedRay = reflectRay(lightRay, intersectedObject, intersection, 0.005);
			Vector3d specular = new Vector3d(viewerPosition);
			specular.sub(intersection.point);
			specular.normalize();
			double aux = specular.dot(lightReflectedRay.direction);
			if (aux < 0) {
				aux = 0;
			}
			double specularTerm = material.specularIndex * Math.pow(aux, nShiny);
			if (specularTerm < 0.001) {
				specularTerm = 0;
			}
			Vector3d lightIntensity = light.getColor(intersection.point);
			lightIntensity.scale(specularTerm);
			specularIntensity.add(lightIntensity);

			/* Calcular término de difusión. */
			Vector3d diffuse = new Vector3d(intersection.normal);
			diffuse.normalize();
			// lightRay.direction.scale(-1);
			double diffuseTerm = diffuse.dot(lightRay.direction) * material.diffuseIndex;
			if (diffuseTerm < 0) {
				diffuseTerm = 0;
			}
			lightIntensity = light.getColor(intersection.point);
			lightIntensity.scale(diffuseTerm * shade);
			diffuseIntensity.add(lightIntensity);
		}

		/* Calcular los colores que aporta cada luz. */
		ambientIntensity.scale((float) material.ambientIntensity);
		Util.multiplyVectors(ambientIntensity, material.diffuseColor);
		Util.multiplyVectors(diffuseIntensity, material.diffuseColor);
		Util.multiplyVectors(specularIntensity, material.specularColor);

		ambientIntensity.add(diffuseIntensity);
		ambientIntensity.add(specularIntensity);

		/* El resultado final queda en ambientIntensity. */
		Util.cropVector(ambientIntensity);

		/* Reflexion */
		if (material.reflectionIndex > 0)
			computeReflection(ray, intersectedObject, intersection, ambientIntensity, currentLevel,
					currentRefraction);

		/* Refracción */
		if (material.transparency > 0)
			computeRefraction(ray, intersectedObject, intersection, ambientIntensity, currentLevel,
					currentRefraction);

		/* Color final */
		Util.cropVector(ambientIntensity);
		color.set(ambientIntensity);

		return intersectedObject;
	}

	private void computeReflection(Ray ray, SceneObject intersectedObject,
			Intersection intersection, Vector3d ambientIntensity, int currentLevel,
			double currentRefraction) {
		Ray reflectedRay = reflectRay(ray, intersectedObject, intersection, 0.00001);
		Vector3d reflectedColor = new Vector3d();
		if (getColor(reflectedRay, currentLevel + 1, reflectedRay.position, reflectedColor,
				currentRefraction) != null) {

			Material material = intersectedObject.getMaterial();
			Util.multiplyVectors(reflectedColor, material.diffuseColor);
			reflectedColor.scale(material.reflectionIndex);
			ambientIntensity.add(reflectedColor);
		}
	}

	private void computeRefraction(Ray ray, SceneObject intersectedObject,
			Intersection intersection, Vector3d ambientIntensity, int currentLevel,
			double currentRefraction) {
		Ray refractedRay = refractRay(ray, intersectedObject, intersection, 0.00001,
				currentRefraction);
		if (refractedRay != null) {

			// TODO
			if (refractedRay.direction.dot(intersection.normal) > 0) {
				currentRefraction = intersectedObject.getMaterial().refractionIndex;
			} else {
				currentRefraction = 1;
			}

			Vector3d refractedColor = new Vector3d();
			SceneObject instersectedObject2 = getColor(refractedRay, currentLevel + 1,
					refractedRay.position, refractedColor, currentRefraction);
			if (instersectedObject2 != null) {

				Vector3d pointOfIntersection = instersectedObject2.intersectsRay(refractedRay).point;
				pointOfIntersection.sub(intersection.point);
				double distancia = intersection.distance;

				Material material = intersectedObject.getMaterial();
				Vector3d absorbance = new Vector3d(material.diffuseColor);
				absorbance.scale(-0.15 * distancia);
				Vector3d transparency = new Vector3d(Math.exp(absorbance.x),
						Math.exp(absorbance.y), Math.exp(absorbance.z));

				Util.multiplyVectors(refractedColor, transparency);
				refractedColor.scale(material.transparency);
				ambientIntensity.add(refractedColor);
			}
		}
	}

	/**
	 * Construye un rayo que sale de la cámara y pasa por el pixel (i,j) del plano de la imagen.
	 * 
	 * @param i Fila del pixel a atravesar.
	 * @param j Columna del pixel a atravesar.
	 * @return Un rayo que sale de la cámara y pasa por dicho pixel.
	 */
	public Ray constructRayThroughPixel(int i, int j) {
		double xDir = (j - imageSize.width / 2f);
		double yDir = (i - imageSize.height / 2f);
		double zDir = (double) (
				Math.min(imageSize.width, imageSize.height)
				/ (2 * Math.tan(scene.getCamera().fieldOfView / 2)));
		Vector4d dir = new Vector4d(xDir, -yDir, -zDir, 1);
		dir.normalize();
		Vector4d result = Util.MultiplyMatrixAndVector(scene.getCamera().rotationMatrix,
				dir);
		Vector3d direction = new Vector3d(result.x, result.y, result.z);
		direction.normalize();
		return new Ray(scene.getCamera().position, direction);
	}

	/**
	 * Construye un rayo para antialiasing que sale de la cámara y pasa por el pixel (i,j) del plano
	 * de la imagen. En ese pixel construye una grilla con el parámetro de antialiasing, y hace que
	 * el rayo pase por el elemento (m, n) de dicha grilla.
	 * 
	 * @param i Fila del pixel a atravesar.
	 * @param j Columna del pixel a atravesar.
	 * @param m Fila de la grilla construida sobre el pixel.
	 * @param n Columna de la grilla construida sobre el pixel.
	 * @return Un rayo que sale de la cámara y pasa por dicho pixel.
	 */
	public Ray constructRayThroughPixel(int i, int j, int m, int n) {
		double xDir = antialiasing * (j - imageSize.width / 2f);
		double yDir = antialiasing * (i - imageSize.height / 2f);
		double zDir = antialiasing * (double) (
				Math.min(imageSize.width, imageSize.height)
				/ (2 * Math.tan(scene.getCamera().fieldOfView / 2)));

//		double zDir = antialiasing
//				* (double) (Math.sqrt(Math.pow(imageSize.width, 2) + Math.pow(imageSize.height, 2)) / (2 * Math
//						.tan(scene.getCamera().fieldOfView)));

		xDir += Util.randomBetween(n, n + 1);
		yDir += Util.randomBetween(m, m + 1);

		Vector4d result = Util.MultiplyMatrixAndVector(scene.getCamera().rotationMatrix,
				new Vector4d(xDir, -yDir, -zDir, 1));
		Vector3d direction = new Vector3d(result.x, result.y, result.z);
		direction.normalize();
		return new Ray(scene.getCamera().position, direction);

	}

	/**
	 * Calcula el reflejo de un rayo sobre un objeto. Utiliza la normal del objeto en el punto
	 * intersectado para construir el reflejo, y lo retorna.
	 * 
	 * @param ray Rayo que impacta en el objeto.
	 * @param intersectedObject Objeto siendo impactado por el rayo.
	 * @param pointOfIntersection Punto en el que el rayo toca el objeto.
	 * @param delta Cuánto se debe desplazar el origen del rayo en el sentido de su dirección.
	 * @return Reflejo del rayo en ese punto.
	 */
	private Ray reflectRay(Ray ray, SceneObject intersectedObject, Intersection intersection,
			double delta) {
		Vector3d direction = new Vector3d(intersection.normal);
		double aux = direction.dot(ray.direction) * -1;
		direction.scale(2 * aux);
		direction.add(ray.direction);
		Vector3d position = new Vector3d(direction);
		position.scale(delta);
		position.add(intersection.point);
		return new Ray(position, direction);
	}

	/**
	 * Calcula la refracción un rayo a través de un objeto.
	 * 
	 * @param ray Rayo que impacta en el objeto.
	 * @param intersectedObject Objeto siendo impactado por el rayo.
	 * @param pointOfIntersection Punto en el que el rayo toca el objeto.
	 * @param delta Cuánto se debe desplazar el origen del rayo en el sentido de la normal.
	 * @return Refracción del rayo en ese punto.
	 */
	private Ray refractRay(Ray ray, SceneObject intersectedObject, Intersection intersection,
			double delta, double currentRefraction) {

		Material material = intersectedObject.getMaterial();
		double rindex = material.refractionIndex;
		double n = currentRefraction / rindex;
		Vector3d normal = new Vector3d(intersection.normal);

		if (normal.dot(ray.direction) > 0) {
			normal.scale(-1);
		}

		double cosI = -normal.dot(ray.direction);
		double cosT2 = 1.0 - n * n * (1.0 - cosI * cosI);
		if (cosT2 > 0) {
			Vector3d direction = new Vector3d(ray.direction);
			direction.scale(n);
			normal.scale(n * cosI - Math.sqrt(cosT2));
			direction.add(normal);
			direction.normalize();

			Vector3d position = new Vector3d(direction);
			position.scale(delta);
			position.add(intersection.point);

			return new Ray(position, direction);
		}
		return null;

	}
}
