

uniform sampler2D tex;
uniform float iTime;

vec2 texCoord = gl_TexCoord[0].xy;

const float offset = 0.5 / 128.0;

void main() {
	vec4 col = texture2D(tex, texCoord);
	vec2 st = texCoord;

	if (col.a > 0.5)
		gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
	else {
		float a = texture2D(tex, vec2(texCoord.x + offset, texCoord.y)).a +
			texture2D(tex, vec2(texCoord.x, texCoord.y - offset)).a +
			texture2D(tex, vec2(texCoord.x - offset, texCoord.y)).a +
			texture2D(tex, vec2(texCoord.x, texCoord.y + offset)).a;
		if (col.a < 1.0 && a > 0.0)
			gl_FragColor = vec4(0.5 + 0.5 * st.x, 0.5 + 0.5 * st.y, 0.5 + 0.5 * st.x, 0.5);
		else
			gl_FragColor = col;
	}

}