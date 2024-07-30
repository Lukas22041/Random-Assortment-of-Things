

uniform sampler2D tex;
uniform sampler2D noiseTex1;
uniform sampler2D noiseTex2;
uniform float iTime;
uniform float alphaMult;


vec2 texCoord = gl_TexCoord[0].xy;


void main() {

	float noiseX = texCoord.x - iTime;
	float noiseY = texCoord.y - iTime;
	vec2 offset = vec2(noiseX, noiseY);

	vec4 color = texture2D(tex, texCoord);
	vec4 noiseCol1 = texture2D(noiseTex1, vec2(texCoord.x - iTime, texCoord.y - iTime * 0.5));
	vec4 noiseCol2 = texture2D(noiseTex2, vec2(texCoord.x - iTime, texCoord.y - iTime));

	if (color.a > 0.5) {
		float brigtness1 = (noiseCol1.r + noiseCol1.g + noiseCol1.b) / 3 * 0.3;
		float brigtness2 = (noiseCol2.r + noiseCol2.g + noiseCol2.b) / 3 * 0.5;

		color.r += brigtness1;
		color.b += brigtness2;
	} 

	color.a *= alphaMult;
	gl_FragColor = color;

	//gl_FragColor = noiseCol;

}