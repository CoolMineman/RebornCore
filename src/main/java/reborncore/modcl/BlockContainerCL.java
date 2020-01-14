/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */


package reborncore.modcl;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by Prospector
 */
public abstract class BlockContainerCL extends BlockContainer {
	public ModCL mod;
	public String name;
	public Class<? extends TileEntity> tileEntity;

	public BlockContainerCL(ModCL mod, String name, Material material, boolean registerModel, Class<? extends TileEntity> tileEntity) {
		super(material);
		setInfo(mod, name, tileEntity);
		if (registerModel) {
			mod.blockModelsToRegister.add(this);
		}
	}

	public BlockContainerCL(ModCL mod, String name, boolean registerModel, Class<? extends TileEntity> tileEntity) {
		this(mod, name, Material.ROCK, registerModel, tileEntity);
	}

	public BlockContainerCL(ModCL mod, String name, Material material, Class<? extends TileEntity> tileEntity) {
		this(mod, name, material, true, tileEntity);
	}

	public BlockContainerCL(ModCL mod, String name, Class<? extends TileEntity> tileEntity) {
		this(mod, name, Material.ROCK, true, tileEntity);
	}

	private void setInfo(ModCL mod, String name, Class<? extends TileEntity> tileEntity) {
		this.mod = mod;
		this.name = name;
		this.tileEntity = tileEntity;
		setTranslationKey(mod.getPrefix() + name);
		setRegistryName(mod.getModID(), name);
		setCreativeTab(mod.getTab());
	}
}
