/*
 * Copyright 2013 Hannes Janetzek
 * Copyright 2016 devemux86
 * Copyright 2019 Gustl22
 *
 * This file is part of the OpenScienceMap project (http://www.opensciencemap.org).
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.oscim.renderer;

import org.oscim.backend.AssetAdapter;
import org.oscim.backend.GL;
import org.oscim.backend.GLAdapter;

import java.nio.IntBuffer;
import java.util.logging.Logger;

import static org.oscim.backend.GLAdapter.gl;

public abstract class GLShader {
    private static final Logger log = Logger.getLogger(GLShader.class.getName());

    public int program;

    protected boolean create(String vertexSource, String fragmentSource) {
        return createVersioned(vertexSource, fragmentSource, null);
    }

    protected boolean createDirective(String vertexSource, String fragmentSource, String directives) {
        program = createProgramDirective(vertexSource, fragmentSource, directives);
        return program != 0;
    }

    protected boolean createVersioned(String vertexSource, String fragmentSource, String version) {
        program = createProgramDirective(vertexSource, fragmentSource, version == null ? null : ("#version " + version + "\n"));
        return program != 0;
    }

    protected boolean create(String fileName) {
        return createDirective(fileName, null);
    }

    protected boolean createDirective(String fileName, String directives) {
        program = loadShaderDirective(fileName, directives);
        return program != 0;
    }

    protected boolean createVersioned(String fileName, String version) {
        program = loadShaderDirective(fileName, version == null ? null : ("#version " + version + "\n"));
        return program != 0;
    }

    protected int getAttrib(String name) {
        int loc = gl.getAttribLocation(program, name);
        if (loc < 0)
            log.fine("missing attribute: " + name);
        return loc;
    }

    protected int getUniform(String name) {
        int loc = gl.getUniformLocation(program, name);
        if (loc < 0)
            log.fine("missing uniform: " + name);
        return loc;
    }

    public boolean useProgram() {
        return GLState.useProgram(program);
    }

    public static int loadShader(String file) {
        return loadShaderDirective(file, null);
    }

    public static int loadShaderDirective(String file, String directives) {
        String path = "shaders/" + file + ".glsl";
        String vs = AssetAdapter.readTextFile(path);

        if (vs == null)
            throw new IllegalArgumentException("shader file not found: " + path);

        // TODO ...
        int fsStart = vs.indexOf('$');
        if (fsStart < 0 || vs.charAt(fsStart + 1) != '$')
            throw new IllegalArgumentException("not a shader file " + path);

        String fs = vs.substring(fsStart + 2);
        vs = vs.substring(0, fsStart);

        int shader = createProgramDirective(vs, fs, directives);
        if (shader == 0) {
            System.out.println(vs + " \n\n" + fs);
        }
        return shader;
    }

    public static int loadShader(int shaderType, String source) {

        int shader = gl.createShader(shaderType);
        if (shader != 0) {
            gl.shaderSource(shader, source);
            gl.compileShader(shader);
            IntBuffer compiled = MapRenderer.getIntBuffer(1);

            gl.getShaderiv(shader, GL.COMPILE_STATUS, compiled);
            compiled.position(0);
            if (compiled.get() == 0) {
                log.severe("Could not compile shader " + shaderType + ":");
                log.severe(gl.getShaderInfoLog(shader));
                gl.deleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    public static int createProgram(String vertexSource, String fragmentSource) {
        return createProgramDirective(vertexSource, fragmentSource, null);
    }

    public static int createProgramDirective(String vertexSource, String fragmentSource, String directives) {
        String defs = "";
        if (directives != null)
            defs += directives + "\n";
        if (GLAdapter.GDX_DESKTOP_QUIRKS)
            defs += "#define DESKTOP_QUIRKS 1\n";
        else
            defs += "#define GLES 1\n";

        defs += "#define GLVERSION " + (GLAdapter.isGL30() ? "30" : "20") + "\n";

        int vertexShader = loadShader(GL.VERTEX_SHADER, defs + vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        int pixelShader = loadShader(GL.FRAGMENT_SHADER, defs + fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = gl.createProgram();
        if (program != 0) {
            GLUtils.checkGlError(GLShader.class.getName() + ": glCreateProgram");
            gl.attachShader(program, vertexShader);
            GLUtils.checkGlError(GLShader.class.getName() + ": glAttachShader");
            gl.attachShader(program, pixelShader);
            GLUtils.checkGlError(GLShader.class.getName() + ": glAttachShader");
            gl.linkProgram(program);
            IntBuffer linkStatus = MapRenderer.getIntBuffer(1);
            gl.getProgramiv(program, GL.LINK_STATUS, linkStatus);
            linkStatus.position(0);
            if (linkStatus.get() != GL.TRUE) {
                log.severe("Could not link program: ");
                log.severe(gl.getProgramInfoLog(program));
                gl.deleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
}
