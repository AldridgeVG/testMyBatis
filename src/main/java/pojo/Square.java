package pojo;

import org.apache.ibatis.type.Alias;

@Alias("msquare")
public class Square {
    private int length;
    private int height;

    public Square(int length, int height) {
        this.length = length;
        this.height = height;
    }

    public Square() {
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
