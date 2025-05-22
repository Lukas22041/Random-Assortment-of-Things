

uniform sampler2D tex;
vec2 texCoord = gl_TexCoord[0].xy;

uniform float saturation;
uniform float colorMult;

void main() {
	vec4 col = texture2D(tex, texCoord);


	float sat = -1.0+saturation;

	float brightness = (col.r + col.g + col.b) / 3.0;
    float rd = brightness - col.r;
    float gd = brightness - col.g;
    float bd = brightness - col.b;
    col.r += rd * -sat;
    col.g += gd * -sat;
    col.b += bd * -sat;

	col.r *= 1.0;
	col.g *= 1.0;
	col.b *= 1.0;

	gl_FragColor = col;

}