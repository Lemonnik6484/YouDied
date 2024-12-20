package se.gory_moon.you_died.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import se.gory_moon.you_died.YouDied;

public class DeathSplashScreen extends DeathScreenWrapper {
    private static final ResourceLocation TIMES_FONT = new ResourceLocation(YouDied.MOD_ID, "times");
    private static final Style ROOT_STYLE = Style.EMPTY.withFont(TIMES_FONT);
    private final Component deathTitle;
    private final DeathScreenWrapper deathScreen;

    private long fadeInStart;
    private long fadeOutStart;
    private long fadeInMenuStart;
    private boolean showingMenu;

    private static final ResourceLocation DEATH_TEXTURE = new ResourceLocation(YouDied.MOD_ID, "textures/gui/death_image.png");

    public DeathSplashScreen(DeathScreenWrapper deathScreen) {
        super(deathScreen);
        this.deathScreen = deathScreen;
        this.deathTitle = Component.translatable("you_died.death").setStyle(ROOT_STYLE);
        this.condition = () -> showingMenu;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        long now = Util.getMillis();

        if (fadeInStart == 0L) {
            fadeInStart = now;
        }
        if (fadeOutStart == 0L && fadeInStart + 4000 < now) {
            fadeOutStart = now;
        }
        if (fadeInMenuStart == 0L && fadeInStart + 5300 < now) {
            fadeInMenuStart = now;
            showingMenu = true;
        }

        float zoomIn = Mth.clamp((now - fadeInStart) / 5500.0F, 0.0F, 1.0F);

        float fadeIn = 0;
        float fadeInText = 0;

        if (fadeOutStart == 0L) {
            float fIn = (now - fadeInStart) / 1000.0F;
            fadeIn = Mth.clamp(fIn, 0.0F, 1.0F);
            fadeInText = Mth.clamp(fIn - 0.5F, 0.0F, 1.0F);
        }

        if (fadeOutStart > 0 && fadeInMenuStart == 0L) {
            float fOut = (now - fadeOutStart) / 1000.0F;
            fadeIn = Mth.clamp(1.0F - fOut, 0.0F, 1.0F);
            fadeInText = Mth.clamp(1.3F - fOut, 0.0F, 1.0F);
        }

        if (showingMenu) {
            float fOut = (now - fadeInMenuStart) / 1000.0F;
            fadeIn = Mth.clamp(fOut, 0.0F, 1.0F);
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fadeIn);

        for (GuiEventListener guieventlistener : deathScreen.children()) {
            if (guieventlistener instanceof AbstractWidget) {
                ((AbstractWidget) guieventlistener).setAlpha(fadeIn);
            }
        }
        deathScreen.setAlpha(fadeIn);

        if (!showingMenu) {
            float centerY = this.height / 2f;
            guiGraphics.fillGradient(0, (int) centerY - 45, this.width, (int) centerY - 25, 0x00000000, 0xea000000);
            guiGraphics.fill(0, (int) centerY - 25, this.width, (int) centerY + 25, 0xea000000);
            guiGraphics.fillGradient(0, (int) centerY + 25, this.width, (int) centerY + 45, 0xea000000, 0x00000000);

            int textureWidth = 161;
            int textureHeight = 21;
            int x = (this.width - textureWidth) / 2;
            int y = (int) centerY - (textureHeight / 2);

            RenderSystem.setShaderTexture(0, DEATH_TEXTURE);
            guiGraphics.blit(DEATH_TEXTURE, x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);
        } else {
            int l = Mth.ceil(fadeIn * 255.0F) << 24;
            if ((l & 0xfc000000) != 0) {
                deathScreen.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        }
    }
}
