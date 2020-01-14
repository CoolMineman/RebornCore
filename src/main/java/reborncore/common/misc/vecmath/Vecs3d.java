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


package reborncore.common.misc.vecmath;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.StringTokenizer;

public class Vecs3d {
	protected double x, y, z;
	protected World w = null;

	public Vecs3d(double x, double y, double z) {

		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vecs3d(double x, double y, double z, World w) {

		this(x, y, z);
		this.w = w;
	}

	public Vecs3d(TileEntity te) {

		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), te.getWorld());
	}

	public Vecs3d(Vec3i vec) {

		this(vec.getX(), vec.getY(), vec.getZ());
	}

	public Vecs3d(Vec3i vec, World w) {

		this(vec.getX(), vec.getY(), vec.getZ());
		this.w = w;
	}

	public boolean hasWorld() {

		return w != null;
	}

	public Vecs3d add(double x, double y, double z) {

		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vecs3d add(EnumFacing dir) {

		return add(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
	}

	public Vecs3d add(Vecs3d vec) {

		return add(vec.x, vec.y, vec.z);
	}

	public Vecs3d sub(double x, double y, double z) {

		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vecs3d sub(EnumFacing dir) {

		return sub(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
	}

	public Vecs3d sub(Vecs3d vec) {

		return sub(vec.x, vec.y, vec.z);
	}

	public Vecs3d mul(double x, double y, double z) {

		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Vecs3d mul(double multiplier) {

		return mul(multiplier, multiplier, multiplier);
	}

	public Vecs3d mul(EnumFacing direction) {

		return mul(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
	}

	public Vecs3d multiply(Vecs3d v) {

		return mul(v.getX(), v.getY(), v.getZ());
	}

	public Vecs3d div(double x, double y, double z) {

		this.x /= x;
		this.y /= y;
		this.z /= z;
		return this;
	}

	public Vecs3d div(double multiplier) {

		return div(multiplier, multiplier, multiplier);
	}

	public Vecs3d div(EnumFacing direction) {

		return div(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
	}

	public double length() {

		return Math.sqrt(x * x + y * y + z * z);
	}

	public Vecs3d normalize() {

		Vecs3d v = clone();

		double len = length();

		if (len == 0)
			return v;

		v.x /= len;
		v.y /= len;
		v.z /= len;

		return v;
	}

	public Vecs3d abs() {

		return new Vecs3d(Math.abs(x), Math.abs(y), Math.abs(z));
	}

	public double dot(Vecs3d v) {

		return x * v.getX() + y * v.getY() + z * v.getZ();
	}

	public Vecs3d cross(Vecs3d v) {

		return new Vecs3d(y * v.getZ() - z * v.getY(), x * v.getZ() - z * v.getX(), x * v.getY() - y * v.getX());
	}

	public Vecs3d getRelative(double x, double y, double z) {

		return clone().add(x, y, z);
	}

	public Vecs3d getRelative(EnumFacing dir) {

		return getRelative(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
	}

	public EnumFacing getDirectionTo(Vecs3d vec) {

		for (EnumFacing d : EnumFacing.VALUES)
			if (getBlockX() + d.getXOffset() == vec.getBlockX()
				&& getBlockY() + d.getYOffset() == vec.getBlockY()
				&& getBlockZ() + d.getZOffset() == vec.getBlockZ())
				return d;
		return null;
	}

	public boolean isZero() {

		return x == 0 && y == 0 && z == 0;
	}

	@Override
	public Vecs3d clone() {

		return new Vecs3d(x, y, z, w);
	}

	public boolean hasTileEntity() {

		if (hasWorld()) {
			return w.getTileEntity(getBlockPos()) != null;
		}
		return false;
	}

	public BlockPos getBlockPos() {
		return new BlockPos(x, y, z);
	}

	public TileEntity getTileEntity() {

		if (hasTileEntity()) {
			return w.getTileEntity(getBlockPos());
		}
		return null;
	}

	public boolean isBlock(Block b) {

		return isBlock(b, false);
	}

	public boolean isBlock(Block b, boolean checkAir) {

		if (hasWorld()) {
			IBlockState state = w.getBlockState(getBlockPos());
			Block bl = state.getBlock();

			if (b == null && bl == Blocks.AIR)
				return true;
			if (b == null && checkAir && bl.getMaterial(state) == Material.AIR)
				return true;
			if (b == null && checkAir && bl.isAir(state, w, getBlockPos()))
				return true;

			return bl.getClass().isInstance(b);
		}
		return false;
	}

	public Block getBlock() {

		return getBlock(false);
	}

	public Block getBlock(boolean airIsNull) {

		if (hasWorld()) {
			if (airIsNull && isBlock(null, true))
				return null;
			return w.getBlockState(getBlockPos()).getBlock();

		}
		return null;
	}

	public World getWorld() {

		return w;
	}

	public Vecs3d setWorld(World world) {

		w = world;

		return this;
	}

	public double getX() {

		return x;
	}

	public double getY() {

		return y;
	}

	public double getZ() {

		return z;
	}

	public int getBlockX() {

		return (int) Math.floor(x);
	}

	public int getBlockY() {

		return (int) Math.floor(y);
	}

	public int getBlockZ() {

		return (int) Math.floor(z);
	}

	public double distanceTo(Vecs3d vec) {

		return distanceTo(vec.x, vec.y, vec.z);
	}

	public double distanceTo(double x, double y, double z) {

		double dx = x - this.x;
		double dy = y - this.y;
		double dz = z - this.z;
		return dx * dx + dy * dy + dz * dz;
	}

	public void setX(double x) {

		this.x = x;
	}

	public void setY(double y) {

		this.y = y;
	}

	public void setZ(double z) {

		this.z = z;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Vecs3d) {
			Vecs3d vec = (Vecs3d) obj;
			return vec.w == w && vec.x == x && vec.y == y && vec.z == z;
		}
		return false;
	}

	@Override
	public int hashCode() {

		return new Double(x).hashCode() + new Double(y).hashCode() << 8 + new Double(z).hashCode() << 16;
	}

	public Vec3i toVec3() {

		return new Vec3i(x, y, z);
	}

	@Override
	public String toString() {

		String s = "Vector3{";
		if (hasWorld())
			s += "w=" + w.provider.getDimension() + ";";
		s += "x=" + x + ";y=" + y + ";z=" + z + "}";
		return s;
	}

	public EnumFacing toForgeDirection() {

		if (z == 1)
			return EnumFacing.SOUTH;
		if (z == -1)
			return EnumFacing.NORTH;

		if (x == 1)
			return EnumFacing.EAST;
		if (x == -1)
			return EnumFacing.WEST;

		if (y == 1)
			return EnumFacing.UP;
		if (y == -1)
			return EnumFacing.DOWN;

		return null;
	}

	public static Vecs3d fromString(String s) {

		if (s.startsWith("Vector3{") && s.endsWith("}")) {
			World w = null;
			double x = 0, y = 0, z = 0;
			String s2 = s.substring(s.indexOf("{") + 1, s.lastIndexOf("}"));
			StringTokenizer st = new StringTokenizer(s2, ";");
			while (st.hasMoreTokens()) {
				String t = st.nextToken();

				if (t.toLowerCase().startsWith("w")) {
					int world = Integer.parseInt(t.split("=")[1]);
					if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
						// TODO 1.9
						// for (World wo :
						// MinecraftServer.getServer().worldServers) {
						// if (wo.provider.getDimension() == world) {
						// w = wo;
						// break;
						// }
						// }
					} else {
						w = getClientWorld(world);
					}
				}

				if (t.toLowerCase().startsWith("x"))
					x = Double.parseDouble(t.split("=")[1]);
				if (t.toLowerCase().startsWith("y"))
					y = Double.parseDouble(t.split("=")[1]);
				if (t.toLowerCase().startsWith("z"))
					z = Double.parseDouble(t.split("=")[1]);
			}

			if (w != null) {
				return new Vecs3d(x, y, z, w);
			} else {
				return new Vecs3d(x, y, z);
			}
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private static World getClientWorld(int world) {

		if (Minecraft.getMinecraft().world.provider.getDimension() != world)
			return null;
		return Minecraft.getMinecraft().world;
	}

}
