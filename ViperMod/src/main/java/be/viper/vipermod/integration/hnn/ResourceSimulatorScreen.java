package be.viper.vipermod.integration.hnn;

import dev.shadowsoffire.hostilenetworks.HostileConfig;
import dev.shadowsoffire.hostilenetworks.HostileNetworks;
import dev.shadowsoffire.hostilenetworks.data.CachedModel;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity.FailureState;
import dev.shadowsoffire.hostilenetworks.util.Color;
import dev.shadowsoffire.placebo.screen.PlaceboContainerScreen;
import dev.shadowsoffire.placebo.screen.TickableText;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * HNN's SimChamberScreen (5.3.3) op dezelfde texture, met een ghost slot onder het model-slot
 * dat de gekozen fabricator-drop toont.
 */
public class ResourceSimulatorScreen extends PlaceboContainerScreen<ResourceSimulatorMenu> {
    public static final int WIDTH = 232;
    public static final int HEIGHT = 230;
    private static final ResourceLocation BASE = new ResourceLocation(HostileNetworks.MODID, "textures/gui/sim_chamber.png");
    private static final ResourceLocation PLAYER = new ResourceLocation(HostileNetworks.MODID, "textures/gui/default_gui.png");
    private static final int GHOST_X = -13, GHOST_Y = 21;

    private final List<TickableText> body = new ArrayList<>(7);
    private FailureState lastFailState = FailureState.NONE;
    private boolean runtimeTextLoaded = false;

    public ResourceSimulatorScreen(ResourceSimulatorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
    }

    private CachedModel model() {
        return new CachedModel(this.menu.getSlot(ResourceSimulatorBlockEntity.SLOT_MODEL).getItem(), 0);
    }

    /** Gekozen drop van het huidige model, of leeg. */
    private ItemStack selectedDrop() {
        CachedModel cModel = this.model();
        int sel = this.menu.getSelectedDrop();
        if (!cModel.isValid() || sel < 0 || sel >= cModel.getModel().fabDrops().size()) return ItemStack.EMPTY;
        return cModel.getModel().fabDrops().get(sel);
    }

