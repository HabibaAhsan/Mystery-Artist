package com.example.mysteryartist;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class DrawingCanvas extends View {

    private Path currentPath;
    private Paint currentPaint;
    private List<DrawablePath> paths;
    private DatabaseReference firebaseRef;
    private boolean isDrawer = true; // To check if the player is the drawer

    public DrawingCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paths = new ArrayList<>();
        currentPath = new Path();
        currentPaint = createNewPaint(0xFF000000); // Default black color
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.drawColor(0xFFFFFFFF);
        for (DrawablePath drawablePath : paths) {
            canvas.drawPath(drawablePath.path, drawablePath.paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawer) {
            return false; // Disable drawing for guessers
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(x, y);
                paths.add(new DrawablePath(currentPath, new Paint(currentPaint)));
                syncDrawing(x, y, "start");
                break;
            case MotionEvent.ACTION_MOVE:
                currentPath.lineTo(x, y);
                syncDrawing(x, y, "move");
                break;
            case MotionEvent.ACTION_UP:
                syncDrawing(x, y, "end");
                break;
        }

        invalidate(); // Refresh the view
        return true;
    }

    private void syncDrawing(float x, float y, String action) {
        if (firebaseRef != null) {
            Point point = new Point(x, y, action);
            point.color = currentPaint.getColor();
            point.strokeWidth = currentPaint.getStrokeWidth();
            firebaseRef.push().setValue(point);
        }
    }

    public void setFirebaseReference(String gameCode) {
        firebaseRef = FirebaseDatabase.getInstance().getReference("games/" + gameCode + "/drawings");
    }

    public DatabaseReference getFirebaseReference(String gameCode) {
        return  firebaseRef;
    }

    public void setIsDrawer(boolean isDrawer) {
        this.isDrawer = isDrawer; // Set whether the current player is the drawer
    }

    public void updateCanvas(Point point) {
        currentPaint = createNewPaint(point.color);
        currentPaint.setStrokeWidth(point.strokeWidth);

        if (point.action.equals("start")) {
            currentPath = new Path();
            currentPath.moveTo(point.x, point.y);
            paths.add(new DrawablePath(currentPath, new Paint(currentPaint)));
        } else if (point.action.equals("move")) {
            currentPath.lineTo(point.x, point.y);
        }
        invalidate();
    }

    public void clear() {
        paths.clear();
        currentPath.reset(); // Clear the current drawing path
        invalidate(); // Refresh the canvas
        if (firebaseRef != null) {
            firebaseRef.removeValue(); // Clear drawings from Firebase for all players
        }
    }

    public void setPenColor(int color) {
        currentPaint = createNewPaint(color); // Create a new Paint instance for the new color
    }

    public void setEraserMode(boolean isEraser) {
        if (isEraser) {
            currentPaint = createNewPaint(0xFFFFFFFF); // Set color to white for eraser
            currentPaint.setStrokeWidth(25); // Optional: Make eraser thicker
        } else {
            currentPaint = createNewPaint(0xFF000000); // Default back to black
            currentPaint.setStrokeWidth(8); // Default stroke width
        }
    }

    private Paint createNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        paint.setAntiAlias(true);
        return paint;
    }

    // Helper class for storing drawable paths
    private static class DrawablePath {
        Path path;
        Paint paint;

        DrawablePath(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }

    // Helper class for storing points
    static class Point {
        public float x, y;
        public String action;
        public int color;
        public float strokeWidth;

        public Point() {} // Default constructor for Firebase

        public Point(float x, float y, String action) {
            this.x = x;
            this.y = y;
            this.action = action;
        }
    }
}
