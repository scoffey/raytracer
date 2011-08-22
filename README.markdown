Raytracer
=========

This project was an assignment of the Computer Graphics course at [ITBA] [1] in 2008. See copyright notes at the bottom.

It features a **3D scene rendering engine** based in [ray tracing] [2].

Examples of scenes rendered by the raytracer program can be found in the `img` directory of this repository, as well as documentation in the `doc` directory.

  [1]: http://www.itba.edu.ar
  [2]: http://en.wikipedia.org/wiki/Ray_tracing_(graphics)

Features
---------

  - Positionable camera
  - Anti-aliasing through stochastic sampling
  - Point lights
  - Reflection
  - Refraction
  - Penumbra
  - Octrees
  - Bounding volumes
  - Shapes: Sphere, TriangleSet, IndexedTriangleSet, IndexedTriangleStripSet, IndexedTriangleFanSet
  - Material: diffuse and specular color, ambient intensity, transparency, shininess
  - Transform operations: scale, rotation and traslation
  - Partially supports X3D format for input representing 3D scene
  - Supports PNG, JPG, BMP and other image formats for output

File contents
-------------

  - `bin`: Reserved for .class files
  - `build.xml`: Apache Ant build file
  - `COPYING`: GNU General Public License
  - `doc`: Documentation (in Spanish)
  - `img`: Examples of 3D scenes rendered by the raytracer program
  - `lib`: Required external libraries
  - `raytracer.bat`: Script for running the raytracer program on Windows
  - `raytracer.jar`: Main raytracer .jar file
  - `raytracer.sh`: Script for running the raytracer program on POSIX systems
  - `README.markdown`: This README file
  - `src`: Source code, fully written in Java

Program usage
-------------

    java -Djava.library.path=lib -jar raytracer.jar [options]

Options:

  - `-i <filename>`: Input filename (X3D only)
  - `-o <filename>`: Output filename (in any image format supported by Java image libraries)
  - `-s <width>x<height>`: Output image size
  - `-progress`: Show progress bar (rendered pixels out of total pixels) on standard output
  - `-show`: Show a window with output image
  - `-as <N>`: Anti-aliasing parameter (square root of the number of rays traced per pixel)
  - `-p <N>`: Penumbra parameter (number of rays traced per pixel)

Copyright
---------

    Copyright (c) 2008
     - Rafael Martín Bigio <rbigio@itba.edu.ar>
     - Santiago Andrés Coffey <scoffey@itba.edu.ar>
     - Andrés Santiago Gregoire <agregoir@itba.edu.ar>

    The following additional provisions apply to third party software
    included as part of this product:
     - Java3D library: Copyright (c) 1996-2008 Sun Microsystems, Inc.
       Licensed under the GNU General Public License (GPL), version 2,
       with the CLASSPATH exception. See <http://java3d.java.net/>
     - XJ3D library: Copyright (c) 2001-2007 Web3D Consortium.
       Licensed under the GNU LGPL v2.1. See <http://www.xj3d.org/>

    Raytracer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Raytracer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Raytracer.  If not, see <http://www.gnu.org/licenses/>.
