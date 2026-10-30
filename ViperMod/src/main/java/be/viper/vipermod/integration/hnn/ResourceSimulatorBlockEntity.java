package be.viper.vipermod.integration.hnn;

import dev.shadowsoffire.hostilenetworks.HostileConfig;
import dev.shadowsoffire.hostilenetworks.data.CachedModel;
import dev.shadowsoffire.hostilenetworks.data.DataModel;
import dev.shadowsoffire.hostilenetworks.data.DataModelRegistry;
import dev.shadowsoffire.hostilenetworks.data.ModelTier;
import dev.shadowsoffire.hostilenetworks.item.DataModelItem;
import dev.shadowsoffire.hostilenetworks.tile.SimChamberTileEntity.FailureState;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.cap.ModifiableEnergyStorage;
import dev.shadowsoffire.placebo.menu.SimpleDataSlots;
import dev.shadowsoffire.placebo.menu.SimpleDataSlots.IDataAutoRegister;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Sim chamber + loot fabricator in één: een gelukte simulatie levert direct de gekozen fabricator-drop op.
 * Tick-logica volgt HNN's SimChamberTileEntity (5.3.3).
 */
public class ResourceSimulatorBlockEntity extends BlockEntity implements TickingBlockEntity, IDataAutoRegister {
    public static final int SLOT_MODEL = 0, SLOT_INPUT = 1, SLOT_BASE = 2, SLOT_LOOT = 3;
    public static final int RUNTIME = 300;
    private static final int PUSH_INTERVAL = 20;

    protected final SimulatorItemHandler inventory = new SimulatorItemHandler();
    protected final ModifiableEnergyStorage energy = new ModifiableEnergyStorage(HostileConfig.simPowerCap, HostileConfig.simPowerCap);
    protected final SimpleDataSlots data = new SimpleDataSlots();
    /** Gekozen fabricator-drop per model (model-id -> index). */
    protected final Object2IntMap<ResourceLocation> selections = new Object2IntOpenHashMap<>();

    protected CachedModel currentModel = CachedModel.EMPTY;
    protected int runtime = 0;
    protected boolean predictionSuccess = false;
    protected FailureState failState = FailureState.NONE;
    /** Enkel client: keuze van het huidige model, via data slot. */
    protected int clientSelection = -1;

    public ResourceSimulatorBlockEntity(BlockPos pos, BlockState state) {
        super(HnnContent.RESOURCE_SIMULATOR_BE.get(), pos, state);
        this.data.addData(() -> this.runtime, v -> this.runtime = v);
        this.data.addData(() -> this.predictionSuccess, v -> this.predictionSuccess = v);
        this.data.addData(() -> this.failState.ordinal(), v -> this.failState = FailureState.values()[v]);
        this.data.addData(() -> this.getSelectedDrop(DataModelItem.getStoredModel(this.inventory.getStackInSlot(SLOT_MODEL)).getOptional().orElse(null)),
                v -> this.clientSelection = v);
        this.data.addEnergy(this.energy);
    }

    @Override
    public void registerSlots(Consumer<DataSlot> consumer) {
        this.data.register(consumer);
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        this.tickSimulation(level);
        if (level.getGameTime() % PUSH_INTERVAL == 0) this.pushLootDown(level, pos);
    }

    private void tickSimulation(Level level) {
        ItemStack model = this.inventory.getStackInSlot(SLOT_MODEL);
        if (!model.isEmpty()) {
            CachedModel oldModel = this.currentModel;
            this.currentModel = this.getOrLoadModel(model);
            if (oldModel != this.currentModel) {
                this.runtime = 0;
            }
            if (this.currentModel.isValid()) {
                if (this.currentModel.getTier() == ModelTier.FAULTY) {
                    this.failState = FailureState.FAULTY;
                    this.runtime = 0;
                    return;
                }
                if (this.runtime == 0) {
                    if (this.canStartSimulation()) {
                        this.runtime = RUNTIME;
                        this.predictionSuccess = level.random.nextFloat() <= this.currentModel.getAccuracy();
                        this.inventory.getStackInSlot(SLOT_INPUT).shrink(1);
                        this.setChanged();
                    }
                }
                else if (this.runtime == 1) {
                    // Laatste tick: enkel afronden als de loot past, anders wachten zonder energie
                    if (this.finishSimulation(model)) {
                        this.runtime = 0;
                        this.failState = FailureState.NONE;
                        this.setChanged();
                        this.pushLootDown(level, this.worldPosition);
                    }
                    else this.failState = FailureState.OUTPUT;
                }
                else if (this.hasPowerFor(this.currentModel.getModel())) {
                    this.failState = FailureState.NONE;
                    this.energy.setEnergy(this.energy.getEnergyStored() - this.getPowerCost(this.currentModel.getModel()));
                    this.runtime--;
                    this.setChanged();
                }
                else {
                    this.failState = FailureState.ENERGY_MID_CYCLE;
                }
                return;
            }
        }
        this.failState = FailureState.MODEL;
        this.runtime = 0;
    }

