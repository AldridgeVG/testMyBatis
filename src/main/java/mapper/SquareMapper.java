package mapper;
import pojo.Square;

public interface SquareMapper {

    // this method won't be realized, mapper only get the parameter of this method as param of SQL
    public Square getSquare(int length);
}
