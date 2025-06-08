package assortment_of_things.abyss.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignEngineLayers;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import lunalib.lunaUtil.campaign.LunaCampaignRenderingPlugin;
import org.apache.log4j.Level;
import org.dark.shaders.distortion.DistortionAPI;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.util.ShaderAPI;
import org.dark.shaders.util.ShaderLib;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;


//Based entirely on "DistortionShader" from GraphicsLib by Dark.Revenant.
//Changed to work for Campaign Use
//Mod: https://fractalsoftworks.com/forum/index.php?topic=10982.0
//License: https://creativecommons.org/licenses/by/4.0/
public class RATCampaignDistortionShader implements LunaCampaignRenderingPlugin {
    private static final String DATA_KEY = "shaderlib_DistortionShader";
    private static final Comparator<DistortionAPI> DISTORTIONSIZE = (distortion1, distortion2) -> {
        float distortion1factor = distortion1.getIntensity() * Math.max(distortion1.getSprite().getWidth(), distortion1.getSprite().getHeight());
        float distortion2factor = distortion2.getIntensity() * Math.max(distortion2.getSprite().getWidth(), distortion2.getSprite().getHeight());
        if (distortion1factor > distortion2factor) {
            return -1;
        } else {
            return distortion1factor < distortion2factor ? 1 : 0;
        }
    };
    private static final String SETTINGS_FILE = "GRAPHICS_OPTIONS.ini";
    private boolean enabled = false;
    private final int[] index = new int[4];
    private final int[] indexAux = new int[7];
    private int maxDistortions = 100;
    private int program = 0;
    private int programAux = 0;
    private boolean validated = false;
    private boolean validatedAux = false;

    public static List<DistortionAPI> distortions = new ArrayList<>();

    Object readResolve() {
        distortions = new ArrayList<>();
        return this;
    }

    public static List<DistortionAPI> getDistortions() {
       /* List<DistortionAPI> distortions = (List<DistortionAPI>) Global.getSector().getMemoryWithoutUpdate().get("$rat_distortions");
        if (distortions == null) {
            distortions = new ArrayList<>();
            Global.getSector().getMemoryWithoutUpdate().set("$rat_distortions", distortions);
        }
        return distortions;*/
        return distortions;
    }


    public static void addDistortion(DistortionAPI distortion) {
        ShaderAPI distortionShader = ShaderLib.getShaderAPI(DistortionShader.class);
        if (distortionShader instanceof DistortionShader && distortionShader.isEnabled() && distortion != null) {
            List<DistortionAPI> distortions = getDistortions();
            distortions.add(distortion);
        }
    }

    public static void removeDistortion(DistortionAPI distortion) {
        ShaderAPI distortionShader = ShaderLib.getShaderAPI(DistortionShader.class);
        if (distortionShader instanceof DistortionShader && distortionShader.isEnabled() && distortion != null) {
            List<DistortionAPI> distortions = getDistortions();
            distortions.remove(distortion);
        }
    }

    public RATCampaignDistortionShader() {
        if (ShaderLib.areShadersAllowed() && ShaderLib.areBuffersAllowed()) {
            /*RippleDistortion.pathsSet = false;
            WaveDistortion.pathsSet = false;
            Global.getLogger(DistortionShader.class).setLevel(Level.ERROR);*/

            try {
                this.loadSettings();
            } catch (Exception e) {
                Global.getLogger(DistortionShader.class).log(Level.ERROR, "Failed to load shader settings: " + e.getMessage());
                this.enabled = false;
                return;
            }

            if (this.enabled) {
                String vertShader;
                String fragShader;
                String vertShaderAux;
                String fragShaderAux;
                try {
                    vertShader = Global.getSettings().loadText("data/shaders/distortion/distortion.vert");
                    fragShader = Global.getSettings().loadText("data/shaders/distortion/distortion.frag");
                    vertShaderAux = Global.getSettings().loadText("data/shaders/distortion/2dtangent.vert");
                    fragShaderAux = Global.getSettings().loadText("data/shaders/distortion/2dtangent.frag");
                } catch (IOException var6) {
                    this.enabled = false;
                    return;
                }

                this.program = ShaderLib.loadShader(vertShader, fragShader);
                this.programAux = ShaderLib.loadShader(vertShaderAux, fragShaderAux);
                if (this.program != 0 && this.programAux != 0) {
                    GL20.glUseProgram(this.program);
                    this.index[0] = GL20.glGetUniformLocation(this.program, "tex");
                    this.index[1] = GL20.glGetUniformLocation(this.program, "distort");
                    this.index[2] = GL20.glGetUniformLocation(this.program, "screen");
                    this.index[3] = GL20.glGetUniformLocation(this.program, "norm");
                    GL20.glUniform1i(this.index[0], 0);
                    GL20.glUniform1i(this.index[1], 1);
                    GL20.glUniform4f(this.index[2], (float)ShaderLib.getInternalWidth(), (float)ShaderLib.getInternalHeight(), ShaderLib.getVisibleU(), ShaderLib.getVisibleV());
                    GL20.glUseProgram(0);
                    GL20.glUseProgram(this.programAux);
                    this.indexAux[0] = GL20.glGetUniformLocation(this.programAux, "tex");
                    this.indexAux[1] = GL20.glGetUniformLocation(this.programAux, "facing");
                    this.indexAux[2] = GL20.glGetUniformLocation(this.programAux, "scale");
                    this.indexAux[3] = GL20.glGetUniformLocation(this.programAux, "norm");
                    this.indexAux[4] = GL20.glGetUniformLocation(this.programAux, "flip");
                    this.indexAux[5] = GL20.glGetUniformLocation(this.programAux, "arc");
                    this.indexAux[6] = GL20.glGetUniformLocation(this.programAux, "attwidth");
                    GL20.glUniform1i(this.indexAux[0], 0);
                    GL20.glUseProgram(0);
                    this.enabled = true;
                } else {
                    this.enabled = false;
                }
            }
        } else {
            this.enabled = false;
        }
    }