    private boolean isHoveringGhost(double mouseX, double mouseY) {
        return this.isHovering(GHOST_X, GHOST_Y, 16, 16, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHoveringGhost(mouseX, mouseY) && (button == 0 || button == 1) && this.model().isValid()) {
            int id = button == 0 ? ResourceSimulatorMenu.BUTTON_NEXT : ResourceSimulatorMenu.BUTTON_PREVIOUS;
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        if (this.isHovering(211, 48, 7, 87, mouseX, mouseY)) {
            List<Component> txt = new ArrayList<>(2);
            txt.add(Component.translatable("hostilenetworks.gui.energy", this.menu.getEnergyStored(), HostileConfig.simPowerCap));
            CachedModel cModel = this.model();
            if (cModel.isValid()) {
                txt.add(Component.translatable("hostilenetworks.gui.cost", cModel.getModel().simCost() + HostileConfig.fabPowerCost));
            }
            gfx.renderComponentTooltip(this.font, txt, mouseX, mouseY);
        }
        else if (this.isHovering(14, 48, 7, 87, mouseX, mouseY)) {
            CachedModel cModel = this.model();
            if (cModel.isValid()) {
                List<Component> txt = new ArrayList<>(1);
                if (cModel.getTier() != cModel.getTier().next()) {
                    txt.add(Component.translatable("hostilenetworks.gui.data", cModel.getData() - cModel.getTierData(), cModel.getNextTierData() - cModel.getTierData()));
                }
                else txt.add(Component.translatable("hostilenetworks.gui.max_data").withStyle(ChatFormatting.RED));
                gfx.renderComponentTooltip(this.font, txt, mouseX, mouseY);
            }
        }
        else if (this.isHoveringGhost(mouseX, mouseY) && this.menu.getCarried().isEmpty()) {
            List<Component> txt = new ArrayList<>();
            ItemStack ghost = this.selectedDrop();
            if (ghost.isEmpty()) {
                txt.add(Component.translatable(this.model().isValid() ? "vipermod.hnn.no_drops" : "vipermod.hnn.no_model").withStyle(ChatFormatting.GRAY));
            }
            else {
                txt.addAll(Screen.getTooltipFromItem(this.minecraft, ghost));
                txt.add(Component.translatable("vipermod.hnn.selected_drop").withStyle(ChatFormatting.GOLD));
                txt.add(Component.translatable("vipermod.hnn.cycle").withStyle(ChatFormatting.GRAY));
            }
            gfx.renderComponentTooltip(this.font, txt, mouseX, mouseY);
        }
        else super.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        int runtime = this.menu.getRuntime();
        int total = ResourceSimulatorBlockEntity.RUNTIME;
        if (runtime > 0) {
            int rTime = Math.min(99, Mth.ceil(100F * (total - runtime) / total));
            gfx.drawString(this.font, rTime + "%", 184, 123, Color.AQUA, true);
        }
        CachedModel cModel = this.model();
        if (cModel.isValid()) {
            int xOff = 18;
            String msg = I18n.get("hostilenetworks.gui.target");
            gfx.drawString(this.font, msg, xOff, 9, Color.WHITE);
            xOff += this.font.width(msg);
            gfx.drawString(this.font, cModel.getModel().name(), xOff, 9, Color.LIME);

            xOff = 18;
            msg = I18n.get("hostilenetworks.gui.tier");
            gfx.drawString(this.font, msg, xOff, 9 + this.font.lineHeight + 3, Color.WHITE);
            xOff += this.font.width(msg);
            msg = I18n.get("hostilenetworks.tier." + cModel.getTier().name);
            gfx.drawString(this.font, msg, xOff, 9 + this.font.lineHeight + 3, cModel.getTier().color());

            xOff = 18;
            msg = I18n.get("hostilenetworks.gui.accuracy");
            gfx.drawString(this.font, msg, xOff, 9 + (this.font.lineHeight + 3) * 2, Color.WHITE);
            xOff += this.font.width(msg);
            msg = new DecimalFormat("##.##%").format(cModel.getAccuracy());
            gfx.drawString(this.font, msg, xOff, 9 + (this.font.lineHeight + 3) * 2, cModel.getTier().color());
        }
        int left = 29;
        int top = 51;
        int spacing = this.font.lineHeight + 3;
        int idx = 0;
        for (TickableText t : this.body) {
            t.render(this.font, gfx, left, top + spacing * idx);
            if (t.causesNewLine()) {
                idx++;
                left = 29;
            }
            else {
                left += t.getWidth(this.font);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int left = this.getGuiLeft();
        int top = this.getGuiTop();

        gfx.blit(BASE, left + 8, top, 0, 0, 216, 141, 256, 256);
        // Model-slot en, 20px lager, het ghost slot (zelfde sprite)
        gfx.blit(BASE, left - 14, top, 0, 141, 18, 18, 256, 256);
        gfx.blit(BASE, left - 14, top + 20, 0, 141, 18, 18, 256, 256);

        int energyHeight = 87 - Mth.ceil(87F * this.menu.getEnergyStored() / HostileConfig.simPowerCap);
        gfx.blit(BASE, left + 211, top + 48, 18, 141, 7, energyHeight, 256, 256);

        int dataHeight = 87;
        CachedModel cModel = this.model();
        if (cModel.isValid()) {
            ModelTier tier = cModel.getTier();
            if (tier == tier.next()) dataHeight = 0;
            else dataHeight = 87 - Mth.ceil(87F * (cModel.getData() - cModel.getTierData()) / (cModel.getNextTierData() - cModel.getTierData()));
        }
        gfx.blit(BASE, left + 14, top + 48, 18, 141, 7, dataHeight, 256, 256);
        gfx.blit(PLAYER, left + 28, top + 145, 0, 0, 176, 90, 256, 256);

        // Ghost slot: gekozen drop, half doorzichtig; in renderBg zodat tooltips erboven komen
        int gx = left + GHOST_X, gy = top + GHOST_Y;
        ItemStack ghost = this.selectedDrop();
        if (!ghost.isEmpty()) {
            gfx.renderItem(ghost, gx, gy);
            gfx.renderItemDecorations(this.font, ghost, gx, gy);
            gfx.fill(RenderType.guiGhostRecipeOverlay(), gx, gy, gx + 16, gy + 16, 0x60_8B8B8B);
        }
        if (this.isHoveringGhost(mouseX, mouseY)) {
            gfx.fill(RenderType.guiOverlay(), gx, gy, gx + 16, gy + 16, 0x80_FFFFFF);
        }
    }

    private static final Component ERROR = Component.literal("ERROR").withStyle(ChatFormatting.OBFUSCATED);

    @Override
    public void containerTick() {
        if (this.menu.getFailState() != FailureState.NONE) {
            FailureState oState = this.lastFailState;
            this.lastFailState = this.menu.getFailState();
            if (oState != this.lastFailState) {
                this.body.clear();
                String[] msg = I18n.get(this.lastFailState.getKey()).split("\\n");
                if (this.lastFailState == FailureState.INPUT) {
                    CachedModel cModel = this.model();
                    Component name = cModel.isValid() ? cModel.getModel().input().getHoverName() : ERROR;
                    msg = I18n.get(this.lastFailState.getKey(), name.getString()).split("\\n");
                }
                for (String s : msg) this.body.add(new TickableText(s, Color.WHITE));
            }
            this.runtimeTextLoaded = false;
        }
        else if (!this.runtimeTextLoaded) {
            int ticks = ResourceSimulatorBlockEntity.RUNTIME - this.menu.getRuntime();
            float speed = 0.65F;
            this.body.clear();
            int iters = DataModelItem.getIters(this.menu.getSlot(ResourceSimulatorBlockEntity.SLOT_MODEL).getItem());
            for (int i = 0; i < 7; i++) {
                TickableText txt = new TickableText(I18n.get("hostilenetworks.run." + i, iters), Color.WHITE, i != 0 && i != 5, speed);
                this.body.add(txt.setTicks(ticks));
                ticks = Math.max(0, ticks - txt.getMaxUsefulTicks());
                if (i == 0) {
                    txt = new TickableText("v" + HostileNetworks.VERSION, ChatFormatting.GOLD.getColor(), true, speed);
                    this.body.add(txt.setTicks(ticks));
                    ticks = Math.max(0, ticks - txt.getMaxUsefulTicks());
                }
                else if (i == 5) {
                    String key = "hostilenetworks.color_text." + (this.menu.didPredictionSucceed() ? "success" : "failed");
                    txt = new TickableText(I18n.get(key), (this.menu.didPredictionSucceed() ? ChatFormatting.GOLD : ChatFormatting.RED).getColor(), true, speed);
                    this.body.add(txt.setTicks(ticks));
                    ticks = Math.max(0, ticks - txt.getMaxUsefulTicks());
                }
            }
            this.runtimeTextLoaded = true;
            this.lastFailState = FailureState.NONE;
        }

        TickableText.tickList(this.body);
        if (this.menu.getRuntime() == 0) this.runtimeTextLoaded = false;
    }
}
