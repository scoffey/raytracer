package scene;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;


/**
 * Clase que representa una transformación. Soporta traslaciones, rotaciones y escalados. Puede
 * construir la matriz de transformación a partir de estos vectores.
 */
public class Transformation {

	/** Cantidad que se desplaza en cada eje. */
	public Vector3d translation = new Vector3d(0, 0, 0);

	/** Vector de rotacion (eje y angulo). */
	public AxisAngle4d rotation = new AxisAngle4d(0, 0, 1, 0);

	/** Factor por el que se escala en cada eje. */
	public Vector3d scale = new Vector3d(1, 1, 1);

	/**
	 * Construye una matriz de transformación que contiene todas las transformaciones juntas. Al
	 * multiplicarla con un vector hace la rotación, la traslación y el escalado solicitados.
	 * 
	 * @return La matriz de transformación.
	 */
	public Matrix4d getTransformationMatrix() {
		Matrix4d rotationMatrix = getRotationMatrix();
		Matrix4d translationMatrix = new Matrix4d(1, 0, 0, -translation.x, 0, 1, 0, -translation.y,
				0, 0, 1, -translation.z, 0, 0, 0, 1);
		Matrix4d scaleMatrix = new Matrix4d(scale.x, 0, 0, 0, 0, scale.y, 0, 0, 0, 0, scale.z, 0,
				0, 0, 0, 1);
		rotationMatrix.mul(scaleMatrix);
		translationMatrix.mul(rotationMatrix);
		return translationMatrix;
		/*
		translationMatrix.mul(scaleMatrix);
		translationMatrix.mul(rotationMatrix);
		return translationMatrix;*/
	}

	public Matrix4d getTransformationMatrix(boolean inverseRotation) {
		Matrix4d rotationMatrix = getRotationMatrix();
//		if (inverseRotation) {
//			rotationMatrix.invert();
//		}
		Matrix4d translationMatrix = new Matrix4d(1, 0, 0, -translation.x, 0, 1, 0, -translation.y,
				0, 0, 1, -translation.z, 0, 0, 0, 1);
		if (inverseRotation) {
			translationMatrix.invert();
		}
		Matrix4d scaleMatrix = new Matrix4d(scale.x, 0, 0, 0, 0, scale.y, 0, 0, 0, 0, scale.z, 0,
				0, 0, 0, 1);
		rotationMatrix.mul(scaleMatrix);
		translationMatrix.mul(rotationMatrix);
		return translationMatrix;
//		translationMatrix.mul(scaleMatrix);
//		translationMatrix.mul(rotationMatrix);
//		return translationMatrix;
	}
	
	/**
	 * Construe una matriz de rotación, a partir del campo rotation.
	 * 
	 * @return La matriz de rotación.
	 */
	public Matrix4d getRotationMatrix() {
		Matrix4d ret = new Matrix4d();
		ret.setIdentity();
		ret.set(rotation);
		return ret;
	}
}
