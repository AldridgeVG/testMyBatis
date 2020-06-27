package mtest;

import mapper.SquareMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

//instead of javax.annotation
import org.apache.ibatis.io.Resources;
import pojo.Square;
import util.SqlSessionFactoryUtil;

import java.io.IOException;
import java.io.InputStream;

//DAO e.g.
public class helloMB {
    public static void main(String[] args) {
        //use SqlSession
        SqlSession sqlSession = null;
        try{
            sqlSession = SqlSessionFactoryUtil.openSqlSession();

            //use reflex to get Mapper
            SquareMapper squareMapper = sqlSession.getMapper(SquareMapper.class);
            Square square = squareMapper.getSquare(3);
            System.out.println(square.getHeight());

            sqlSession.commit();
        }catch (Exception e){
            System.err.println(e.getMessage());
            sqlSession.rollback();
        }finally {
            if(sqlSession!=null) sqlSession.close();
        }
    }
}
