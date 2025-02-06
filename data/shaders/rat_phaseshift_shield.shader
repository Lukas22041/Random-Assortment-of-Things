

uniform sampler2D tex;
uniform sampler2D noiseTex1;
uniform sampler2D noiseTex2;
uniform float iTime;
uniform float alphaMult;
uniform float level;

vec2 texCoord = gl_TexCoord[0].xy;


void main() {

	float noiseX = texCoord.x - iTime;
	float noiseY = texCoord.y - iTime;
	vec2 offset = vec2(noiseX, noiseY);

	vec4 color = texture2D(tex, texCoord);
	vec4 noiseCol1 = texture2D(noiseTex1, vec2(texCoord.x - iTime, texCoord.y - iTime * 0.5));
	vec4 noiseCol2 = texture2D(noiseTex2, vec2(texCoord.x - iTime, texCoord.y - iTime));

	if (color.a > 0.5) {
		float brigtness1 = (noiseCol1.r + noiseCol1.g + noiseCol1.b) / 3 ;
		float brigtness2 = (noiseCol2.r + noiseCol2.g + noiseCol2.b) / 3 ;

		
		float scaled1 = brigtness1 * brigtness1 * brigtness1  * brigtness1;
		float scaled2 = brigtness2 * brigtness2 * brigtness2 * brigtness2 * brigtness2;

		color.r += scaled1 * 4;
		color.g += scaled1 * 2;
		

	

		color.r += scaled2 * 2;
		color.g += scaled2 * 1;
	} else {
		color.a = 0;
	}

	color.a *= alphaMult;
	gl_FragColor = color;

}