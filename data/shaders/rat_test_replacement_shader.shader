
//Test shader that applies a shader to an onslaught to overlap it with the XIV sprite

uniform sampler2D tex;
uniform sampler2D replacementTex;

vec2 texCoord = gl_TexCoord[0].xy;

uniform vec2 resolution;
uniform float alphaMult;
uniform vec3 colorMix;

uniform float minRadius;
uniform float maxRadius;

void main() {

	vec2 uv = texCoord / resolution;

	vec4 color = texture2D(tex, texCoord);
	vec4 replacementColor = texture2D(replacementTex, texCoord);

	float brightness = (color.r + color.g + color.b) / 3;


	float dist = distance(uv, vec2(0.5, 0.5));
	if (dist > minRadius && dist < maxRadius) {
		color = replacementColor;
	}

	//color.r = brightness;
	//color.g = brightness;
	//color.b = brightness;

	//As the Shader Overwrites the effect of glColor, apply the "color" variable from the sprite like this (i.e color.r * sprite.color.r / 255)
	color.r *= colorMix.r;
	color.g *= colorMix.g;
	color.b *= colorMix.b;

	//Need to apply the change 
	color.a *= alphaMult;


	gl_FragColor = color;

}