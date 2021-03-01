#version 300 es

precision mediump float;
//JAVA input apostion aTexCoord aNormal
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

//output to fragment
out vec3 fragPos;
out vec2 texCoord;
out vec3 normal;

//MVP
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    texCoord = aTexCoord;
    //计算模型放入世界坐标后的位置
    fragPos = vec3(model * vec4(aPos, 1.0));
    //计算漫反射的法向量
    normal = mat3(transpose(inverse(model))) * aNormal;
    //计算gl位置  MVP
    gl_Position = projection * view * vec4(fragPos, 1.0);
}