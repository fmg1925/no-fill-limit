package com.fmg1925.nofilllimit.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(FillCommand.class)
public class NoFillLimitMixin {
	@ModifyVariable(
			method = "fillBlocks",
			at = @At("STORE"),
			name = "limit")
	private static int bypassBlockLimit(int originalLimit) {
		// Sobreescribimos cualquier límite establecido por la gamerule
		return Integer.MAX_VALUE;
	}

	@Unique
	private static CommandSourceStack currentSource;

	@Inject(method = "fillBlocks", at = @At("HEAD"))
	private static void captureSourceContext(
			CommandSourceStack source,
			BoundingBox region,
			BlockInput target,
			@Coerce Object mode,
			@Nullable Predicate<BlockInWorld> predicate,
			boolean strict,
			CallbackInfoReturnable<Integer> cir
	) {
		// 1. Capturamos el origen del comando apenas entra al método
		currentSource = source;
	}

	@ModifyVariable(
			method = "fillBlocks",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/BoundingBox;getXSpan()I"),
			ordinal = 0,
			argsOnly = true)
	private static BoundingBox clampRegion(BoundingBox originalRegion) {
		if (currentSource == null) return originalRegion;

		int minBuildY = currentSource.getLevel().getMinY();
		int maxBuildY = currentSource.getLevel().getMaxY();

		int x1 = originalRegion.minX();
		int x2 = originalRegion.maxX();
		int y1 = Math.max(minBuildY, originalRegion.minY());
		int y2 = Math.min(maxBuildY, originalRegion.maxY());
		int z1 = originalRegion.minZ();
		int z2 = originalRegion.maxZ();

		// 3. Limpiamos la referencia para evitar fugas de memoria
		currentSource = null;

		return new BoundingBox(x1, y1, z1, x2, y2, z2);
	}

	@Inject(method = "fillBlocks", at = @At("RETURN"))
	private static void mensaje(
			CommandSourceStack source, BoundingBox region, BlockInput target, @Coerce Object mode, @Nullable Predicate<BlockInWorld> predicate, boolean strict, CallbackInfoReturnable<Integer> cir
	) {
		if (cir.getReturnValue() == 0) return;

		int x1 = region.minX();
		int y1 = region.minY();
		int z1 = region.minZ();
		int x2 = region.maxX();
		int y2 = region.maxY();
		int z2 = region.maxZ();

		String cmdStart = String.format("/tp %d %d %d", x1, y1, z1);
		String cmdEnd = String.format("/tp %d %d %d", x2, y2, z2);

		Component hintStart = Component.literal("Tp to start");
		Component hintEnd = Component.literal("Tp to end");

		Component message = Component.literal("Filling from ")
				.withColor(0xFFFFFF)
				.append(Component.literal(String.format("[%d, %d, %d]", x1, y1, z1))
						.withStyle(style -> style
								.withColor(0x00FF00)
								.withClickEvent(new ClickEvent.SuggestCommand(cmdStart))
								.withHoverEvent(new HoverEvent.ShowText(hintStart))
						))
				.append(Component.literal(" to ").withColor(0xFFFFFF)
				.append(Component.literal(String.format("[%d, %d, %d]", x2, y2, z2))
						.withStyle(style -> style
								.withColor(0x00FF00)
								.withClickEvent(new ClickEvent.SuggestCommand(cmdEnd))
								.withHoverEvent(new HoverEvent.ShowText(hintEnd))
						)));

		Component message2 = Component.literal(String.format("Width: %d, Height: %d, Depth: %d", region.getXSpan(), region.getYSpan(), region.getZSpan()));

		source.sendSuccess(() -> message, true);
		source.sendSuccess(() -> message2, true);
	}
}