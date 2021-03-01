#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPos;//顶点位置
layout (location = 1) in vec2 aTexCoord;//纹理位置
layout (location = 2) in vec3 aNormal;//漫反射位置
layout (location = 3) in vec3 aOffset;//粒子之间的偏移量
layout (location = 4) in vec3 aParticlesColor;//粒子颜色

out vec3 fragPos;//顶点位置
out vec2 texCoord;//纹理位置
out vec3 normal;

uniform mat4 model;//模型矩阵
uniform mat4 view;//视图矩阵
uniform mat4 projection;//透视矩阵

void main()
{
    texCoord = aTexCoord;//纹理位置
    fragPos = vec3(model * vec4(aPos-vec(0.0,0.95,0.0)+aOffset, 1.0));
    normal = mat3(transpose(inverse(model))) * aNormal;
    gl_Position = projection * view * vec4(fragPos, 1.0);
}