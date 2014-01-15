package com.droppages.Skepter;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestCounter extends JavaPlugin implements Listener {

	String PERMISSION = "CC.use";
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	public void onDisable() {
	}

	HashMap<Player, Block> map = new HashMap<Player, Block>();
	
	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		if(map.containsKey(event.getPlayer())) {
			updateSign(map.get(event.getPlayer()));
		}
	}
	
	@EventHandler
	public void onUpdateSign(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if (hasSignAttacked(block)) {
				if (block.getState().getType() == Material.CHEST) {
					map.put(event.getPlayer(), block);
				} else {
					return;
				}
			} else {
				return;
			}
		} else {
			return;
		}
	}
	
	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (event.getLine(0).equalsIgnoreCase("[CC]")
				&& (event.getPlayer().hasPermission(PERMISSION) || event
						.getPlayer().isOp())) {
			Block chest = getAttachedBlock(event.getBlock());
			if (chest.getType().equals(Material.CHEST)) {
				event.getPlayer().sendMessage("[CC] Chest initialized");
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
	
	public boolean hasSignAttacked(Block block) {
		if(block.getRelative(BlockFace.NORTH).getType() == Material.WALL_SIGN
				|| block.getRelative(BlockFace.SOUTH).getType() == Material.WALL_SIGN
				|| block.getRelative(BlockFace.WEST).getType() == Material.WALL_SIGN
				|| block.getRelative(BlockFace.EAST).getType() == Material.WALL_SIGN) {
			return true;
		} else {
			return false;
		}
	}
	
	public void updateSign(Block block) {
		ItemStack[] contents = ((Chest) block.getState()).getInventory().getContents();
		int amount = 0;
		if(contents != null) {
			for(ItemStack itemstack : contents) {
				if(itemstack == null) {
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
		}
		sign = (Sign) sig.getState();
		if(sign.getLine(0).equalsIgnoreCase("[CC]")) {
			if (amount < 64) {
				sign.setLine(1, amount == 1 ? amount + " item" : amount + " items");
				sign.setLine(2, "0 stacks");
				sign.setLine(3, "");
			} else {
				int k = amount % 64;
				int l = amount / 64;
				sign.setLine(1, k == 1 ? k + " item" : k + " items");
				sign.setLine(1, k == 0 ? "0 items" : (k == 1 ? k + " item" : k + " items"));
				sign.setLine(2, l == 1 ? l + " stack" : l + " stacks");
				sign.setLine(3, "");
				if(l == 27) {
					sign.setLine(3, "Full chest!");
				}
			}
			sign.update(true);
		}
	}
}