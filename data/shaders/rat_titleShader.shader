

uniform sampler2D tex;
vec2 texCoord = gl_TexCoord[0].xy;

uniform float iTime;
uniform float noise;

void main() {
	vec4 col = texture2D(tex, texCoord);

	vec2 offset = vec2(noise * 0.05, 0.0);

	col.g = texture2D(tex, texCoord + offset).g;
	col.r = texture2D(tex, texCoord).r;
	col.b = texture2D(tex, texCoord - offset).b;

	col.r *= 1.25;
	col.g *= 0.75;
	col.b *= 0.75;

	

	gl_FragColor = col;

}