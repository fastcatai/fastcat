package ui;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
//import settings.SettingsProperties;

public class UIProperties {
    public static final Color ANCHOR_POINT_DEFAULT_FILL = Color.GOLD.deriveColor(1, 1, 1, 0.5);
    public static final Color ANCHOR_POINT_DEFAULT_STROKE = Color.GOLD;
    public static final Color ANCHOR_POINT_HOVER_FILL = Color.RED.deriveColor(1, 1, 1, 0.5);
    public static final Color ANCHOR_POINT_HOVER_STROKE = Color.RED;
    public static final Color ANCHOR_POINT_FIRST_FILL = Color.BLUE.deriveColor(1, 1, 1, 0.5);
    public static final Color ANCHOR_POINT_FIRST_STROKE = Color.BLUE;
    public static final double ANCHOR_POINT_DEFAULT_RADIUS = 5.0;
    public static final double ANCHOR_POINT_HOVER_RADIUS = 10.0;

    public static final double BOX_STROKE_WIDTH = 2.0;

//    public static Color ANNOTATION_FILL_DEFAULT = Color.valueOf(SettingsProperties.getProperty(SettingsProperties.ANNOTATION_COLOR_FILL_DEFAULT));
    public static Color ANNOTATION_FILL_HOVER_DEFAULT = Color.rgb(255, 255, 255, 0.2);
    public static Color ANNOTATION_STROKE_COLOR_DEFAULT = Color.RED;
    public static Color ANNOTATION_STROKE_HOVER_DEFAULT = Color.RED;
    public static Color BOX_LABEL_COLOR_DEFAULT = Color.rgb(255, 0, 0, 0.2);

    public static final Color BOX_STROKE_COLOR_VERIFIED = Color.BLUE;
    public static final Color BOX_LABEL_COLOR_VERIFIED = Color.BLUE;
    public static final Color BOX_FILL_COLOR_VERIFIED = Color.rgb(0, 0, 255, 0);
    public static final Color BOX_FILL_COLOR_HOVER_VERIFIED = Color.rgb(0, 0, 255, 0.2);

    public static final Color BOX_STROKE_COLOR_SELECTED = Color.GREEN;
    public static final Color BOX_LABEL_COLOR_SELECTED = Color.GREEN;
    public static final Color BOX_FILL_COLOR_SELECTED = Color.rgb(0, 255, 0, 0.2);
    public static final Color BOX_FILL_COLOR_HOVER_SELECTED = Color.rgb(255, 255, 255, 0.2);

    public static final Color BOX_FILL_COLOR_EDIT = Color.rgb(255, 255, 255, 0);
    public static final Color BOX_FILL_COLOR_HOVER_EDIT = Color.rgb(255, 255, 255, 0.2);

    public static final Color BOX_STROKE_COLOR_AUTO = Color.ORANGE;
    public static final Color BOX_LABEL_COLOR_AUTO = Color.ORANGE;
    public static final Color BOX_FILL_COLOR_AUTO = Color.rgb(255, 165, 0, 0);
    public static final Color BOX_FILL_COLOR_HOVER_AUTO = Color.rgb(255, 165, 0, 0.2);

    public static final Color POINT_STROKE_COLOR_DEFAULT = Color.BLUE;
    public static final Color POINT_FILL_COLOR_DEFAULT = Color.RED;
    public static final Color POINT_STROKE_COLOR_SELECTED = Color.GREEN;
    public static final Color POINT_FILL_COLOR_SELECTED = Color.GREEN;
    public static final double POINT_STROKE_WIDTH_DEFAULT = 1.0;
    public static final double POINT_STROKE_WIDTH_SELECTED = 1.0;
    public static final double POINT_RADIUS = 15.0;


    // TODO: Use these in Video Annotation too
    public static final Image VIDEO_PLAYER_PLAY_ICON = new Image(UIProperties.class.getResourceAsStream("/icons/play_arrow_black_36dp.png"));
    public static final Image VIDEO_PLAYER_PAUSE_ICON = new Image(UIProperties.class.getResourceAsStream("/icons/pause_black_36dp.png"));
}
