

uniform sampler2D screen;
uniform sampler2D distortion;

vec2 screenCoord = gl_TexCoord[0].xy;
vec2 distortCoord = gl_TexCoord[1].xy;


void main() {

	vec4 distortCol = texture2D(distortion, distortCoord);
	vec4 screenCol = texture2D(screen, screenCoord);

	if (distortCoord.x == 0.0) {
		screenCol.r = 0.0;
	}


	gl_FragColor = screenCol;

}