package com.mcking.stonespeaker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mcking.stonespeaker.client.ClientPlayback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(StoneSpeaker.MODID)
public final class StoneSpeaker {
    public static final String MODID = "explainationstone";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    /** 没有插入 U 盘时，讲解石默认播放的曲目（内置音乐复制到 config 目录）。 */
    public static final String DEFAULT_TRACK = "music.ogg";

    // 讲解石
    public static final DeferredBlock<SpeakerBlock> STONE_SPEAKER = BLOCKS.register(
            "stone_speaker",
            () -> new SpeakerBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion())
    );

    public static final DeferredItem<net.minecraft.world.item.BlockItem> STONE_SPEAKER_ITEM =
            ITEMS.register("stone_speaker",
                    () -> new net.minecraft.world.item.BlockItem(STONE_SPEAKER.get(),
                            new net.minecraft.world.item.Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SpeakerBlockEntity>> SPEAKER_BE =
            BLOCK_ENTITIES.register("stone_speaker",
                    () -> BlockEntityType.Builder.of(SpeakerBlockEntity::new, STONE_SPEAKER.get())
                            .build(null));

    // U 盘
    public static final DeferredItem<UsbDriveItem> USB_DRIVE = ITEMS.register(
            "usb_drive",
            () -> new UsbDriveItem(new Item.Properties().stacksTo(1)));

    // 原石铠：防御 2/5/4/1，耐久倍率 12，用圆石修复
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> RAW_STONE_MATERIAL =
            ARMOR_MATERIALS.register("raw_stone", () -> new ArmorMaterial(
                    Map.of(
                            ArmorItem.Type.HELMET, 2,
                            ArmorItem.Type.CHESTPLATE, 5,
                            ArmorItem.Type.LEGGINGS, 4,
                            ArmorItem.Type.BOOTS, 1),
                    12,
                    SoundEvents.ARMOR_EQUIP_IRON,
                    () -> Ingredient.of(Items.COBBLESTONE),
                    List.of(new ArmorMaterial.Layer(
                            ResourceLocation.fromNamespaceAndPath(MODID, "raw_stone"))),
                    0.0F,
                    0.0F
            ));

    public static final DeferredItem<ArmorItem> RAW_STONE_HELMET = ITEMS.register(
            "raw_stone_helmet",
            () -> new ArmorItem(RAW_STONE_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(12))));

    public static final DeferredItem<ArmorItem> RAW_STONE_CHESTPLATE = ITEMS.register(
            "raw_stone_chestplate",
            () -> new ArmorItem(RAW_STONE_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(12))));

    public static final DeferredItem<ArmorItem> RAW_STONE_LEGGINGS = ITEMS.register(
            "raw_stone_leggings",
            () -> new ArmorItem(RAW_STONE_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(12))));

    public static final DeferredItem<ArmorItem> RAW_STONE_BOOTS = ITEMS.register(
            "raw_stone_boots",
            () -> new ArmorItem(RAW_STONE_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(12))));

    public StoneSpeaker(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ARMOR_MATERIALS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        ModComponents.DATA_COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerPayloads);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(RAW_STONE_HELMET.get());
            event.accept(RAW_STONE_CHESTPLATE.get());
            event.accept(RAW_STONE_LEGGINGS.get());
            event.accept(RAW_STONE_BOOTS.get());
        }
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(STONE_SPEAKER_ITEM.get());
            event.accept(USB_DRIVE.get());
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SetPlaylistPayload.TYPE, SetPlaylistPayload.STREAM_CODEC,
                StoneSpeaker::onSetPlaylist);
        registrar.playToClient(PlaylistSyncPayload.TYPE, PlaylistSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> ClientPlayback.sync(payload.pos(), payload.tracks(), payload.playMode(),
                                payload.range())));
        registrar.playToClient(PlaylistCommandPayload.TYPE, PlaylistCommandPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(
                        () -> ClientPlayback.command(payload.pos(), payload.command())));
    }

    /** 客户端保存 U 盘歌单与模式。 */
    private static void onSetPlaylist(SetPlaylistPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            List<String> tracks = new ArrayList<>();
            for (String part : payload.tracks().split("\n")) {
                if (!part.isBlank()) {
                    tracks.add(part.trim());
                }
            }
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() instanceof UsbDriveItem) {
                    ModComponents.setPlaylist(stack, tracks);
                    ModComponents.setSettings(stack, payload.playMode(), payload.actionMode(),
                            payload.redstoneMode(), payload.range());
                    break;
                }
            }
        });
    }

    /** 模型实际占用的体积（Blockbench 坐标 x3..13, y0..12, z4..13），按朝向旋转。 */
    public static final class SpeakerBlock extends Block implements EntityBlock {
        public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
        public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

        private static final double MUSIC_RANGE = 64.0;

        private static final VoxelShape SHAPE_NORTH = Shapes.box(
                3.0 / 16.0, 0.0, 4.0 / 16.0,
                13.0 / 16.0, 12.0 / 16.0, 13.0 / 16.0);
        private static final VoxelShape SHAPE_EAST = Shapes.box(
                3.0 / 16.0, 0.0, 3.0 / 16.0,
                12.0 / 16.0, 12.0 / 16.0, 13.0 / 16.0);
        private static final VoxelShape SHAPE_SOUTH = Shapes.box(
                3.0 / 16.0, 0.0, 3.0 / 16.0,
                13.0 / 16.0, 12.0 / 16.0, 12.0 / 16.0);
        private static final VoxelShape SHAPE_WEST = Shapes.box(
                4.0 / 16.0, 0.0, 3.0 / 16.0,
                13.0 / 16.0, 12.0 / 16.0, 13.0 / 16.0);

        public SpeakerBlock(Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any()
                    .setValue(FACING, Direction.NORTH)
                    .setValue(POWERED, Boolean.FALSE));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING, POWERED);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        protected BlockState mirror(BlockState state, Mirror mirror) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return switch (state.getValue(FACING)) {
                case EAST -> SHAPE_EAST;
                case SOUTH -> SHAPE_SOUTH;
                case WEST -> SHAPE_WEST;
                default -> SHAPE_NORTH;
            };
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new SpeakerBlockEntity(pos, state);
        }

        /** 拿 U 盘右键：把歌单与模式写进讲解石（不自动播放）。 */
        @Override
        protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                  Player player, InteractionHand hand, BlockHitResult hitResult) {
            if (stack.getItem() instanceof UsbDriveItem) {
                if (!level.isClientSide && level.getBlockEntity(pos) instanceof SpeakerBlockEntity speaker) {
                    speaker.setSettings(ModComponents.getPlaylist(stack),
                            ModComponents.getPlayMode(stack),
                            ModComponents.getActionMode(stack),
                            ModComponents.getRedstoneMode(stack),
                            ModComponents.getRange(stack));
                    sendSync(level, pos, speaker.getTracks(), speaker.getPlayMode(), speaker.getRange());
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }

        /** 空手右键：按 U 盘的「右键模式」执行。 */
        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                  Player player, BlockHitResult hitResult) {
            if (!level.isClientSide) {
                applyActionMode(level, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        /** 红石：按 U 盘的「红石模式」执行。 */
        @Override
        protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                       Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
            if (level.isClientSide) {
                return;
            }
            boolean powered = level.hasNeighborSignal(pos);
            if (powered == state.getValue(POWERED)) {
                return;
            }
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
            // 只在上升沿触发：脉冲或持续信号都只算一次
            if (!powered) {
                return;
            }
            if (!(level.getBlockEntity(pos) instanceof SpeakerBlockEntity speaker)) {
                return;
            }
            if (!speaker.hasTracks()) {
                sendSync(level, pos, List.of(DEFAULT_TRACK), "list", ModComponents.DEFAULT_RANGE);
            }
            switch (speaker.getRedstoneMode()) {
                case "disabled" -> {
                }
                case "next" -> sendCommand(level, pos, PlaylistCommandPayload.NEXT);
                case "toggle_play" -> {
                    sendCommand(level, pos, PlaylistCommandPayload.TOGGLE_PAUSE);
                    speaker.setPaused(!speaker.isPaused());
                }
                default -> {
                    if (speaker.isPlaying()) {
                        sendCommand(level, pos, PlaylistCommandPayload.STOP);
                        speaker.setPlaying(false);
                    } else {
                        sendCommand(level, pos, PlaylistCommandPayload.PLAY);
                        speaker.setPlaying(true);
                    }
                }
            }
        }

        /** 方块被破坏/替换：停止这个坐标的播放。 */
        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos,
                                BlockState newState, boolean movedByPiston) {
            if (!level.isClientSide && !state.is(newState.getBlock())) {
                broadcastStop(level, pos);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }

        private static void applyActionMode(Level level, BlockPos pos) {
            if (!(level.getBlockEntity(pos) instanceof SpeakerBlockEntity speaker)) {
                return;
            }
            if (!speaker.hasTracks()) {
                sendSync(level, pos, List.of(DEFAULT_TRACK), "list", ModComponents.DEFAULT_RANGE);
            }
            switch (speaker.getActionMode()) {
                case "disabled" -> {
                }
                case "next" -> sendCommand(level, pos, PlaylistCommandPayload.NEXT);
                case "toggle_play" -> {
                    sendCommand(level, pos, PlaylistCommandPayload.TOGGLE_PAUSE);
                    speaker.setPaused(!speaker.isPaused());
                }
                default -> {
                    if (speaker.isPlaying()) {
                        sendCommand(level, pos, PlaylistCommandPayload.STOP);
                        speaker.setPlaying(false);
                    } else {
                        sendCommand(level, pos, PlaylistCommandPayload.PLAY);
                        speaker.setPlaying(true);
                    }
                }
            }
        }

        private static void sendSync(Level level, BlockPos pos, List<String> tracks, String playMode, int range) {
            PlaylistSyncPayload payload = new PlaylistSyncPayload(pos.immutable(),
                    String.join("\n", tracks), playMode, range);
            sendNear(level, pos, payload);
        }

        private static void sendCommand(Level level, BlockPos pos, int command) {
            sendNear(level, pos, new PlaylistCommandPayload(pos.immutable(), command));
        }

        private static void sendNear(Level level, BlockPos pos, net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 0.5;
            double cz = pos.getZ() + 0.5;
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(cx, cy, cz) <= MUSIC_RANGE * MUSIC_RANGE) {
                    PacketDistributor.sendToPlayer(player, payload);
                }
            }
        }

        private static void broadcastStop(Level level, BlockPos pos) {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }
            PlaylistCommandPayload payload = new PlaylistCommandPayload(pos.immutable(),
                    PlaylistCommandPayload.STOP);
            for (ServerPlayer player : serverLevel.players()) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}
