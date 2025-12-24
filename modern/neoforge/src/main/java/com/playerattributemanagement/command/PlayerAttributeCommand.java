package com.playerattributemanagement.command;

import com.playerattributemanagement.Playerattributemanagement;
import com.playerattributemanagement.api.PlayerAttributeApi;
import com.playerattributemanagement.attribute.AttributeIdMapper;
import com.playerattributemanagement.attribute.AttributeResolver;
import com.playerattributemanagement.common.command.AttributeCommandLogic;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Comparator;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Playerattributemanagement.MODID)
public final class PlayerAttributeCommand {
    private static final AttributeCommandLogic.Adapter<ServerPlayer, ResourceLocation, CommandSourceStack> COMMAND_ADAPTER =
        new AttributeCommandLogic.Adapter<>() {
            @Override
            public ResourceLocation normalize(ResourceLocation requested) {
                return AttributeIdMapper.normalize(requested);
            }

            @Override
            public boolean isValid(CommandSourceStack context, ResourceLocation canonical) {
                if (canonical == null) {
                    return false;
                }
                return AttributeResolver.exists(canonical, context.registryAccess())
                    && PlayerAttributeApi.isManagedAttribute(canonical);
            }

            @Override
            public double setExtra(ServerPlayer player, ResourceLocation canonical, double value) {
                return PlayerAttributeApi.setExtra(player, canonical, value);
            }

            @Override
            public double addExtra(ServerPlayer player, ResourceLocation canonical, double delta) {
                return PlayerAttributeApi.addExtra(player, canonical, delta);
            }

            @Override
            public void resetExtra(ServerPlayer player, ResourceLocation canonical) {
                PlayerAttributeApi.resetExtra(player, canonical);
            }

            @Override
            public Map<ResourceLocation, Double> listExtras(ServerPlayer player) {
                return PlayerAttributeApi.getAllExtras(player);
            }
        };

    private PlayerAttributeCommand() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("pamattr")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(attributeArg()
                            .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                                .executes(ctx -> setValue(
                                    ctx,
                                    EntityArgument.getPlayer(ctx, "target"),
                                    ResourceLocationArgument.getId(ctx, "attribute"),
                                    DoubleArgumentType.getDouble(ctx, "value")
                                )))))
                    .then(attributeArg()
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg())
                            .executes(ctx -> setValue(
                                ctx,
                                ctx.getSource().getPlayerOrException(),
                                ResourceLocationArgument.getId(ctx, "attribute"),
                                DoubleArgumentType.getDouble(ctx, "value")
                            )))))
                .then(Commands.literal("add")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(attributeArg()
                            .then(Commands.argument("delta", DoubleArgumentType.doubleArg())
                                .executes(ctx -> addValue(
                                    ctx,
                                    EntityArgument.getPlayer(ctx, "target"),
                                    ResourceLocationArgument.getId(ctx, "attribute"),
                                    DoubleArgumentType.getDouble(ctx, "delta")
                                )))))
                    .then(attributeArg()
                        .then(Commands.argument("delta", DoubleArgumentType.doubleArg())
                            .executes(ctx -> addValue(
                                ctx,
                                ctx.getSource().getPlayerOrException(),
                                ResourceLocationArgument.getId(ctx, "attribute"),
                                DoubleArgumentType.getDouble(ctx, "delta")
                            )))))
                .then(Commands.literal("reset")
                    .then(Commands.argument("target", EntityArgument.player())
                        .then(attributeArg()
                            .executes(ctx -> resetValue(
                                ctx,
                                EntityArgument.getPlayer(ctx, "target"),
                                ResourceLocationArgument.getId(ctx, "attribute")
                            ))))
                    .then(attributeArg()
                        .executes(ctx -> resetValue(
                            ctx,
                            ctx.getSource().getPlayerOrException(),
                            ResourceLocationArgument.getId(ctx, "attribute")
                        ))))
                .then(Commands.literal("list")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> listValues(ctx, EntityArgument.getPlayer(ctx, "target"))))
                    .executes(ctx -> listValues(ctx, ctx.getSource().getPlayerOrException())))
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> attributeArg() {
        return Commands.argument("attribute", ResourceLocationArgument.id());
    }

    private static int setValue(CommandContext<CommandSourceStack> ctx, ServerPlayer target, ResourceLocation requestedId, double value) {
        return executeAndReport(ctx, target, requestedId, value, AttributeCommandLogic.Operation.SET);
    }

    private static int addValue(CommandContext<CommandSourceStack> ctx, ServerPlayer target, ResourceLocation requestedId, double delta) {
        return executeAndReport(ctx, target, requestedId, delta, AttributeCommandLogic.Operation.ADD);
    }

    private static int resetValue(CommandContext<CommandSourceStack> ctx, ServerPlayer target, ResourceLocation requestedId) {
        return executeAndReport(ctx, target, requestedId, 0.0, AttributeCommandLogic.Operation.RESET);
    }

    private static int listValues(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        AttributeCommandLogic.Result<ResourceLocation> result = AttributeCommandLogic.execute(
            AttributeCommandLogic.Operation.LIST,
            ctx.getSource(),
            target,
            null,
            0.0,
            COMMAND_ADAPTER
        );
        Map<ResourceLocation, Double> extras = result.list();
        if (extras.isEmpty()) {
            ctx.getSource().sendSuccess(
                () -> Component.literal(target.getName().getString() + " has no managed bonuses"),
                false
            );
            return Command.SINGLE_SUCCESS;
        }

        extras.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
            .forEach(entry -> ctx.getSource().sendSuccess(
                () -> Component.literal(target.getName().getString() + " | " + entry.getKey() + ": " + entry.getValue()),
                false
            ));
        return extras.size();
    }

    private static int executeAndReport(
        CommandContext<CommandSourceStack> ctx,
        ServerPlayer target,
        ResourceLocation requestedId,
        double value,
        AttributeCommandLogic.Operation operation
    ) {
        AttributeCommandLogic.Result<ResourceLocation> result = AttributeCommandLogic.execute(
            operation,
            ctx.getSource(),
            target,
            requestedId,
            value,
            COMMAND_ADAPTER
        );
        if (!result.success()) {
            ctx.getSource().sendFailure(Component.literal("Unknown attribute id: " + requestedId));
            return 0;
        }

        ResourceLocation canonical = result.canonicalId();
        switch (operation) {
            case SET -> ctx.getSource().sendSuccess(
                () -> Component.literal("Set " + target.getName().getString() + " " + canonical + " extra to " + result.value()),
                true
            );
            case ADD -> ctx.getSource().sendSuccess(
                () -> Component.literal("Added " + value + " to " + target.getName().getString() + " " + canonical + ", extra=" + result.value()),
                true
            );
            case RESET -> ctx.getSource().sendSuccess(
                () -> Component.literal("Reset " + target.getName().getString() + " " + canonical + " extra"),
                true
            );
            default -> {}
        }
        return Command.SINGLE_SUCCESS;
    }
}
