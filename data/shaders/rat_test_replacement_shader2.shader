
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

	color.r *= overlayColor.r + brightness;
	color.g *= overlayColor.g + brightness;
	color.b *= overlayColor.b + brightness;

	//As the Shader Overwrites the effect of glColor, apply the "color" variable from the sprite like this (i.e color.r * sprite.color.r / 255)
	color.r *= colorMix.r;
	color.g *= colorMix.g;
	color.b *= colorMix.b;

	//Need to apply the change 
	color.a *= alphaMult;

	color.r = 3;
	color.g = 0;
	color.b = 0;
	color.a = 1;


	gl_FragColor = color;

}