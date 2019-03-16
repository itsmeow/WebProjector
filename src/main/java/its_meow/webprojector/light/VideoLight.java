package its_meow.webprojector.light;

import java.nio.ByteBuffer;

import javax.swing.JFrame;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.mojang.blaze3d.platform.GlStateManager;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import therealfarfetchd.illuminate.client.api.Light;
import therealfarfetchd.illuminate.client.api.Lights;
import therealfarfetchd.qcommon.croco.Vec3;

public class VideoLight implements Light {

	public static final int noImageTexID;
	public final BlockPos bpos;
	public final Vec3 pos;
	public final Block block;

	public final float fov;
	public final float aspect;
	public final float near;

	public float yaw = 0F;

	private JFXPanel jfxPanel = null;
	private WebView browser = null;

	private int browserTexID = 0;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public VideoLight(BlockPos pos, Block block) {
		this.bpos = pos;
		this.pos = new Vec3(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
		this.block = block;
		this.fov = 35F;
		this.aspect = 768F/576F;
		this.near = (float) (Math.sqrt(2F) / 2F);
		this.wait = true;

		JFrame jFrame = new JFrame("WebProjector Browser");
		jfxPanel = new JFXPanel();
		jFrame.add(jfxPanel);
		jFrame.setVisible(true);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Platform.runLater(() -> {
			browser = new WebView();
			jfxPanel.setScene(browser.getScene());
			jFrame.setSize((int) browser.getWidth(), (int) browser.getHeight());

			browser.getEngine().getLoadWorker().stateProperty().addListener(
					new ChangeListener() {
						@Override
						public void changed(ObservableValue observable,
								Object oldValue, Object newValue) {
							if (State.SUCCEEDED == newValue) {
								captureView();
							}
						}
					});

			browser.getEngine().load("https://stackoverflow.com/questions/13487786/add-webview-control-on-swing-jframe");
		});
	}

	private void captureView() {
		NativeImage bi = new NativeImage(jfxPanel.getWidth(), jfxPanel.getHeight(), false);
		Graphics graphics = bi.createGraphics();
		jfxPanel.paint(graphics);
		this.setTexture(bi);
		graphics.dispose();
		bi.flush();
	}

	private int setTexture(BufferedImage bi) {

		int[] pixels = new int[bi.getWidth() * bi.getHeight()];
		bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), pixels, 0, bi.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(bi.getWidth() * bi.getHeight() * (bi.getType() == BufferedImage.TYPE_4BYTE_ABGR ? 4 : 3)); //4 for RGBA, 3 for RGB

		for(int y = 0; y < bi.getHeight(); y++){
			for(int x = 0; x < bi.getWidth(); x++){
				int pixel = pixels[y * bi.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
				buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
				buffer.put((byte) (pixel & 0xFF));               // Blue component
				buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
			}
		}

		buffer.flip();

		int texID = GL11.glGenTextures();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, bi.getWidth(), bi.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		//Setup wrap mode
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		//Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		//Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, bi.getWidth(), bi.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		return this.browserTexID = texID;
	}

	private boolean wait = true;

	public void prepare(float delta) {
		BlockState state = MinecraftClient.getInstance().world.getBlockState(bpos);
		if (state.getBlock() != block) {
			if (!wait) {
				Lights.getInstance().remove(this);
				return;
			}
		} else {
			wait = false;
		}

		yaw = -state.get(Properties.FACING_HORIZONTAL).asRotation();
	}

	@Override
	public Vec3 getPos() {
		return this.pos;
	}
	
	static {
		Identifier identifier = new Identifier("illuminate", "textures/test.png");
		MinecraftClient.getInstance().getTextureManager().bindTexture(identifier);
		GlStateManager.bindTexture(0);
		
		noImageTexID = MinecraftClient.getInstance().getTextureManager().getTexture(identifier).getGlId();
	}

	@Override
	public int getTex() {
		if(browser == null || browser.getEngine().getLocation().isEmpty()) {
			return noImageTexID;
		} else {

			BufferedImage test = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = test.createGraphics();

			g2d.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
			g2d.fillRect(0, 0, 128, 128); //A transparent white background

			g2d.setColor(Color.red);
			g2d.drawRect(0, 0, 127, 127); //A red frame around the image
			g2d.fillRect(10, 10, 10, 10); //A red box 

			g2d.setColor(Color.blue);
			g2d.drawString("Test image", 10, 64); //Some blue text

			return setTexture(test);
			//return this.browserTexID;
		}
	}

	@Override
	public float getYaw() {
		return this.yaw;
	}

	@Override
	public float getAspect() {
		return this.aspect;
	}

	@Override
	public float getFar() {
		return Light.super.getFar();
	}

	@Override
	public float getFov() {
		return this.fov;
	}

	@Override
	public float getNear() {
		return this.near;
	}

	@Override
	public float getPitch() {
		return Light.super.getPitch();
	}

	@Override
	public float getRoll() {
		return Light.super.getRoll();
	}

}