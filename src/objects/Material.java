package objects;

import javax.vecmath.Vector3d;

public class Material {

	/** Color que refleja el objeto cuando le pega un rayo. */
	public Vector3d diffuseColor = new Vector3d(0.8, 0.8, 0.8);

	/** Color con el que refleja los rayos especulares. */
	public Vector3d specularColor = new Vector3d(1, 1, 1);

	/** Índice de cuánto emite de su color al ser iluminado. */
	public double diffuseIndex = 1;

	/** Índice de cuánto refleja los rayos que le llegan. */
	public double specularIndex = 0.5;

	/** Intensidad que refleja aunque no le pegue ningún rayo. */
	public double ambientIntensity = 0.2;

	/** Índice de cuán transparente es el objeto. */
	public double transparency = 0;

	/** Qué proporción del rayo es refractado. */
	public double refractionIndex = 0;

	/** Qué proporción del rayo es reflejado. */
	public double reflectionIndex = 0;

	/** Brillo del objeto. */
	public double shininess = 0.2;
	
	@Override
	public String toString() {
		return "Material(diffuseColor=" + diffuseColor + ", specularColor="
				+ specularColor + ", diffuseIndex=" + diffuseIndex
				+ ", specularIndex=" + specularIndex + ", ambientIntensity="
				+ ambientIntensity + ", transparency=" + transparency
				+ ", refractionIndex=" + refractionIndex + ", reflectionIndex="
				+ reflectionIndex + ")";
	}

	public void set(Material m) {
		this.diffuseColor = m.diffuseColor;
		this.specularColor = m.specularColor;
		this.diffuseIndex = m.diffuseIndex;
		this.specularIndex = m.specularIndex;
		this.ambientIntensity = m.ambientIntensity;
		this.transparency = m.transparency;
		this.refractionIndex = m.refractionIndex;
		this.reflectionIndex = m.reflectionIndex;
		this.shininess = m.shininess;
	}

}
