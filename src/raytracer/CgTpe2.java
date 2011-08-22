package raytracer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


import scene.OctreeScene;
import scene.Scene;

/**
 * Clase principal que se encarga de parsear la línea de comandos, invocar al loader de la escena, y
 * luego al ray tracer.
 */
public class CgTpe2 {

	private boolean optionProgress = false;
	private int optionAntialiasing = 1;
	private String optionOutputFile;
	private String optionInputFile;
	private int optionWidth = 400;
	private int optionHeight = 300;
	private boolean optionShow;
	private int optionShadow = 1;

	public static void main(String[] args) {
		CgTpe2 cgtpe2 = new CgTpe2();
		try {
			cgtpe2.parseOptions(args);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			printUsage();
			return;
		}

		try {
			cgtpe2.renderScene();
		} catch (IOException e) {
			System.err.println("Error rendering scene: " + e.getMessage());
		}

		if (!cgtpe2.optionShow) {
			System.exit(0);
		}
	}

	public static void printUsage() {
		System.err.println("Usage: raytracer -i <input_file> [options]");
		// TODO: Agregar las opciones
		return;
	}

	/**
	 * Parsea los argumentos de la línea de comandos y setea los valores de las variables de
	 * instancia correspondientes.
	 * 
	 * @param args Arreglo de argumentos recibido por main.
	 * @throws IllegalArgumentException Si hay errores en la línea de comandos.
	 */
	public void parseOptions(String[] args) throws IllegalArgumentException {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-progress")) {
				optionProgress = true;
			} else if (args[i].startsWith("-a")) {
				if (args[i].length() > 2 && args[i].charAt(2) != 's') {
					throw new IllegalArgumentException("Unsupported antialiasing flag: "
							+ args[i].charAt(i));
				}
				try {
					String s = args[i].substring(3);
					if (s.length() == 0)
						s = (i + 1 < args.length ? args[++i] : "1");
					optionAntialiasing = Integer.parseInt(s);
					if (optionAntialiasing % 2 != 1) {
						optionAntialiasing++;
						//throw new IllegalArgumentException("Antialiasing parameter should be an odd number.");
					}
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid antialiasing value: "
							+ args[i].substring(3));
				}
			} else if (args[i].equals("-o")) {
				if (i + 1 < args.length)
					optionOutputFile = args[++i];
			} else if (args[i].equals("-i")) {
				if (i + 1 < args.length)
					optionInputFile = args[++i];
			} else if (args[i].equals("-s") || args[i].equals("-size")) {
				String[] parts;
				if (i + 1 < args.length) {
					parts = args[++i].split("x");
				} else {
					parts = new String(optionWidth + "x" + optionHeight).split("x");
				}
				if (parts.length != 2) {
					throw new IllegalArgumentException("Invalid image size: " + args[i]);
				}
				try {
					optionWidth = Integer.parseInt(parts[0]);
					optionHeight = Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid image size: " + args[i]);
				}
			} else if (args[i].equals("-show")) {
				optionShow = true;
			} else if (args[i].startsWith("-p")) {
				try {
					String s = args[i].substring(2);
					if (s.length() == 0)
						s = (i + 1 < args.length ? args[++i] : "1");
					optionShadow = Integer.parseInt(s);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid shadow parameter: " + args[i]);
				}
			} else {
				throw new IllegalArgumentException("Invalid option: " + args[i]);
			}
		}
		if (optionInputFile == null) {
			throw new IllegalArgumentException("No input file supplied.");
		}
		String[] inputFileParts = optionInputFile.split("\\.");
		if (!inputFileParts[inputFileParts.length - 1].equals("x3d")) {
			throw new IllegalArgumentException("Input file must have .x3d extension.");
		}
		if (optionOutputFile == null) {
			optionOutputFile = optionInputFile.replaceAll("\\.x3d", ".png");
		}
		return;
	}

	/**
	 * Hace el rendering de la escena. Levanta el archivo a través del loader, crea el objeto ray
	 * tracer, lo invoca con la escena generada y luego guarda la imagen creada.
	 * 
	 * @throws IOException Si hay errores en los accesos a los archivos de entrada o salida.
	 */
	public void renderScene() throws IOException {
		SceneLoader sceneLoader = new SceneLoader();
		Scene scene = sceneLoader.loadScene(optionInputFile);
		//scene.dumpScene();
		Dimension imageSize = new Dimension(optionWidth, optionHeight);
		RayTracer rayTracer = new RayTracer(new OctreeScene(scene), imageSize, optionAntialiasing, optionShadow);
		//RayTracer rayTracer = new RayTracer(scene, imageSize, optionAntialiasing, optionShadow);
		BufferedImage result = rayTracer.render(optionProgress);
		if (optionShow) {
			new RenderViewer(result);
		} else {
			try {
				String[] parts = optionOutputFile.split("\\.");
				ImageIO.write(result, parts[parts.length - 1], new File(optionOutputFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
