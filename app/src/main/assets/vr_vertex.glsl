#version 300 es
precision mediump float;
layout(location = 0) in vec4 a_position;
layout(location = 1) in vec2 a_textureCoordinate;
layout(location = 2) uniform mat4 u_mvp;
out vec2 v_textureCoordinate;
void main() {
    v_textureCoordinate = a_textureCoordinate;
    gl_Position = u_mvp * a_position;
}
