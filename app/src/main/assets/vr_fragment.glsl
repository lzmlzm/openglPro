#version 300 es
precision mediump float;
precision mediump sampler2D;
layout(location = 0) out vec4 fragColor;
layout(location = 0) uniform sampler2D u_texture;
in vec2 v_textureCoordinate;
void main() {
    fragColor = texture(u_texture, v_textureCoordinate);
}