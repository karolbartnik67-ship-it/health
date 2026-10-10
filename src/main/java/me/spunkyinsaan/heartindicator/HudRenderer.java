package me.spunkyinsaan.heartindicator;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


/**
 * Rysuje na ekranie np. "[serce] 2/10" dla gracza, którego ostatnio uderzyłeś (przez 6 s).
 * Liczby to SERCA (życie / 2), max też w sercach - gracz może mieć np. 15 serc.
 */
public final class HudRenderer {
    private static final Identifier CONTAINER = Identifier.ofVanilla("hud/heart/container");
    private static final Identifier FULL = Identifier.ofVanilla("hud/heart/full");
    private static final Identifier ABSORB = Identifier.ofVanilla("hud/heart/absorbing_full");

    /** po trafieniu gracza wskaźnik jest widoczny tyle ms (każde kolejne trafienie odświeża czas) */
    public static final long SHOW_MS = 6000;

    private static int targetId = -1;
    private static long lastHit = 0;

    private HudRenderer() {}

    /** Wywoływane, gdy uderzasz jakąś encję. Liczy się tylko gracz; trafienie innego gracza przełącza cel. */
    public static void onAttack(Entity target) {
        if (target instanceof PlayerEntity && target != MinecraftClient.getInstance().player) {
            targetId = target.getId();
            lastHit = System.currentTimeMillis();
        }
    }

    // ---------------------------------------------------------------- rysowanie

    public static String fmt(float v) {
        float r = Math.round(v * 2f) / 2f;           // do połowy serca
        return r == (int) r ? String.valueOf((int) r) : String.valueOf(r);
    }

    public static int[] size(MinecraftClient mc, String name, String text, boolean showName, boolean absorb, String absText) {
        int w = 9 + 3 + mc.textRenderer.getWidth(text);
        if (absorb) w += 3 + 9 + 2 + mc.textRenderer.getWidth(absText);
        int h = 9;
        if (showName && name != null) {
            w = Math.max(w, mc.textRenderer.getWidth(name));
            h += 11;
        }
        return new int[]{w, h};
    }

    /** Rysuje wskaźnik z lewym górnym rogiem w (x,y) - współrzędne ekranu, już przeskalowane. */
    public static void draw(DrawContext ctx, MinecraftClient mc, int x, int y, float scale,
                            String name, float health, float maxHealth, float absorption) {
        Config c = Config.INSTANCE;
        String text = fmt(health / 2f) + "/" + fmt(maxHealth / 2f);
        boolean absorb = c.showAbsorption && absorption > 0.01f;
        String absText = "+" + fmt(absorption / 2f);

        ctx.getMatrices().push();
        ctx.getMatrices().translate(x, y, 0);
        ctx.getMatrices().scale(scale, scale, 1f);

        int ty = 0;
        if (c.showName && name != null) {
            ctx.drawText(mc.textRenderer, Text.literal(name), 0, 0, 0xFFFFFFFF, true);
            ty = 11;
        }
        ctx.drawGuiTexture(RenderLayer::getGuiTextured, CONTAINER, 0, ty, 9, 9);
        ctx.drawGuiTexture(RenderLayer::getGuiTextured, FULL, 0, ty, 9, 9);
        int tx = 12;
        ctx.drawText(mc.textRenderer, Text.literal(text), tx, ty + 1, 0xFFFFFFFF, true);
        if (absorb) {
            int ax = tx + mc.textRenderer.getWidth(text) + 3;
            ctx.drawGuiTexture(RenderLayer::getGuiTextured, ABSORB, ax, ty, 9, 9);
            ctx.drawText(mc.textRenderer, Text.literal(absText), ax + 11, ty + 1, 0xFFFFD700, true);
        }
        ctx.getMatrices().pop();
    }

    public static void render(DrawContext ctx) {
        Config c = Config.INSTANCE;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!c.renderingEnabled || mc.player == null || mc.world == null) return;
        if (mc.options.hudHidden || mc.currentScreen != null) return;
        if (targetId == -1 || System.currentTimeMillis() - lastHit > SHOW_MS) return;

        Entity e = mc.world.getEntityById(targetId);
        if (!(e instanceof PlayerEntity shown) || shown.isRemoved()) return;

        int x = Math.round(c.posX * mc.getWindow().getScaledWidth());
        int y = Math.round(c.posY * mc.getWindow().getScaledHeight());
        draw(ctx, mc, x, y, c.scale, shown.getName().getString(),
                shown.getHealth(), shown.getMaxHealth(), shown.getAbsorptionAmount());
    }
}
