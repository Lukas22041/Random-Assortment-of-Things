
//Test shader that applies a shader to an onslaught to overlap it with the XIV sprite

uniform sampler2D tex;
uniform sampler2D overlayTex;

vec2 texCoord = gl_TexCoord[0].xy;

uniform vec2 resolution;
uniform float alphaMult;
uniform vec3 colorMix;

void main() {

	vec2 uv = texCoord / resolution;

	vec4 color = texture2D(tex, texCoord);
	vec4 overlayColor = texture2D(overlayTex, texCoord);

	float brightness = (color.r + color.g + color.b) / 3;
	float overlayBrightness = (overlayColor.r + overlayColor.g + overlayColor.b) / 3;

	float ogAlpha = color.a;

	color.r *= 1;
	color.g *= 0.9;
	color.b *= 0.75;

	color = mix(color, overlayColor, 0.75);
	color.a = ogAlpha * overlayBrightness * 2;

	//color.r *= overlayBrightness * 2;
	//color.g *= overlayBrightness* 2;
	//color.b *= overlayBrightness* 2;

	//color.r -= 0.5 * overlayBrightness;
	//color.g -= 0.5 * overlayBrightness;
	//color.b -= 0.5 * overlayBrightness;



	//color.r *= overlayColor.r + brightness;
	//color.g *= overlayColor.g + brightness;
	//color.b *= overlayColor.b + brightness;

	//As the Shader Overwrites the effect of glColor, apply the "color" variable from the sprite like this (i.e color.r * sprite.color.r / 255)
	color.r *= colorMix.r;
	color.g *= colorMix.g;
	color.b *= colorMix.b;

	//Need to apply the change 
	color.a *= alphaMult;



	gl_FragColor = color;

}