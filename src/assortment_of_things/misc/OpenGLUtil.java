package assortment_of_things.misc;

import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

public class OpenGLUtil {

    //Code made by Ruddygreat
    public static void drawTexturedRing(SpriteAPI tex, Vector2f loc, float radius, float thickness, int numPoints, float scrollSpeed, float totalElapsedTime) {

        if (numPoints < 3) return;

        numPoints += 1;

        float[] points = new float[(numPoints * 2) * 2];
        float[] texCoordArray = new float[points.length];

        float totalScroll = (scrollSpeed * totalElapsedTime) / tex.getHeight();
        float angleBetweenPoints = 360f / (numPoints - 1);
        float scrollPerSegment = MathUtils.getDistance(MathUtils.getPointOnCircumference(loc, radius, 0), MathUtils.getPointOnCircumference(loc, radius, angleBetweenPoints)) / tex.getHeight();

        for (int i = 0; i < numPoints; i++) {

            //todo make this use lazylib's fancy method
            Vector2f innerEdge = MathUtils.getPointOnCircumference(loc, radius - (thickness / 2f), angleBetweenPoints * i);
            Vector2f outerEdge = MathUtils.getPointOnCircumference(loc, radius + (thickness / 2f), angleBetweenPoints * i);

            int actualIndex = i * 4;

            points[actualIndex] = innerEdge.x; //blx
            points[actualIndex + 1] = innerEdge.y; //bly

            points[actualIndex + 2] = outerEdge.x; //tlx
            points[actualIndex + 3] = outerEdge.y; //tly

            texCoordArray[actualIndex + 1] = totalScroll + (scrollPerSegment * i); //blTexY
            texCoordArray[actualIndex] = 0; //blTexX

            texCoordArray[actualIndex + 3] = totalScroll + (scrollPerSegment * i); //tlTexX
            texCoordArray[actualIndex + 2] = tex.getTextureWidth(); //tlTexY
        }

        //set up floatBuffers
        FloatBuffer vertices = BufferUtils.createFloatBuffer(points.length);
        FloatBuffer texCoords = BufferUtils.createFloatBuffer(texCoordArray.length);
        vertices.put(points).flip();
        texCoords.put(texCoordArray).flip();

        //do caps
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);

        Misc.setColor(tex.getColor());
        tex.bindTexture();

        //draw the bastard
        glPushClientAttrib(GL_CLIENT_VERTEX_ARRAY_BIT);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, 0, vertices);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glTexCoordPointer(2, 0, texCoords);
        glDrawArrays(GL_QUAD_STRIP, 0, points.length / 2);
        glPopClientAttrib();
    }



}