    private boolean finishSimulation(ItemStack model) {
        ItemStack loot = this.predictionSuccess ? this.getSelectedLoot() : ItemStack.EMPTY;
        if (!loot.isEmpty() && !this.inventory.insertItemInternal(SLOT_LOOT, loot, true).isEmpty()) return false;

        ItemStack base = this.inventory.getStackInSlot(SLOT_BASE);
        if (base.isEmpty()) this.inventory.setStackInSlot(SLOT_BASE, this.currentModel.getModel().baseDrop().copy());
        else base.grow(1);
        if (!loot.isEmpty()) this.inventory.insertItemInternal(SLOT_LOOT, loot, false);

        ModelTier tier = this.currentModel.getTier();
        if (tier != tier.next() && HostileConfig.simModelUpgrade > 0) {
            int newData = this.currentModel.getData() + 1;
            if (!(HostileConfig.simModelUpgrade == 2 && newData > this.currentModel.getNextTierData())) {
                this.currentModel.setData(newData);
            }
        }
        DataModelItem.setIters(model, DataModelItem.getIters(model) + 1);
        return true;
    }

    /** Zelfde controles als HNN; voor de loot-plaats wordt de huidige keuze gebruikt. */
    public boolean canStartSimulation() {
        if (!DataModelItem.matchesInput(this.inventory.getStackInSlot(SLOT_MODEL), this.inventory.getStackInSlot(SLOT_INPUT))) {
            this.failState = FailureState.INPUT;
            return false;
        }

        DataModel model = this.currentModel.getModel();
        ItemStack loot = this.getSelectedLoot();
        boolean baseFits = canStack(this.inventory.getStackInSlot(SLOT_BASE), model.baseDrop());
        boolean lootFits = loot.isEmpty() || this.inventory.insertItemInternal(SLOT_LOOT, loot, true).isEmpty();
        if (!baseFits || !lootFits) {
            this.failState = FailureState.OUTPUT;
            return false;
        }
        if (!this.hasPowerFor(model)) {
            this.failState = FailureState.ENERGY;
            return false;
        }
        this.failState = FailureState.NONE;
        return true;
    }

    private static boolean canStack(ItemStack a, ItemStack b) {
        if (a.isEmpty()) return true;
        return ItemStack.isSameItemSameTags(a, b) && a.getCount() < a.getMaxStackSize();
    }

