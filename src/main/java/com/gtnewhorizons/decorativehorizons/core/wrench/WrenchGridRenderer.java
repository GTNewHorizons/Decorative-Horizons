package com.gtnewhorizons.decorativehorizons.core.wrench;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class WrenchGridRenderer {

    private static final int RED = 0, GREEN = 0, BLUE = 0, ALPHA = 127;
    private static final float LINE_WIDTH = 2.5F;
    private static final int ROTATION_MARKER_RESOLUTION = 120;
    // the block's faces, pushed out by 0.002 so the lines draw over the block
    private static final double LO = -0.002, HI = 1.002;
    private static final double[] FACES = { LO, HI };
    private static final double[] GRID_LINES = { 0.25, 0.75 };

    /**
     * Replaces the block outline with GT's wrench grid while the player has a wrench in hand and looks at an
     * {@link IWrenchable} block with a grid.
     */
    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        MovingObjectPosition target = event.target;
        if (event.subID != 0 || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        EntityPlayer player = event.player;
        Block block = player.worldObj.getBlock(target.blockX, target.blockY, target.blockZ);
        if (!(block instanceof IWrenchable wrenchable) || !wrenchable.hasWrenchGrid()
            || !Wrench.isWrench(event.currentItem)) return;
        event.setCanceled(true);

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(0);
        GL11.glLineWidth(LINE_WIDTH * (Minecraft.getMinecraft().displayHeight / 1080F));

        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;
        GL11.glTranslated(target.blockX - camX, target.blockY - camY, target.blockZ - camZ);

        drawOutline();
        ForgeDirection face = ForgeDirection.getOrientation(target.sideHit);
        drawGrid(face);
        if (player.isSneaking() && wrenchable.rotatesWithWrench()) drawRotationMarker(face);

        GL20.glUseProgram(program);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private static void drawOutline() {
        Tessellator tess = Tessellator.instance;
        for (double y : FACES) {
            tess.startDrawing(GL11.GL_LINE_STRIP);
            tess.setColorRGBA(RED, GREEN, BLUE, ALPHA);
            tess.addVertex(LO, y, LO);
            tess.addVertex(HI, y, LO);
            tess.addVertex(HI, y, HI);
            tess.addVertex(LO, y, HI);
            tess.addVertex(LO, y, LO);
            tess.draw();
        }
        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA(RED, GREEN, BLUE, ALPHA);
        for (double x : FACES) {
            for (double z : FACES) {
                tess.addVertex(x, LO, z);
                tess.addVertex(x, HI, z);
            }
        }
        tess.draw();
    }

    private static void drawGrid(ForgeDirection face) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINES);
        tess.setColorRGBA(RED, GREEN, BLUE, ALPHA);
        for (double a : GRID_LINES) {
            addFaceVertex(face, a, 0);
            addFaceVertex(face, a, 1);
            addFaceVertex(face, 0, a);
            addFaceVertex(face, 1, a);
        }
        tess.draw();
    }

    // GT's rotation marker (compileRotationMarkerDisplayList), scaled to the center cell
    private static void drawRotationMarker(ForgeDirection face) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.setColorRGBA(RED, GREEN, BLUE, ALPHA);
        for (int i = 0; i <= ROTATION_MARKER_RESOLUTION; i++) {
            double a = i * Math.PI * 1.75 / ROTATION_MARKER_RESOLUTION;
            addMarkerVertex(face, Math.cos(a) * 0.4, Math.sin(a) * 0.4);
        }
        for (int i = ROTATION_MARKER_RESOLUTION; i >= 0; i--) {
            double a = i * Math.PI * 1.75 / ROTATION_MARKER_RESOLUTION;
            addMarkerVertex(face, Math.cos(a) * 0.24, Math.sin(a) * 0.24);
        }
        addMarkerVertex(face, 0.141114561800, 0);
        addMarkerVertex(face, 0.32, -0.178885438199);
        addMarkerVertex(face, 0.498885438199, 0);
        tess.draw();
    }

    private static void addMarkerVertex(ForgeDirection face, double x, double z) {
        addFaceVertex(face, 0.5 + x * 0.5, 0.5 + z * 0.5);
    }

    private static void addFaceVertex(ForgeDirection face, double u, double v) {
        Tessellator tess = Tessellator.instance;
        double n = (face.offsetX + face.offsetY + face.offsetZ) > 0 ? HI : LO;
        if (face.offsetY != 0) tess.addVertex(u, n, v);
        else if (face.offsetZ != 0) tess.addVertex(u, v, n);
        else tess.addVertex(n, v, u);
    }
}
