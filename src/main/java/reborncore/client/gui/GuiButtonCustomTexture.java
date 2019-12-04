/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import reborncore.common.util.Color;

public class GuiButtonCustomTexture extends ButtonWidget {
	public int textureU;
	public int textureV;
	public String texturename;
	public String LINKED_PAGE;
	public String NAME;
	public String imageprefix = "techreborn:textures/manual/elements/";
	public int buttonHeight;
	public int buttonWidth;
	public int buttonU;
	public int buttonV;
	public int textureH;
	public int textureW;

	public GuiButtonCustomTexture(int xPos, int yPos, int u, int v, int buttonWidth, int buttonHeight,
	                              String texturename, String linkedPage, String name, int buttonU, int buttonV, int textureH, int textureW, ButtonWidget.PressAction pressAction) {
		super(xPos, yPos, buttonWidth, buttonHeight, "_", pressAction);
		this.textureU = u;
		this.textureV = v;
		this.texturename = texturename;
		this.NAME = name;
		this.LINKED_PAGE = linkedPage;
		this.buttonHeight = height;
		this.buttonWidth = width;
		this.buttonU = buttonU;
		this.buttonV = buttonV;
		this.textureH = textureH;
		this.textureW = textureW;
	}

	public void drawButton(MinecraftClient mc, int mouseX, int mouseY) {
		if (this.visible) {
			boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
				&& mouseY < this.y + this.height;
			mc.getTextureManager().bindTexture(WIDGETS_LOCATION);
			int u = textureU;
			int v = textureV;

			if (flag) {
				u += width;
				GL11.glPushMatrix();
				GL11.glColor4f(0f, 0f, 0f, 1f);
				this.blit(this.x, this.y, u, v, width, height);
				GL11.glPopMatrix();
			}
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(32826);
			DiffuseLighting.enable();
			renderImage(this.x, this.y);
			this.drawString(mc.textRenderer, this.NAME, this.x + 20, this.y + 3,
			                Color.WHITE.getColor());
		}
	}

	public void renderImage(int offsetX, int offsetY) {
		TextureManager render = MinecraftClient.getInstance().getTextureManager();
		render.bindTexture(new Identifier(imageprefix + this.texturename + ".png"));

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		blit(offsetX, offsetY, this.buttonU, this.buttonV, this.textureW, this.textureH);
		GL11.glDisable(GL11.GL_BLEND);
	}

}