    /** Enkel de loot gaat naar onder, generalized predictions nooit. */
    private void pushLootDown(Level level, BlockPos pos) {
        ItemStack loot = this.inventory.getStackInSlot(SLOT_LOOT);
        if (loot.isEmpty()) return;
        BlockEntity below = level.getBlockEntity(pos.below());
        if (below == null) return;
        below.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).ifPresent(target -> {
            ItemStack rest = ItemHandlerHelper.insertItemStacked(target, loot.copy(), false);
            if (rest.getCount() != loot.getCount()) {
                this.inventory.setStackInSlot(SLOT_LOOT, rest);
                this.setChanged();
            }
        });
    }

    public int getPowerCost(DataModel model) {
        return model.simCost() + HostileConfig.fabPowerCost;
    }

    public boolean hasPowerFor(DataModel model) {
        return this.energy.getEnergyStored() >= this.getPowerCost(model);
    }

    protected CachedModel getOrLoadModel(ItemStack stack) {
        if (this.currentModel.getSourceStack() == stack) return this.currentModel;
        return new CachedModel(stack, 0);
    }

    // ---- keuze van de fabricator-drop ----

    /** Index van de gekozen drop, standaard de eerste; -1 als het model geen drops heeft. */
    public int getSelectedDrop(DataModel model) {
        if (model == null || model.fabDrops().isEmpty()) return -1;
        int sel = this.selections.getOrDefault(DataModelRegistry.INSTANCE.getKey(model), 0);
        return sel >= 0 && sel < model.fabDrops().size() ? sel : 0;
    }

    private ItemStack getSelectedLoot() {
        DataModel model = this.currentModel.getModel();
        int sel = this.getSelectedDrop(model);
        return sel == -1 ? ItemStack.EMPTY : model.fabDrops().get(sel).copy();
    }

    /** Volgende (+1) of vorige (-1) drop kiezen voor het model in het model-slot. */
    public void cycleSelection(int delta) {
        DataModel model = DataModelItem.getStoredModel(this.inventory.getStackInSlot(SLOT_MODEL)).getOptional().orElse(null);
        int size = model == null ? 0 : model.fabDrops().size();
        if (size == 0) return;
        int sel = Math.floorMod(this.getSelectedDrop(model) + delta, size);
        this.selections.put(DataModelRegistry.INSTANCE.getKey(model), sel);
        this.setChanged();
    }

    public int getClientSelection() {
        return this.clientSelection;
    }

    // ---- opslag ----

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", this.inventory.serializeNBT());
        tag.putInt("energy", this.energy.getEnergyStored());
        tag.putString("model", !this.currentModel.isValid() ? "null" : DataModelRegistry.INSTANCE.getKey(this.currentModel.getModel()).toString());
        tag.putInt("runtime", this.runtime);
        tag.putBoolean("predSuccess", this.predictionSuccess);
        tag.putInt("failState", this.failState.ordinal());
        CompoundTag sel = new CompoundTag();
        this.selections.forEach((id, idx) -> sel.putInt(id.toString(), idx));
        tag.put("selections", sel);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound("inventory"));
        this.energy.setEnergy(tag.getInt("energy"));
        CachedModel cModel = this.getOrLoadModel(this.inventory.getStackInSlot(SLOT_MODEL));
        ResourceLocation modelId = ResourceLocation.tryParse(tag.getString("model"));
        if (cModel.isValid() && DataModelRegistry.INSTANCE.getKey(cModel.getModel()).equals(modelId)) {
            this.currentModel = cModel;
        }
        this.runtime = tag.getInt("runtime");
        this.predictionSuccess = tag.getBoolean("predSuccess");
        this.failState = FailureState.values()[tag.getInt("failState")];
        this.selections.clear();
        CompoundTag sel = tag.getCompound("selections");
        for (String key : sel.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) this.selections.put(id, sel.getInt(key));
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return LazyOptional.of(() -> this.inventory).cast();
        if (cap == ForgeCapabilities.ENERGY) return LazyOptional.of(() -> this.energy).cast();
        return super.getCapability(cap, side);
    }

    public SimulatorItemHandler getInventory() {
        return this.inventory;
    }

    public int getEnergyStored() {
        return this.energy.getEnergyStored();
    }

    public int getRuntime() {
        return this.runtime;
    }

    public boolean didPredictionSucceed() {
        return this.predictionSuccess;
    }

    public FailureState getFailState() {
        return this.failState;
    }

    /** Model en input erin, beide outputs eruit; langs alle zijden. */
    public class SimulatorItemHandler extends InternalItemHandler {
        public SimulatorItemHandler() {
            super(4);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_MODEL) return stack.getItem() instanceof DataModelItem;
            if (slot == SLOT_INPUT) return DataModelItem.matchesInput(this.getStackInSlot(SLOT_MODEL), stack);
            return true;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_MODEL ? 1 : super.getSlotLimit(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot > SLOT_INPUT) return stack;
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot <= SLOT_INPUT) return ItemStack.EMPTY;
            return super.extractItem(slot, amount, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            ResourceSimulatorBlockEntity.this.setChanged();
        }
    }
}
