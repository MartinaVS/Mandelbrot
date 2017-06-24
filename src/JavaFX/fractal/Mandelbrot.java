package JavaFX.fractal;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Mandelbrot extends Application implements Fractal {
    private double R = 1000000000;
    private double R2 = R * R;
    private double x0 = -2;
    private double y0 = 2;
    private double dx = 4 / 640.0;
    private int width = 640;
    private int height = 480;
    private final static FileChooser fileChooser = new FileChooser();

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane pane = createInterface(primaryStage);
        Scene scene = new Scene(pane, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Pane createInterface(Stage primaryStage) {
        Pane pane = new VBox();

        //поле для рисования
        ImageView imageView = new ImageView();
        pane.getChildren().add(imageView);
        WritableImage wi = new WritableImage(width, height);
        imageView.setImage(wi);
        PixelWriter pw = wi.getPixelWriter();

        //кнопки
        HBox hBox1 = new HBox();
        HBox hBox2 = new HBox();
        Button left = new Button("Влево");
        Button right = new Button("Вправо");
        Button up = new Button("Вверх");
        Button down = new Button("Вниз");
        Button plus = new Button("+");
        Button minus = new Button("-");
        Button saveBt = new Button("Сохранить картинку");
        hBox1.getChildren().addAll(left, right, up, down, plus, minus);
        hBox2.getChildren().add(saveBt);

        //поле для палитры
        ImageView ivForHB = new ImageView();
        HBox hBox3 = new HBox();
        WritableImage wiForHB = new WritableImage(500, 50);
        ivForHB.setImage(wiForHB);
        PixelWriter pwForHB = wiForHB.getPixelWriter();
        hBox3.getChildren().add(ivForHB);
        drawPaletteHSB(pwForHB);
        pane.getChildren().add(hBox3);
        pane.getChildren().add(hBox1);
        pane.getChildren().add(hBox2);
        draw(x0, y0, pw);

        //движение влево-вправо-вверх-вниз по кнопкам
        left.setOnAction(event -> {
            x0 = x0 - width * dx / 5;
            draw(x0, y0, pw);
        });
        right.setOnAction(event -> {
            x0 = x0 + width * dx / 5;
            draw(x0, y0, pw);
        });
        up.setOnAction(event -> {
            y0 = y0 + height * dx / 5;
            draw(x0, y0, pw);
        });
        down.setOnAction(event -> {
            y0 = y0 - height * dx / 5;
            draw(x0, y0, pw);
        });

        //приближение и отдаление
        plus.setOnAction(event -> {
            double dxOld = dx;
            dx = dx / 1.5;
            y0 -= 1 / 2 * height * (dxOld - dx);
            x0 += 1 / 2 * width * (dxOld - dx);
            draw(x0, y0, pw);
        });
        minus.setOnAction(event -> {
            double dxOld = dx;
            dx = dx * 1.5;
            y0 = y0 - 1 / 2 * (height * dxOld - height * dx);
            x0 = x0 + 1 / 2 * (width * dxOld - width * dx);
            draw(x0, y0, pw);
        });

        //Сохранение картинки
        saveBt.setOnAction(event -> saveImage(imageView.getImage(), primaryStage));

        return pane;
    }

    //заполнение и рисование изначального фрактала
    public void draw(double x0, double y0, PixelWriter pw) {
        double x;
        double y;
        for (int xs = 0; xs < width; xs++) {
            x = x0 + xs * dx;
            for (int ys = 0; ys < height; ys++) {
                y = y0 - ys * dx;
                pw.setColor(xs, ys, paletteHSB.eval(countMandelbrot(x, y)));
            }
        }
    }

    //подсчет фрактала
    private double countMandelbrot(double cReal, double cImag) {
        double zReal = 0;
        double zImag = 0;
        int N = 1000;

        for (int step = 0; step < N; step++) {
            //квадрат z
            double tZReal = zReal;
            zReal = zReal * zReal - zImag * zImag;
            zImag = tZReal * zImag + tZReal * zImag;
            //прибавляем c
            zReal += cReal;
            zImag += cImag;
            //модуль z  в квадрате
            double modZsq = zReal * zReal + zImag * zImag;
            if (modZsq > R2)
                return (double) step / N;
        }
        return 1;
    }

    //рисование отдельной панели-палитры
    private void drawPaletteHSB(PixelWriter pw) {
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 50; y++) {
                pw.setColor(x, y, paletteHSB.eval(x / 500.0));
            }
        }
    }

    //Черно-белая палитра
    private Palette paletteBW = i -> Color.rgb((int) Math.round(i * 255), (int) Math.round(i * 255), (int) Math.round(i * 255));

    //цветная палитра
    private Palette paletteColor = i -> {
        if (i < 1.0 / 3)
            return Color.color(3 * i, 0, 0);
        else if (i < 2.0 / 3)
            return Color.color(1, 3 * i - 1, 0);
        else
            return Color.color(1, 1, 3 * i - 2);
    };

    //палитра HSB
    private Palette paletteHSB = v -> {
        double b = 1 - (int) (v * 10) / 10.0;
        return Color.hsb(360 * v * 10, 1, b);
    };

    private void saveImage(Image image, Stage primaryStage) {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        fileChooser.setTitle("Save Image");
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }

}