    public void destroy() {
        if (this.enabled) {
            if (this.program != 0) {
                ByteBuffer countbb = ByteBuffer.allocateDirect(4);
                ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
                IntBuffer count = countbb.asIntBuffer();
                IntBuffer shaders = shadersbb.asIntBuffer();
                GL20.glGetAttachedShaders(this.program, count, shaders);

                for(int i = 0; i < 2; ++i) {
                    GL20.glDeleteShader(shaders.get());
                }

                GL20.glDeleteProgram(this.program);
            }

            if (this.programAux != 0) {
                ByteBuffer countbb = ByteBuffer.allocateDirect(4);
                ByteBuffer shadersbb = ByteBuffer.allocateDirect(8);
                IntBuffer count = countbb.asIntBuffer();
                IntBuffer shaders = shadersbb.asIntBuffer();
                GL20.glGetAttachedShaders(this.programAux, count, shaders);

                for(int i = 0; i < 2; ++i) {
                    GL20.glDeleteShader(shaders.get());
                }

                GL20.glDeleteProgram(this.programAux);
            }

        }
    }

    private void drawDistortion(ViewportAPI viewport) {
        List<DistortionAPI> distortions = getDistortions();
        //List<DistortionAPI> distortions = ((DistortionShader.LocalData)engine.getCustomData().get("shaderlib_DistortionShader")).distortions;
        GL20.glUseProgram(this.programAux);
        GL11.glPushAttrib(1048575);
        if (ShaderLib.useBufferCore()) {
            GL30.glBindFramebuffer(36160, ShaderLib.getAuxiliaryBufferId());
        } else if (ShaderLib.useBufferARB()) {
            ARBFramebufferObject.glBindFramebuffer(36160, ShaderLib.getAuxiliaryBufferId());
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(36160, ShaderLib.getAuxiliaryBufferId());
        }

        GL11.glViewport(0, 0, (int)(Global.getSettings().getScreenWidthPixels() * Display.getPixelScaleFactor()), (int)(Global.getSettings().getScreenHeightPixels() * Display.getPixelScaleFactor()));
        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho((double)viewport.getLLX(), (double)(viewport.getLLX() + viewport.getVisibleWidth()), (double)viewport.getLLY(), (double)(viewport.getLLY() + viewport.getVisibleHeight()), (double)-2000.0F, (double)2000.0F);
        GL11.glMatrixMode(5890);
        GL11.glPushMatrix();
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glColorMask(true, true, true, true);
        GL11.glClear(16384);
        float maxScale = 0.0F;
        float minScale = 0.0F;
        int distortionCount = 0;
        ListIterator<DistortionAPI> iter = distortions.listIterator();

        while(iter.hasNext()) {
            DistortionAPI distortion = (DistortionAPI)iter.next();
            float scale = ShaderLib.unitsToUV(distortion.getIntensity());
            if (scale > maxScale) {
                maxScale = scale;
            }

            if (scale < minScale) {
                minScale = scale;
            }

            ++distortionCount;
            if (distortionCount >= this.maxDistortions) {
                break;
            }
        }

        Vector2f normS = ShaderLib.getTextureDataNormalization(minScale, maxScale);
        iter = distortions.listIterator(distortionCount);
        distortionCount = 0;

        while(iter.hasPrevious()) {
            DistortionAPI distortion = (DistortionAPI)iter.previous();
            Vector2f location = distortion.getLocation();
            SpriteAPI sprite = distortion.getSprite();
            if (location != null && sprite != null /*&& ShaderLib.isOnScreen(location, Math.max(sprite.getHeight(), sprite.getWidth()))*/) {
                float scale = ShaderLib.unitsToUV(Math.max(distortion.getIntensity(), 0.0F));
                GL20.glUniform1f(this.indexAux[1], distortion.getFacing());
                GL20.glUniform1f(this.indexAux[2], scale);
                GL20.glUniform2f(this.indexAux[3], normS.x, normS.y);
                GL20.glUniform1f(this.indexAux[4], distortion.isFlipped() ? -1.0F : 1.0F);
                GL20.glUniform2f(this.indexAux[5], (float)Math.toRadians((double)distortion.getArcStart()), (float)Math.toRadians((double)distortion.getArcEnd()));
                GL20.glUniform1f(this.indexAux[6], (float)Math.toRadians((double)distortion.getArcAttenuationWidth()));
                if (!this.validatedAux) {
                    this.validatedAux = true;
                    GL20.glValidateProgram(this.programAux);
                    if (GL20.glGetProgrami(this.programAux, 35715) == 0) {
                        Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(this.programAux));
                        ShaderLib.exitDraw();
                        this.enabled = false;
                        return;
                    }
                }

                sprite.renderAtCenter(location.x, location.y);
                ++distortionCount;
                if (distortionCount >= this.maxDistortions) {
                    break;
                }
            }
        }

