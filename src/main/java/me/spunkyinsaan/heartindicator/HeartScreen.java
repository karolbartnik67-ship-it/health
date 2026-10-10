package me.spunkyinsaan.heartindicator;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Bez przycisków: łapiesz napis myszką i przesuwasz, a kwadracik ze strzałką w prawym dolnym rogu
 * ciągniesz w różne strony - napis się powiększa albo pomniejsza.
 */
public class HeartScreen extends Screen {
    private static final int HANDLE = 10;

    private boolean moving = false, resizing = false;
    private double grabDx, grabDy;

    public HeartScreen() {
        super(Text.literal("Heart Indicator"));
    }

    /** rozmiar napisu przy skali 1.0 */
    private int[] base() {
        Config c = Config.INSTANCE;
        return HudRenderer.size(client, "Nick", "2/10", c.showName, c.showAbsorption, "+2");
    }

    /** x, y, szerokość, wysokość (po przeskalowaniu) */
    private int[] box() {
        Config c = Config.INSTANCE;
        int[] s = base();
        return new int[]{Math.round(c.posX * this.width), Math.round(c.posY * this.height),
                Math.round(s[0] * c.scale), Math.round(s[1] * c.scale)};
    }

    private int[] handle(int[] b) {
        return new int[]{b[0] + b[2] + 2, b[1] + b[3] + 2};
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        Config c = Config.INSTANCE;
        int[] b = box();

        ctx.fill(b[0] - 3, b[1] - 3, b[0] + b[2] + 3, b[1] + b[3] + 3, moving ? 0x6655AAFF : 0x44FFFFFF);
        HudRenderer.draw(ctx, client, b[0], b[1], c.scale, "Nick", 4f, 20f, 4f);

        // kwadracik ze strzałką ↘ (pomniejsza / powiększa)
        int[] h = handle(b);
        int hx = h[0], hy = h[1];
        ctx.fill(hx, hy, hx + HANDLE, hy + HANDLE, resizing ? 0xFF55AAFF : 0xFFCCCCCC);
        ctx.fill(hx, hy, hx + HANDLE, hy + 1, 0xFF000000);
        ctx.fill(hx, hy + HANDLE - 1, hx + HANDLE, hy + HANDLE, 0xFF000000);
        ctx.fill(hx, hy, hx + 1, hy + HANDLE, 0xFF000000);
        ctx.fill(hx + HANDLE - 1, hy, hx + HANDLE, hy + HANDLE, 0xFF000000);
        for (int i = 2; i <= 7; i++) ctx.fill(hx + i, hy + i, hx + i + 1, hy + i + 1, 0xFF000000);   // przekątna
        for (int i = 3; i <= 7; i++) {
            ctx.fill(hx + 7, hy + i, hx + 8, hy + i + 1, 0xFF000000);                                 // grot
            ctx.fill(hx + i, hy + 7, hx + i + 1, hy + 8, 0xFF000000);
        }

        ctx.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Przykładowy napis (w grze: serca trafionego gracza) • złap i przesuń • ciągnij kwadracik ↘ aby zmienić rozmiar • Esc = gotowe")
                        .formatted(Formatting.GRAY),
                this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0) return super.mouseClicked(mx, my, button);
        int[] b = box();
        int[] h = handle(b);
        if (in(mx, my, h[0] - 2, h[1] - 2, HANDLE + 4, HANDLE + 4)) {
            resizing = true;
            return true;
        }
        if (in(mx, my, b[0] - 3, b[1] - 3, b[2] + 6, b[3] + 6)) {
            moving = true;
            grabDx = mx - b[0];
            grabDy = my - b[1];
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        Config c = Config.INSTANCE;
        if (resizing) {
            // ciągnięcie od lewego górnego rogu napisu: w prawo/dół = większy, w lewo/górę = mniejszy
            int[] s = base();
            double bx = c.posX * this.width, by = c.posY * this.height;
            double sx = (mx - bx) / s[0];
            double sy = (my - by) / s[1];
            double ns = (sx + sy) / 2.0;
            c.scale = (float) Math.max(0.5, Math.min(6.0, ns));
            return true;
        }
        if (moving) {
            int[] b = box();
            double nx = Math.max(0, Math.min(this.width - b[2] - HANDLE - 4, mx - grabDx));
            double ny = Math.max(0, Math.min(this.height - b[3] - HANDLE - 4, my - grabDy));
            c.posX = (float) (nx / this.width);
            c.posY = (float) (ny / this.height);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (moving || resizing) {
            moving = false;
            resizing = false;
            Config.save();
            return true;
        }
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        Config c = Config.INSTANCE;
        c.scale = Math.max(0.5f, Math.min(6f, c.scale + (float) Math.signum(vertical) * 0.25f));
        Config.save();
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
