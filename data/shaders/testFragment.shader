

uniform sampler2D tex;
uniform float iTime;

vec2 texCoord = gl_TexCoord[0].xy;

void main() {
	vec4 col = texture2D(tex, texCoord);


	if (col.a > 0.0) {
		col.g = col.g * (2.0) * (texCoord.y*iTime);
		col.b = col.b * (2.0) * (texCoord.y*iTime);
	}


	gl_FragColor = col;

}