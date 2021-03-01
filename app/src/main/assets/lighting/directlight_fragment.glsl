#version 300 es

precision mediump float;

layout(location = 0) out vec4 fragColor;

in vec2 texCoord;
in vec3 fragPos;
in vec3 normal;

//材质
uniform sampler2D imageTex;
uniform sampler2D normalTex;
uniform vec3 lightDirection;
//观察者坐标
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;

float myPow(float x, int r) {
    float result = 1.0;
    for (int i = 0; i < r; i = i + 1) {
        result = result * x;
    }
    return result;
}

void main()
{

    vec3 texColor = texture(imageTex, texCoord).rgb;

    // ambient
    //环境光计算：光源颜色*光强系数（0.5）
    float ambientStrength = 0.5;
    vec3 ambient = ambientStrength * lightColor;

    // diffuse
    //漫反射的法向量
    //标准化法向量
    vec3 norm = normalize(normal);
    //标准化光源位置
    vec3 lightDir = normalize(-lightDirection);
    //点成光方向向量和物体法向量
    //如果两个向量之间的角度大于90度，点乘的结果就会变成负数，这样会导致漫反射分量变为负数。
    //为此，我们使用max函数返回两个参数之间较大的参数，从而保证漫反射分量不会变成负数。负数颜色的光照是没有定义的，所以最好避免它
    float diff = max(dot(norm, lightDir), 0.0);
    //得到漫反射分量
    vec3 diffuse = diff * lightColor;

    // specular镜面光照
    float specularStrength = 2.0;
    //观察者坐标-片段坐标=观察者方向向量
    vec3 viewDir = normalize(viewPos - fragPos);

    //对lightDir向量进行了取反。reflect函数要求第一个向量是从光源指向片段位置的向量，但是lightDir当前正好相反，
    //是从片段指向光源（由先前我们计算lightDir向量时，减法的顺序决定）。
    //为了保证我们得到正确的reflect向量，我们通过对lightDir向量取反来获得相反的方向。第二个参数要求是一个法向量，所以我们提供的是已标准化的norm向量。
    vec3 reflectDir = reflect(-lightDir, norm);

    //计算视线方向与反射方向的点乘（并确保它不是负值），然后取它的16次幂。这个16是高光的反光度(Shininess)
    //一个物体的反光度越高，反射光的能力越强，散射得越少，高光点就会越小
    float spec = myPow(max(dot(viewDir, reflectDir), 0.0), 32);
    //计算镜面分量
    vec3 specular = specularStrength * spec * lightColor;

    //最后一件事情是把它加到环境光分量和漫反射分量里，再用结果乘以物体的颜色
    vec3 result = (ambient + diffuse + specular) * texColor;
    fragColor = vec4(result, 1.0);
}