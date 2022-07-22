package squeek.squeedometer.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import squeek.squeedometer.config.ConfigWrapper;
import squeek.squeedometer.config.SqueedometerConfig;
import squeek.squeedometer.config.SqueedometerConfig.Position;

import javax.swing.text.JTextComponent;

@Environment(EnvType.CLIENT)
public class SqueedometerHud {
    
    // Vars
    private MinecraftClient client;
    private TextRenderer textRenderer;

    private int color = ConfigWrapper.config.textColor;
    private int vertColor = ConfigWrapper.config.textColor;
    private double lastFrameSpeed = 0.0;
    private double lastFrameVertSpeed = 0.0;
    private float tickCounter = 0.0f;

    public void draw(MatrixStack matrixStack, float tickDelta) {
        this.client = MinecraftClient.getInstance();
        this.textRenderer = client.textRenderer;

        // Calculating Speed
        Vec3d playerPosVec = client.player.getPos();
        double travelledX = playerPosVec.x - client.player.prevX;
        double travelledZ = playerPosVec.z - client.player.prevZ;
        double currentSpeed = (double)MathHelper.sqrt((float)(travelledX * travelledX + travelledZ * travelledZ));
        double currentVertSpeed = playerPosVec.y - client.player.prevY;

        if (ConfigWrapper.config.changeColors) {
            // Every tick determine if speeds are increasing or decreasing and set color accordingly   
            tickCounter += tickDelta;
            if (tickCounter >= (float)ConfigWrapper.config.tickInterval) {
                if (currentSpeed < lastFrameSpeed) {
                    color = ConfigWrapper.config.deceleratingColor;
                } else if (currentSpeed > lastFrameSpeed) {
                    color = ConfigWrapper.config.acceleratingColor;
                } else {
                    color = ConfigWrapper.config.textColor;
                }

                if (currentVertSpeed < lastFrameVertSpeed) {
                    vertColor = ConfigWrapper.config.deceleratingColor;
                } else if (currentVertSpeed > lastFrameVertSpeed) {
                    vertColor = ConfigWrapper.config.acceleratingColor;
                } else {
                    vertColor = ConfigWrapper.config.textColor;
                }

                lastFrameSpeed = currentSpeed;
                lastFrameVertSpeed = currentVertSpeed;
                tickCounter = 0.0f;
            }
        }

        String currentVertSpeedText = "";
        String currentSpeedText = "";
        // Convert speeds to text
        if (ConfigWrapper.config.showVertical) {
            currentVertSpeedText = String.format("Vertical: %s", SpeedCalculator.speedText(currentVertSpeed, ConfigWrapper.config.speedUnit));
            currentSpeedText = String.format("Horizontal: %s", SpeedCalculator.speedText(currentSpeed, ConfigWrapper.config.speedUnit));
        } else {
            currentSpeedText = SpeedCalculator.speedText(currentSpeed, ConfigWrapper.config.speedUnit);
        }
        // Calculate text position
        int horizWidth = this.textRenderer.getWidth(currentSpeedText);
        int vertWidth = this.textRenderer.getWidth(currentVertSpeedText);
        int height = this.textRenderer.fontHeight;
        int paddingX = 2;
        int paddingY = 2;
        int marginX = 4;
        int marginY = 4;
        int left = 0 + marginX;
        int vertLeft = 0 + marginX;
        int top = 0 + marginY;
        int realHorizWidth = horizWidth + paddingX * 2 - 1;
        int realVertWidth = vertWidth + paddingX * 2 - 1;
        int realHeight = height + paddingY * 2 - 1;

        if (ConfigWrapper.config.position == Position.BOTTOM_LEFT) {
            top += client.getWindow().getScaledHeight() - marginY * 2 - realHeight;

            left += paddingX;
            vertLeft += paddingX;
            top += paddingY;
        }

        if (ConfigWrapper.config.position == Position.BOTTOM_RIGHT) {
            top += client.getWindow().getScaledHeight() - marginY * 2 - realHeight;
            left += client.getWindow().getScaledWidth() - marginX * 2 - realHorizWidth;
            vertLeft += client.getWindow().getScaledWidth() - marginX * 2 - realVertWidth;

            left += paddingX;
            vertLeft += paddingX;
            top += paddingY;
        }

        if (ConfigWrapper.config.position == Position.TOP_LEFT) {
            left += paddingX;
            vertLeft += paddingX;
            top += paddingY;

            if (ConfigWrapper.config.showVertical) {
                top += 10;
            }
        }

        if (ConfigWrapper.config.position == Position.TOP_RIGHT) {
            left += client.getWindow().getScaledWidth() - marginX * 2 - realHorizWidth;
            vertLeft += client.getWindow().getScaledWidth() - marginX * 2 - realVertWidth;

            left += paddingX;
            vertLeft += paddingX;
            top += paddingY;

            if (ConfigWrapper.config.showVertical) {
                top += 10;
            }
        }

        // need it for traveling on private server, thanks babe <3
        // np, babe <3
        // im not schizo :)
        if (client.player.getWorld().getRegistryKey().equals(World.NETHER)
                && client.getCameraEntity().getPos().getY() >= 127
                && client.player.getVehicle() != null) {
            MinecraftClient.getInstance().options.forwardKey.setPressed(true);
            client.player.setYaw(-135);
            if (SpeedCalculator.metersPerSecond(currentSpeed) >= 8.5) {
                currentSpeedText = currentSpeedText + " [WORLDBORDER PROJECT]";
            }
        }

        // Render the text

        this.textRenderer.drawWithShadow(matrixStack, currentSpeedText, left, top, color);

        return;
    }
}