        GL11.glMatrixMode(5888);
        GL11.glPopMatrix();
        GL11.glMatrixMode(5890);
        GL11.glPopMatrix();
        GL11.glMatrixMode(5889);
        GL11.glPopMatrix();
        if (ShaderLib.useBufferCore()) {
            GL30.glBindFramebuffer(36160, 0);
        } else if (ShaderLib.useBufferARB()) {
            ARBFramebufferObject.glBindFramebuffer(36160, 0);
        } else {
            EXTFramebufferObject.glBindFramebufferEXT(36160, 0);
        }

        GL11.glPopAttrib();
        ShaderLib.beginDraw(this.program);
        GL20.glUniform2f(this.index[3], normS.x, normS.y);
        GL13.glActiveTexture(33984);
        GL11.glBindTexture(3553, ShaderLib.getScreenTexture());
        GL13.glActiveTexture(33985);
        GL11.glBindTexture(3553, ShaderLib.getAuxiliaryBufferTexture());
        if (!this.validated) {
            this.validated = true;
            GL20.glValidateProgram(this.program);
            if (GL20.glGetProgrami(this.program, 35715) == 0) {
                Global.getLogger(ShaderLib.class).log(Level.ERROR, ShaderLib.getProgramLogInfo(this.program));
                ShaderLib.exitDraw();
                this.enabled = false;
                return;
            }
        }

        GL11.glDisable(3042);
        ShaderLib.screenDraw(ShaderLib.getScreenTexture(), 33984);
        ShaderLib.exitDraw();
    }

    private void loadSettings() throws IOException, JSONException {
        JSONObject settings = Global.getSettings().loadJSON("GRAPHICS_OPTIONS.ini");
        this.enabled = settings.getBoolean("enableDistortion");
        this.maxDistortions = settings.getInt("maximumDistortions");
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (this.enabled) {
            SectorAPI sector = Global.getSector();
            //List<DistortionAPI> distortions = ((DistortionShader.LocalData)Global.getCombatEngine().getCustomData().get("shaderlib_DistortionShader")).distortions;
            if (!sector.isPaused()) {
                List<DistortionAPI> distortions = getDistortions();
                Iterator<DistortionAPI> iter = distortions.iterator();

                while(iter.hasNext()) {
                    DistortionAPI distortion = (DistortionAPI)iter.next();
                    if (distortion.advance(amount)) {
                        iter.remove();
                    }
                }
            }
        }
    }

    EnumSet<CampaignEngineLayers> layers = EnumSet.of(CampaignEngineLayers.ABOVE);

    @Override
    public EnumSet<CampaignEngineLayers> getActiveLayers() {
        return layers;
    }

    @Override
    public void render(CampaignEngineLayers layer, ViewportAPI viewport) {
        if (this.enabled) {
            //List<DistortionAPI> distortions = ((DistortionShader.LocalData)engine.getCustomData().get("shaderlib_DistortionShader")).distortions;
            List<DistortionAPI> distortions = getDistortions();
            if (!distortions.isEmpty()) {
                Collections.sort(distortions, DISTORTIONSIZE);
                this.drawDistortion(viewport);
            }

        }
    }

}
