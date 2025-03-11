

uniform sampler2D tex;
vec2 texCoord = gl_TexCoord[0].xy;

uniform vec2 screenLocUV;
uniform float range;
uniform float intensity;

void main() {
	vec4 col = texture2D(tex, texCoord);

	float dist = sqrt(pow(texCoord.x - screenLocUV.x, 2.0) + pow(texCoord.y - screenLocUV.y, 2.0));

	if (dist <= range) {
		float b = (col.r + col.g + col.b) / 3.0;

		col.r *= 1.0 - 0.1 * intensity;
		col.g *= 1.0 - 0.1 * intensity;
		col.b *= 1.0 - 0.1 * intensity;

		col.r += b * 0.4 * intensity;
		col.g += b * 0.0 * intensity;
		col.b += b * 0.75 * intensity;
	} else {
		
	}

	gl_FragColor = col;

}