package com.playerattributemanagement;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.ManagedAttributeIds;
import com.playerattributemanagement.attribute.PlayerAttributeStore;
import com.playerattributemanagement.common.command.AttributeCommandLogic;
import com.playerattributemanagement.util.Identifiers;
import java.util.Map;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class PlayerAttributeCommand {
	private static final AttributeCommandLogic.Adapter<ServerPlayerEntity, Identifier, ServerCommandSource> COMMAND_ADAPTER =
		new AttributeCommandLogic.Adapter<>() {
			@Override
			public Identifier normalize(Identifier requested) {
				return AttributeIdMapper.normalize(requested);
			}

			@Override
			public boolean isValid(ServerCommandSource context, Identifier canonical) {
				return canonical != null;
			}

			@Override
			public double setExtra(ServerPlayerEntity player, Identifier canonical, double value) {
				return PlayerAttributeStore.setExtra(player, canonical, value);
			}

			@Override
			public double addExtra(ServerPlayerEntity player, Identifier canonical, double delta) {
				return PlayerAttributeStore.addExtra(player, canonical, delta);
			}

			@Override
			public void resetExtra(ServerPlayerEntity player, Identifier canonical) {
				PlayerAttributeStore.reset(player, canonical);
			}

			@Override
			public Map<Identifier, Double> listExtras(ServerPlayerEntity player) {
				return PlayerAttributeStore.getExtras(player);
			}
		};

	private PlayerAttributeCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register(PlayerAttributeCommand::registerInternal);
	}

	private static void registerInternal(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(
			CommandManager.literal("pamattr")
				.requires(source -> source.hasPermissionLevel(2))
				.then(targeted("set", DoubleArgumentType.doubleArg(-2048, 2048), (source, player, id, value) -> {
					AttributeCommandLogic.Result<Identifier> result = AttributeCommandLogic.execute(
						AttributeCommandLogic.Operation.SET,
						source,
						player,
						id,
						value,
						COMMAND_ADAPTER
					);
					if (!result.success()) {
						source.sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
						return 0;
					}
					return playerSuccess(player, result.canonicalId(), result.value(), "command.playerattributemanagement.changed.set");
				}))
				.then(targeted("add", DoubleArgumentType.doubleArg(-2048, 2048), (source, player, id, value) -> {
					AttributeCommandLogic.Result<Identifier> result = AttributeCommandLogic.execute(
						AttributeCommandLogic.Operation.ADD,
						source,
						player,
						id,
						value,
						COMMAND_ADAPTER
					);
					if (!result.success()) {
						source.sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
						return 0;
					}
					return playerSuccess(player, result.canonicalId(), result.value(), "command.playerattributemanagement.changed.add");
				}))
				.then(CommandManager.literal("reset").then(
					CommandManager.argument("attribute", StringArgumentType.string())
						.suggests((ctx, builder) -> CommandSource.suggestMatching(ManagedAttributeIds.all().stream().map(Identifier::toString), builder))
						.executes(ctx -> {
							ServerPlayerEntity player = ctx.getSource().getPlayer();
							if (player == null) {
								ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.need_player"));
								return 0;
							}
							Identifier id = parseId(ctx.getArgument("attribute", String.class));
							if (id == null) {
								ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
								return 0;
							}
							AttributeCommandLogic.Result<Identifier> result = AttributeCommandLogic.execute(
								AttributeCommandLogic.Operation.RESET,
								ctx.getSource(),
								player,
								id,
								0.0,
								COMMAND_ADAPTER
							);
							if (!result.success()) {
								ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
								return 0;
							}
							ctx.getSource().sendFeedback(() -> Text.translatable("command.playerattributemanagement.reset", id.toString()), true);
							return 1;
						})
						.then(CommandManager.argument("target", EntityArgumentType.player()).executes(ctx -> {
							ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
							Identifier id = parseId(ctx.getArgument("attribute", String.class));
							if (id == null) {
								ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
								return 0;
							}
							AttributeCommandLogic.Result<Identifier> result = AttributeCommandLogic.execute(
								AttributeCommandLogic.Operation.RESET,
								ctx.getSource(),
								player,
								id,
								0.0,
								COMMAND_ADAPTER
							);
							if (!result.success()) {
								ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
								return 0;
							}
							ctx.getSource().sendFeedback(
								() -> Text.translatable("command.playerattributemanagement.reset.target", id.toString(), player.getName().getString()),
								true
							);
							return 1;
						}))
				))
				.then(CommandManager.literal("resetall").executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayer();
					if (player == null) {
						ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.need_player"));
						return 0;
					}
					PlayerAttributeStore.resetAll(player);
					ctx.getSource().sendFeedback(() -> Text.translatable("command.playerattributemanagement.reset_all"), true);
					return 1;
				}).then(CommandManager.argument("target", EntityArgumentType.player()).executes(ctx -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
					PlayerAttributeStore.resetAll(player);
					ctx.getSource().sendFeedback(
						() -> Text.translatable("command.playerattributemanagement.reset_all.target", player.getName().getString()),
						true
					);
					return 1;
				})))
				.then(CommandManager.literal("list").executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayer();
					if (player == null) {
						ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.need_player"));
						return 0;
					}
					return listAttributes(ctx.getSource(), player);
				}).then(CommandManager.argument("target", EntityArgumentType.player()).executes(ctx -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
					return listAttributes(ctx.getSource(), player);
				})))
		);
	}

	@FunctionalInterface
	private interface AttributeOp {
		int run(ServerCommandSource source, ServerPlayerEntity player, Identifier id, double value);
	}

	private static ArgumentBuilder<ServerCommandSource, ?> targeted(String literal, com.mojang.brigadier.arguments.ArgumentType<Double> arg, AttributeOp op) {
		return CommandManager.literal(literal)
			.then(CommandManager.argument("attribute", StringArgumentType.string())
				.suggests((ctx, builder) -> CommandSource.suggestMatching(ManagedAttributeIds.all().stream().map(Identifier::toString), builder))
				.then(CommandManager.argument("value", arg).executes(ctx -> {
					ServerPlayerEntity player = ctx.getSource().getPlayer();
					if (player == null) {
						ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.need_player"));
						return 0;
					}
					Identifier id = parseId(ctx.getArgument("attribute", String.class));
					if (id == null) {
						ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
						return 0;
					}
					double value = ctx.getArgument("value", Double.class);
					return op.run(ctx.getSource(), player, id, value);
				}).then(CommandManager.argument("target", EntityArgumentType.player()).executes(ctx -> {
					ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "target");
					Identifier id = parseId(ctx.getArgument("attribute", String.class));
					if (id == null) {
						ctx.getSource().sendError(Text.translatable("command.playerattributemanagement.unknown_attr"));
						return 0;
					}
					double value = ctx.getArgument("value", Double.class);
					return op.run(ctx.getSource(), player, id, value);
				}))));
	}

	private static Identifier parseId(String raw) {
		try {
			return Identifiers.fromPath(raw);
		} catch (Exception e) {
			return null;
		}
	}

	private static int playerSuccess(ServerPlayerEntity target, Identifier id, double newValue, String translationKey) {
		target.sendMessage(Text.translatable(translationKey, id.toString(), newValue), false);
		return 1;
	}

	private static int listAttributes(ServerCommandSource source, ServerPlayerEntity player) {
		AttributeCommandLogic.Result<Identifier> result = AttributeCommandLogic.execute(
			AttributeCommandLogic.Operation.LIST,
			source,
			player,
			null,
			0.0,
			COMMAND_ADAPTER
		);
		Map<Identifier, Double> extras = result.list();
		if (extras.isEmpty()) {
			source.sendFeedback(() -> Text.translatable("command.playerattributemanagement.list_empty", player.getName().getString()), false);
			return 1;
		}

		extras.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			source.sendFeedback(() -> Text.translatable("command.playerattributemanagement.list_entry", entry.getKey().toString(), entry.getValue()), false);
		});
		return extras.size();
	}
}
