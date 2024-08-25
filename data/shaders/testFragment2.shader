

uniform sampler2D screen;
uniform sampler2D distortion;

vec2 screenCoord = gl_TexCoord[0].xy;
vec2 distortCoord = gl_TexCoord[1].xy;

void main() {
	vec4 distortCol = texture2D(distortion, distortCoord);

	vec2 distortedUV = screenCoord + distortCol.a * 0.1;
	vec4 screenColor =  texture2D(screen, distortedUV);

	gl_FragColor = screenColor;

}