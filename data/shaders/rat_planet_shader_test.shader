
//Test shader that applies a shader to an onslaught to overlap it with the XIV sprite

uniform sampler2D tex;
uniform sampler2D noiseTex;

vec2 texCoord = gl_TexCoord[0].xy;

uniform float alphaMult;

void main() {

	vec4 color = texture2D(tex, texCoord);
	vec4 noise = texture2D(noiseTex, texCoord);

	float brightness = (color.r + color.g + color.b) / 3;
	float noiseBrightness = (noise.r + noise.g + noise.b) / 3;

	//color.r = brightness;
	//color.g = brightness;
	//color.b = brightness;

	color.r += 1 * (1-noiseBrightness);


	color.a *= alphaMult;

	//color.a = 0.5;

	gl_FragColor = color;

}