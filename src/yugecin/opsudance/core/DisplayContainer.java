/*
 * opsu!dance - fork of opsu! with cursordance auto
 * Copyright (C) 2017 yugecin
 *
 * opsu!dance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * opsu!dance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with opsu!dance.  If not, see <http://www.gnu.org/licenses/>.
 */
package yugecin.opsudance.core;

import com.google.inject.Inject;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.opengl.InternalTextureLoader;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;
import org.newdawn.slick.util.Log;
import yugecin.opsudance.core.state.OpsuState;
import yugecin.opsudance.utils.GLHelper;

import java.util.LinkedList;
import java.util.List;

import static yugecin.opsudance.kernel.Entrypoint.log;

/**
 * based on org.newdawn.slick.AppGameContainer
 */
public class DisplayContainer {

	private static SGL GL = Renderer.get();

	private final Demux demux;
	private final DisplayMode nativeDisplayMode;
	private final List<ResolutionChangeListener> resolutionChangeListeners;

	private Graphics graphics;
	private Input input;

	public int width;
	public int height;

	public int targetRenderInterval;
	public int targetBackgroundRenderInterval;

	public int realRenderInterval;
	public int delta;

	public int timeSinceLastRender;

	private long lastFrame;

	@Inject
	public DisplayContainer(Demux demux) {
		this.demux = demux;
		this.nativeDisplayMode = Display.getDisplayMode();
		this.resolutionChangeListeners = new LinkedList<>();
		targetRenderInterval = 16; // ~60 fps
		targetBackgroundRenderInterval = 41; // ~24 fps
		lastFrame = getTime();
	}

	public void addResolutionChangeListener(ResolutionChangeListener listener) {
		resolutionChangeListeners.add(listener);
	}

	public void switchState(OpsuState newState) {
		demux.switchState(newState);
	}

	public void switchState(Class<? extends OpsuState> newState) {
		demux.switchState(newState);
	}

	public void run() throws LWJGLException {
		demux.init();
		setup();
		log("GL ready");
		while(!(Display.isCloseRequested() && demux.onCloseRequest())) {
			delta = getDelta();

			timeSinceLastRender += delta;

			input.poll(width, height);

			int maxRenderInterval;
			if (Display.isVisible() && Display.isActive()) {
				maxRenderInterval = targetRenderInterval;
			} else {
				maxRenderInterval = targetBackgroundRenderInterval;
			}

			if (timeSinceLastRender >= maxRenderInterval) {
				GL.glClear(SGL.GL_COLOR_BUFFER_BIT);

				/*
				graphics.resetTransform();
				graphics.resetFont();
				graphics.resetLineWidth();
				graphics.resetTransform();
				*/

				demux.update(timeSinceLastRender);
				demux.render(graphics);

				realRenderInterval = timeSinceLastRender;
				timeSinceLastRender = 0;

				Display.update(false);
			}

			Display.processMessages();
			Display.sync(1000);
		}
		teardown();
	}

	private void setup() {
		Input.disableControllers();
		Display.setTitle("opsu!dance");
		try {
			// temp displaymode to not flash the screen with a 1ms black window
			Display.setDisplayMode(new DisplayMode(100, 100));
			Display.create();
			GLHelper.setIcons(new String[] { "icon16.png", "icon32.png" });
			setDisplayMode(640, 480, false);
		} catch (LWJGLException e) {
			e.printStackTrace();
			// TODO errorhandler dialog here
			Log.error("could not initialize GL", e);
		}
	}

	private void teardown() {
		Display.destroy();
		AL.destroy();
	}

	public void setDisplayMode(int width, int height, boolean fullscreen) throws LWJGLException {
		if (this.width == width && this.height == height) {
			Display.setFullscreen(fullscreen);
			return;
		}

		DisplayMode displayMode = null;
		if (fullscreen) {
			displayMode = GLHelper.findFullscreenDisplayMode(nativeDisplayMode.getBitsPerPixel(), nativeDisplayMode.getFrequency(), width, height);
		}

		if (displayMode == null) {
			displayMode = new DisplayMode(width,height);
			if (fullscreen) {
				fullscreen = false;
				Log.warn("could not find fullscreen displaymode for " + width + "x" + height);
			}
		}

		this.width = displayMode.getWidth();
		this.height = displayMode.getHeight();

		Display.setDisplayMode(displayMode);
		Display.setFullscreen(fullscreen);

		initGL();

		for (ResolutionChangeListener resolutionChangeListener : resolutionChangeListeners) {
			resolutionChangeListener.onDisplayResolutionChanged(width, height);
		}

		if (displayMode.getBitsPerPixel() == 16) {
			InternalTextureLoader.get().set16BitMode();
		}
	}

	private void initGL() {
		GL.initDisplay(width, height);
		GL.enterOrtho(width, height);

		graphics = new Graphics(width, height);
		graphics.setAntiAlias(false);

		input = new Input(height);
		input.addKeyListener(demux);
		input.addMouseListener(demux);
	}

	private int getDelta() {
		long time = getTime();
		int delta = (int) (time - lastFrame);
		lastFrame = time;
		return delta;
	}

	public long getTime() {
		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
	}

}
