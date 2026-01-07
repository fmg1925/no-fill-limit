package com.fmg1925.nofilllimit.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.command.argument.BlockPosArgumentType.getBlockPos;

@Mixin(BlockPosArgumentType.class)
public class NoOutOfWorldMixin {
    @Inject(method = "getLoadedBlockPos", at = @At("HEAD"), cancellable = true)
    private static void bypassLoadedCheck(CommandContext<ServerCommandSource> context, String name, CallbackInfoReturnable<BlockPos> cir) {
        cir.setReturnValue(getBlockPos(context, name));
    }

    @Inject(method = "getValidBlockPos", at = @At("HEAD"), cancellable = true)
    private static void bypassWorldCheck(CommandContext<ServerCommandSource> context, String name, CallbackInfoReturnable<BlockPos> cir) {
        cir.setReturnValue(getBlockPos(context, name));
    }
}