package raytracer;

import javax.vecmath.Vector3d;

/**
 * Representación de un rayo. Contiene la posición de donde sale el rayo y su dirección (un vector
 * de norma 1).
 */
public class Ray {

	/** Posición en donde se origina el rayo. */
	public Vector3d position;

	/** Dirección del rayo (vector normalizado) */
	public Vector3d direction;

	public Ray(Vector3d origin, Vector3d direction) {
		super();
		direction.normalize();
		this.position = origin;
		this.direction = direction;
	}

	@Override
	public String toString() {
		return "position: " + position + ", direction: " + direction;
	}
}
