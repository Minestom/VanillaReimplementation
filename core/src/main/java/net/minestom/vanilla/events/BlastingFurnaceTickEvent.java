package net.minestom.vanilla.events;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;

public record BlastingFurnaceTickEvent(Block getBlock, Instance getInstance, BlockVec getBlockPosition,
                                       Inventory getInventory) implements Event, InstanceEvent, BlockEvent, InventoryEvent {
}
