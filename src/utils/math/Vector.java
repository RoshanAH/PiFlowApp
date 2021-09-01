package utils.math;

public class Vector implements Cloneable{

    public double x;
    public double y;

    public Vector(double x, double y){
        this.x = x;
        this.y = y;
    }

    @Override
    public Vector clone() {
        return new Vector(x, y);
    }

    public double getMagnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double getTheta() {
        return Math.atan2(y, x);
    }

    public Vector setMagnitude(double r) {
        Vector clone = clone();

        final double theta = getTheta();
        clone.x = r * Math.cos(theta);
        clone.y = r * Math.sin(theta);

        return clone;
    }

    public Vector setTheta(double theta) {
        Vector clone = clone();


        final double r = getMagnitude();
        clone.x = r * Math.cos(theta);
        clone.y = r * Math.sin(theta);

        return clone;
    }

    public Vector rotate(double theta) {
        Vector clone = clone();

        clone.x = Math.cos(theta) * x - Math.sin(theta) * y;
        clone.y = Math.sin(theta) * x + Math.cos(theta) * y;

        return clone;
    }

    public Vector add(Vector other){
        Vector clone = clone();
        clone.x += other.x;
        clone.y += other.y;

        return clone;
    }

    public Vector subtract(Vector other){
        Vector clone = clone();
        clone.x -= other.x;
        clone.y -= other.y;

        return clone;
    }

    public Vector translate(double x, double y){
        return add(new Vector(x, y));
    }

    public Vector scale(double scale){
        Vector clone = clone();

        clone.x *= scale;
        clone.y *= scale;

        return clone;
    }

    public Vector projectOnto(Vector other){
        return scale(dot(other) / square());
    }

    public Vector normalize(){
        return scale(1 / getMagnitude());
    }

    public double dist(Vector other){
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    public double dot(Vector other){
        return x * other.x + y * other.y;
    }

    public double square(){
        return dot(this);
    }

    public String toString(){
        return "(" + x + ", "+ y + ")";
    }

    public static Vector Polar(double r, double theta){
        return new Vector(Math.cos(theta) * r, Math.sin(theta) * r);
    }
}
