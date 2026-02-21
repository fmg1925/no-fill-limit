package com.fmg1925.nofilllimit.mixin;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPosArgument.class)
public class NoOutOfWorldMixin {
    @Shadow
    public static BlockPos getBlockPos(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        throw new AssertionError();
    }

    @Inject(
            method = "getLoadedBlockPos(Lcom/mojang/brigadier/context/CommandContext;Lnet/minecraft/server/level/ServerLevel;Ljava/lang/String;)Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void bypassWorldBounds(CommandContext<CommandSourceStack> context, ServerLevel level, String name, CallbackInfoReturnable<BlockPos> cir) throws CommandSyntaxException {
        cir.setReturnValue(getBlockPos(context, name));
    }
}