uniform sampler2D tex;

// Center of the circle in *UV* space (RTT UV, same as gl_TexCoord)
uniform vec2 centerUV;

// Radius of the circle in *screen pixels*
uniform float radiusPx;

// Screen size in pixels
uniform float screenWidth;
uniform float screenHeight;

// Visible portion of the RTT texture (from ShaderLib)
uniform float visibleU;
uniform float visibleV;

uniform float intensity;

void main() {
    vec2 texCoord = gl_TexCoord[0].xy;

    // Convert UV delta to *screen pixels*
    // texCoord.x spans [0, visibleU] over [0, screenWidth]
    // texCoord.y spans [0, visibleV] over [0, screenHeight]
    vec2 dUV = texCoord - centerUV;

    vec2 dPx = vec2(
        dUV.x * (screenWidth  / visibleU),
        dUV.y * (screenHeight / visibleV)
    );

    float distPx = length(dPx);

    vec4 col = texture2D(tex, texCoord);

    if (distPx <= radiusPx) {
        float b = (col.r + col.g + col.b) / 3.0;

        col.r *= 1.0 - 0.1 * intensity;
        col.g *= 1.0 - 0.1 * intensity;
        col.b *= 1.0 - 0.1 * intensity;

        col.r += b * 0.4 * intensity;
        col.g += b * 0.0 * intensity;
        col.b += b * 0.75 * intensity;
    }

    gl_FragColor = col;
}
