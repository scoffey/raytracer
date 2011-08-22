package raytracer;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

public class Util {
	public static Vector4d MultiplyMatrixAndVector(Matrix4d matrix, Vector4d vector) {
		Vector4d result = new Vector4d();
		double[] newValues = new double[4];
		double[] oldValues = new double[4];
		oldValues[0] = vector.getX();
		oldValues[1] = vector.getY();
		oldValues[2] = vector.getZ();
		oldValues[3] = vector.getW();
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				newValues[i] += matrix.getElement(i, j) * oldValues[j];
			}
		}

		result.setX(newValues[0]);
		result.setY(newValues[1]);
		result.setZ(newValues[2]);
		result.setW(newValues[3]);
		return result;
	}

	public static double Norm(Vector3d vector) {
		return Math.sqrt(Math.pow(vector.getX(), 2.0) + Math.pow(vector.getY(), 2.0) + Math.pow(vector.getZ(), 2.0));
	}
	

	/**
	 * Verifica que los tres valores de un vector esten entre 0 y 1. Si alguno es menor que 0, lo
	 * setea en 0. Si alguno es mayor que 1, lo setea en 1.
	 * 
	 * @param v Vector a procesar.
	 */
	public static void cropVector(Vector3d v) {
		v.x = v.x > 1 ? 1 : v.x;
		v.y = v.y > 1 ? 1 : v.y;
		v.z = v.z > 1 ? 1 : v.z;
		v.x = v.x < 0 ? 0 : v.x;
		v.y = v.y < 0 ? 0 : v.y;
		v.z = v.z < 0 ? 0 : v.z;
	}

	/**
	 * Multiplica componente a componente dos vectores, dejando el resultado en el primer vector.
	 * 
	 * @param v1 Vector a multiplicar, y lugar en donde se dejara el resultado.
	 * @param v2 Segundo operando.
	 */
	public static void multiplyVectors(Vector3d v1, Vector3d v2) {
		v1.x = v1.x * v2.x;
		v1.y = v1.y * v2.y;
		v1.z = v1.z * v2.z;
	}

	/** @return Producto escalar entre dos Vector3d */
	public static double dotProduct(Vector3d v1, Vector3d v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z; 
	}
	
	public static double randomBetween(double min, double max) {
		return min + Math.random() * (max - min);
	}
}
