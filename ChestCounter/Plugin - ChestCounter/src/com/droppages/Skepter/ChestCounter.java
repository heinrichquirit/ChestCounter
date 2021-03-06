package com.droppages.Skepter;

import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class ChestCounter extends JavaPlugin implements Listener {

	private final String PERMISSION = "CC.use";
	private HashMap<String, Block> map = new HashMap<String, Block>();

	public void onEnable() {
		saveDefaultConfig();
		if(getConfig().getBoolean("allowMetrics")) {
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
				Bukkit.getLogger().info("[CC] Metrics loaded");
			} catch (IOException e) {	
				Bukkit.getLogger().info("[CC] Metrics could not be loaded");
			}
		}
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {
		saveConfig();
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		String name = event.getPlayer().getName();
		int size = event.getInventory().getSize();
		if (size == 27 || size == 54) {
			if (map.containsKey(name)) {
				updateSign(map.get(name));
			}
		}
	}

	@EventHandler
	public void onUpdateSign(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if (hasSignAttached(block)) {
				if (block.getState().getType() == Material.CHEST) {
					map.put(event.getPlayer().getName(), block);
				} 
			} 
		} 
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[CC]")
				&& (event.getPlayer().hasPermission(PERMISSION) || event
						.getPlayer().isOp())) {
			Block chest = getAttachedBlock(event.getBlock());
			if (chest.getType().equals(Material.CHEST)) {
				event.getPlayer().sendMessage("§b[CC] §fChest initialized");
			}
		}
	}

	public Block getAttachedBlock(Block b) {
		MaterialData m = b.getState().getData();
		BlockFace face = BlockFace.DOWN;
		if (m instanceof Attachable) {
			face = ((Attachable) m).getAttachedFace();
		}
		return b.getRelative(face);
	}

	public boolean hasSignAttached(Block block) {
		return block.getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN
				|| block.getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN
				|| block.getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN
				|| block.getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN;
	}

	public void updateSign(Block block) {
		boolean dChest = false;
		ItemStack[] contents = null;
		Inventory chestInventory = ((Chest) block.getState()).getInventory();
		if (chestInventory instanceof DoubleChestInventory) {
			DoubleChest c = new DoubleChest(
					(DoubleChestInventory) chestInventory);
			contents = c.getInventory().getContents();
			dChest = true;
		} else {
			contents = chestInventory.getContents();
		}
		int amount = 0;
		if (contents != null) {
			for (ItemStack itemstack : contents) {
				if (itemstack == null) {
					continue;
				} else {
					amount += itemstack.getAmount();
				}
			}
		}
		Sign sign = null;
		Block sig = null;
		if (block.getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.NORTH);
		} else if (block.getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.SOUTH);
		} else if (block.getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.WEST);
		} else if (block.getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.EAST);
		} else if (block.getRelative(BlockFace.SOUTH_EAST).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.SOUTH_EAST);
		} else if (block.getRelative(BlockFace.SOUTH_WEST).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.SOUTH_WEST);
		} else if (block.getRelative(BlockFace.NORTH_EAST).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.NORTH_EAST);
		} else if (block.getRelative(BlockFace.NORTH_WEST).getType() == Material.WALL_SIGN) {
			sig = block.getRelative(BlockFace.NORTH_WEST);
		}
		sign = (Sign) sig.getState();
		if (sign.getLine(0).equalsIgnoreCase("[CC]")) {
			if (amount < 64) {
				sign.setLine(1, amount == 1 ? amount + " item" : amount
						+ " items");
				sign.setLine(2, "0 stacks");
				sign.setLine(3, "");
			} else {
				int k = amount % 64;
				int l = amount / 64;
				sign.setLine(1, k == 1 ? k + " item" : k + " items");
				sign.setLine(1, k == 0 ? "0 items" : (k == 1 ? k + " item" : k
						+ " items"));
				sign.setLine(2, l == 1 ? l + " stack" : l + " stacks");
				sign.setLine(3, "");
				if ((l == 27 && dChest == false) || (l == 54 && dChest == true)) {
					sign.setLine(3, "Full chest!");
				}
			}
			sign.update(true);
		}
	}
}
