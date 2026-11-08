package com.mcking.stonespeaker;

import java.util.List;

import com.mcking.stonespeaker.client.ClientHooks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** U 盘：右键空气打开歌单编辑界面；右键讲解石插入歌单。 */
public class UsbDriveItem extends Item {
    public UsbDriveItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            ClientHooks.openUsbScreen(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        int count = ModComponents.getPlaylist(stack).size();
        tooltip.add(Component.translatable("tooltip.stonespeaker.usb_tracks", count)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.stonespeaker.usb_hint")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
