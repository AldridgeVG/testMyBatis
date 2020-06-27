package mtest;

import mapper.SquareMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

//instead of javax.annotation
import org.apache.ibatis.io.Resources;
import pojo.Square;

import java.io.IOException;
import java.io.InputStream;

//DAO e.g.
public class helloMB {
    public static void main(String[] args) {

        //generate SqlSessionFactory
        String resource = "config/mybatis-config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = null;
        sqlSessionFactory=new SqlSessionFactoryBuilder().build(inputStream);

        //use SqlSession
        SqlSession sqlSession = null;
        try{
            sqlSession = sqlSessionFactory.openSession();

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
