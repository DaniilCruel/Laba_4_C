package Laba_4C;


import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;

import static java.lang.Math.abs;

public class GraphicsDisplay extends JPanel {

    private Font axisFont;             // шрифт подписей к осям коорд

    private Double[][] graphicsData;         // список коорд точек для построения графика

    // различные стили черчения линий
    private BasicStroke axisStroke;
    private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;

    // границы диапазона пространства,подлежащего отображению
    private double maxX;
    private double maxY;
    private double minX;
    private double minY;
    private double scale;
    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;

    public GraphicsDisplay() {
        // Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
        // Сконструировать необходимые объекты, используемые в рисовании
        // Перо для рисования графика
        graphicsStroke = new BasicStroke(4.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 10.0f, new float[]{12,6,12,6,12,4,4,4,4,4}, 0.0f);
        // Перо для рисования осей координат
        axisStroke = new BasicStroke(3.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE ,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    public void showGraphics(Double[][] graphicsData) {
        // Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
        // Запросить перерисовку компонента (неявно вызвать paintComponent())
        repaint();
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        // Вычисляем смещение X от самой левой точки (minX)
        double deltaX = x - minX;
        // Вычисляем смещение Y от точки верхней точки (maxY)
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        // Инициализировать новый экземпляр точки
        Point2D.Double dest = new Point2D.Double();
        // Задать еѐ координаты как координаты существующей точки +
        // заданные смещения
        dest.setLocation(src.getX() + deltaX, src.getY() + deltaY);
        return dest;
    }

    protected void paintGraphics(Graphics2D canvas) {
        // Выбрать линию для рисования графика
        canvas.setStroke(graphicsStroke);
        // Выбрать цвет линии
        canvas.setColor(Color.RED);
        /* Будем рисовать линию графика как путь, состоящий из множества
        сегментов (GeneralPath). Начало пути устанавливается в первую точку
        графика, после чего прямой соединяется со следующими точками */
        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            // Преобразовать значения (x,y) в точку на экране point
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
                // Не первая итерация – вести линию в точку point
                graphics.lineTo(point.getX(), point.getY());
            } else {
                // Первая итерация - установить начало пути в точку point
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        // Отобразить график
        canvas.draw(graphics);
    }

    protected void paintAxis(Graphics2D canvas) {
        // Шаг 1 – установить необходимые настройки рисования
        // Установить особое начертание для осей
        canvas.setStroke(axisStroke);
        // Оси рисуются чѐрным цветом
        canvas.setColor(Color.BLACK);
        // Стрелки заливаются чѐрным цветом
        canvas.setPaint(Color.BLACK);
        // Подписи к координатным осям делаются специальным шрифтом
        canvas.setFont(axisFont);
        // Создать объект контекста отображения текста - для получения
        // характеристик устройства (экрана)
        FontRenderContext context = canvas.getFontRenderContext();
        // Шаг 2 - Определить, должна ли быть видна ось Y на графике
        if (minX <= 0.0 && maxX >= 0.0) {
            // Она видна, если левая граница показываемой области minX<=0.0,
            // а правая (maxX) >= 0.0
            // Шаг 2а - ось Y - это линия между точками (0, maxY) и (0, minY)
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));
            // Шаг 2б - Стрелка оси Y
            GeneralPath arrow = new GeneralPath();
            // Установить начальную точку ломаной точно на верхний конец оси Y
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            // Вести левый "скат" стрелки в точку с относительными
            // координатами (5,20)
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            // Вести нижнюю часть стрелки в точку с относительными
            // координатами (-10, 0)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            // Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
            // Шаг 2в - Нарисовать подпись к оси Y
            // Определить, сколько места понадобится для надписи “y”
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            // Вывести надпись в точке с вычисленными координатами
            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }
        // Шаг 3 - Определить, должна ли быть видна ось X на графике
        if (minY <= 0.0 && maxY >= 0.0) {
            // Она видна, если верхняя граница показываемой области max)>=0.0,
            // а нижняя (minY) <= 0.0
            // Шаг 3а - ось X - это линия между точками (minX, 0) и (maxX, 0)
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0),
                    xyToPoint(maxX, 0)));
            // Шаг 3б - Стрелка оси X
            GeneralPath arrow = new GeneralPath();
            // Установить начальную точку ломаной точно на правый конец оси X
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            // Вести верхний "скат" стрелки в точку с относительными
            // координатами (-20,-5)
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20,
                    arrow.getCurrentPoint().getY() - 5);
            // Вести левую часть стрелки в точку
            // с относительными координатами (0, 10)
            arrow.lineTo(arrow.getCurrentPoint().getX(),
                    arrow.getCurrentPoint().getY() + 10);
            // Замкнуть треугольник стрелки
            arrow.closePath();
            canvas.draw(arrow); // Нарисовать стрелку
            canvas.fill(arrow); // Закрасить стрелку
            // Шаг 3в - Нарисовать подпись к оси X
            // Определить, сколько места понадобится для надписи “x”
            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            // Вывести надпись в точке с вычисленными координатами
            canvas.drawString("x",
                    (float) (labelPos.getX() - bounds.getWidth() - 10),
                    (float) (labelPos.getY() + bounds.getY()));
        }
    }

    protected void paintMarkers(Graphics2D canvas) {


        // Шаг 2 - Организовать цикл по всем точкам графика
        for (Double[] point : graphicsData) {
            boolean temp = true;
            double znach = point[1];
            double S=0 ;

            double cifr1 = znach % 10;
            znach=Math.floor(znach/10);

            while (abs(znach) > 0) {
                S=S+abs(cifr1);
                System.out.println(S);
                cifr1=znach % 10;
                znach=Math.floor(znach/10);
                if (S>10) {

                    temp = false;
                    break;
                }

            }
            if (true) {
                // Выбрать красный цвета для контуров маркеров
                canvas.setColor(Color.BLACK);
                canvas.setPaint(Color.BLACK);


            Ellipse2D.Double marker = new Ellipse2D.Double();

            Point2D.Double center = xyToPoint(point[0], point[1]);

            Point2D.Double corner = shiftPoint(center, 10, 10);

            marker.setFrameFromCenter(center, corner);
            canvas.draw(marker); // Начертить контур маркера

            canvas.setStroke(markerStroke);
            canvas.draw(new Line2D.Double(shiftPoint(center, -8, 0), shiftPoint(center, 8, 0)));
            canvas.draw(new Line2D.Double(shiftPoint(center, 0, 8), shiftPoint(center, 0, -8)));
            }


        }
    }

    public void paintComponent(Graphics g) {
        /* Шаг 1 - Вызвать метод предка для заливки области цветом заднего фона
         * Эта функциональность - единственное, что осталось в наследство от
         * paintComponent класса JPanel */
        super.paintComponent(g);
        // Шаг 2 - Если данные графика не загружены (при показе компонента при
        // запуске программы) - ничего не делать
        if (graphicsData == null || graphicsData.length == 0) return;
        // Шаг 3 - Определить начальные границы области отображения
        // Еѐ верхний левый угол - (minX, maxY), правый нижний - (maxX, minY)
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;
        // Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }
        /* Шаг 4 - Определить (исходя из размеров окна) масштабы по осям X и Y –
        сколько пикселов приходится на единицу длины по X и по Y */
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);
        // Выбрать единый масштаб как минимальный из двух
        scale = Math.min(scaleX, scaleY);
        // Шаг 5 - корректировка границ области согласно выбранному масштабу
        if (scale == scaleX) {
            /* Если за основу был взят масштаб по оси X, значит по оси Y
            делений меньше, т.е. подлежащий отображению диапазон по Y будет меньше
            высоты окна. Значит необходимо добавить делений, сделаем это так:
            1) Вычислим, сколько делений влезет по Y при выбранном масштабе -
            getSize().getHeight()/scale;
            2) Вычтем из этого значения сколько делений требовалось изначально;
            3) Набросим по половине недостающего расстояния на maxY и minY */
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            // Если за основу был взят масштаб по оси Y, действовать по аналогии
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }
        // Шаг 5 – Преобразовать экземпляр Graphics к Graphics2D
        Graphics2D canvas = (Graphics2D) g;
        // Шаг 6 - Сохранить текущие настройки холста
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();
        // Шаг 8 - В нужном порядке вызвать методы отображения элементов графика
        // Порядок вызова методов имеет значение, т.к. предыдущий рисунок будет
        // затираться последующим
        // Первым (если нужно) отрисовываются оси координат.
        if (showAxis) paintAxis(canvas);
        // Затем отображается сам график
        paintGraphics(canvas);
        // Затем (если нужно) отображаются маркеры точек графика.
        if (showMarkers) paintMarkers(canvas);
        // Шаг 9 - Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }


    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

}



