package raytracer;

import javax.vecmath.*;

import scene.Transformation;


/**
 * Representación de la cámara. Una cámara está formada por una posición, una orientación (en forma
 * de vector 4d, en donde las primeras 3 componentes determinan un eje, y la cuarta el ángulo que se
 * rota sobre ese eje) y un campo visual. Adicionalmente, al construir una cámara se calcula la
 * matriz de rotación (para aplicarle a los rayos que se construyen), y una matriz de traslacion,
 * que se multiplica con la de rotación para formar la transformationMatrix.
 */
public class Camera {

	/** Posición de la cámara. */
	public Vector3d position;

	/** Orientación. Un eje y un ángulo sobre dicho eje. */
	public AxisAngle4d orientation;

	/** Campo visual horizontal. */
	public double fieldOfView;
	
	/** Matriz de rotación. */
	public Matrix4d rotationMatrix;

	/** Matriz de rotación y traslación, compuestas. */  
	public Matrix4d transformationMatrix;

	/**
	 * Crea una nueva cámara con los parámetros dados.
	 * 
	 * @param position Ubicación de la cámara.
	 * @param orientation Hacia dónde mira la cámara.
	 * @param rotation Rotación de la cámara sobre el eje orientation.
	 * @param fieldOfView ángulo de visión a lo ancho.
	 */
	public Camera(Vector3d position, AxisAngle4d orientation, double fieldOfView) {
		super();
		this.position = position;
		this.fieldOfView = fieldOfView;
		this.orientation = orientation;
		
		Transformation t = new Transformation();
//		t.rotation = orientation;
//		Vector4d o = Util.MultiplyMatrixAndVector(t.getRotationMatrix(), new Vector4d(0, 0, -1, 1));
//		
//		Vector3d orientationAxis = new Vector3d(o.x, o.y, o.z);
//		orientationAxis.normalize();
//		this.orientation = new Vector4d(orientationAxis.x, orientationAxis.y, orientationAxis.z, orientation.w);
//		
		//orientation.angle = Math.PI - orientation.angle;
		this.rotationMatrix = new Matrix4d();
		this.rotationMatrix.setIdentity();
		this.rotationMatrix.set(orientation);
		
		t.translation = this.position;
		this.transformationMatrix = t.getTransformationMatrix();
		this.transformationMatrix.mul(this.rotationMatrix);
		
	}

	public void transform(Transformation t) {
		Vector4d aux = new Vector4d(position.x, position.y, position.z, 1);
		Matrix4d matrix = t.getTransformationMatrix();
		aux = Util.MultiplyMatrixAndVector(matrix, aux);
		this.position.set(aux.x, aux.y, aux.z);
		
		aux = new Vector4d(orientation.x, orientation.y, orientation.z, 1);
		aux = Util.MultiplyMatrixAndVector(matrix, aux);
		
		this.orientation.x = aux.x;
		this.orientation.y = aux.y;
		this.orientation.z = aux.z;		
	}
	
	@Override
	public String toString() {
		return "Camera: position = " + position + ", orientation = " + orientation
				+ ", fieldOfView = " + fieldOfView;
	}
}
