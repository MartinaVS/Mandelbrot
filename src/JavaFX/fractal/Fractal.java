package JavaFX.fractal;

import javafx.scene.image.PixelWriter;

public interface Fractal {
    void draw(double x0, double y0, PixelWriter pw);
}
