package com.project;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.project.clases.Joystick;


public class GameScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private SpriteBatch uiBatch;
    private ShapeRenderer shapeRenderer;
    private ShapeRenderer uiShapeRenderer;
    private BitmapFont font, titleFont;
    private Texture backgroundTexture;

    private OrthographicCamera camera;

    private float playerX, playerY;
    private Vector2 movementOutput;

    private Joystick joystick;

    private Texture torchRed;
    private TextureRegion[][] torchFrames;

    private float animationTimer = 0f;
    private float frameDuration = 0.1f; // 10 fps

    private boolean facingRight = true;

    public GameScreen(Game game) {
        this.game = game;

        movementOutput = new Vector2();
        playerX = 500;
        playerY = 400;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1000, 800);

        batch = new SpriteBatch(); // para el mundo
        uiBatch = new SpriteBatch(); // para la UI

        shapeRenderer = new ShapeRenderer(); // para mundo
        uiShapeRenderer = new ShapeRenderer(); // para UI

        font = new BitmapFont();
        titleFont = new BitmapFont();

        joystick = new Joystick(175, 175, 75);

        initTextures();
    }

    private void initTextures() {
        torchRed = new Texture("Torch_Red.png");
        backgroundTexture = new Texture("background.jpg"); // Cambia esto si tu fondo tiene otro nombre
        torchFrames = extractFrames(torchRed, 192, 192, 8, 6);
    }

    private TextureRegion[][] extractFrames(Texture sheet, int frameWidth, int frameHeight, int totalRows, int framesPerRow) {
        TextureRegion[][] allFrames = new TextureRegion[totalRows][framesPerRow];
        for (int row = 0; row < totalRows; row++) {
            for (int col = 0; col < framesPerRow; col++) {
                allFrames[row][col] = new TextureRegion(sheet, col * frameWidth, row * frameHeight, frameWidth, frameHeight);
            }
        }
        return allFrames;
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        animationTimer += delta;
        ScreenUtils.clear(0.18f, 0.506f, 0.2f, 1f);

        // Movimiento del jugador
        float speed = 200f;
        playerX += movementOutput.x * speed * delta;
        playerY += movementOutput.y * speed * delta;

        // Cámara sigue al jugador
        camera.position.set(playerX, playerY, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        drawMap();
        drawLocalPlayer();

        // UI fija (sin cámara)
        uiShapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        uiBatch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        // Dibujar joystick
        uiShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        joystick.draw(uiShapeRenderer);
        uiShapeRenderer.end();

        // Actualizar movimiento del joystick
        Vector2 touchPosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        movementOutput = joystick.update(touchPosition);

        // Actualizar dirección del personaje
        if (movementOutput.x < -0.1f) {
            facingRight = false;
        } else if (movementOutput.x > 0.1f) {
            facingRight = true;
        }

    }

    private void drawMap() {
        batch.begin();
        batch.draw(backgroundTexture, 0, 0);
        batch.end();
    }

    private void drawLocalPlayer() {
        batch.begin();

        int row = movementOutput.isZero(0.1f) ? 0 : 1;
        int frameIndex = ((int)(animationTimer / frameDuration)) % 6;
        TextureRegion frame = torchFrames[row][frameIndex];

        float scale = 0.85f;
        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;
        float drawX = playerX - (96 * scale);
        float drawY = playerY - (96 * scale);

        // Si está mirando hacia la izquierda, invertimos horizontalmente
        if (!facingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        } else if (facingRight && frame.isFlipX()) {
            frame.flip(true, false);
        }

        batch.draw(frame, drawX, drawY, width, height);

        batch.end();
    }



    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        uiBatch.dispose();
        shapeRenderer.dispose();
        uiShapeRenderer.dispose();
        font.dispose();
        titleFont.dispose();
        backgroundTexture.dispose();
        torchRed.dispose();
    }
}
