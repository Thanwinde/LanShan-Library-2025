package com.LanShan.Library.mapper;

import com.LanShan.Library.pojo.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

//各种sql

@Mapper
public interface UserMapper {

    //账号操作区

    @Insert("INSERT INTO users (id, username, password, authority,enabled,introduction,max_borrow,email) VALUES (#{id}, #{username}, #{password}, #{authority},#{enabled},'这个人很懒，什么都没写',#{max_borrow},#{email})")
    public Integer addUser(String id, String username, String password, String authority, boolean enabled,Integer max_borrow,String email);

    @Select("select * from users where id=#{id}")
    public User getUserById(String id);

    @Select("select * from users where username=#{username}")
    public User getUserByUsername(String username);

    @Select("select * from users where email=#{email}")
    public User getUserByEmail(String email);

    @Select("select * from users")
    public List<User> getAllUsers();

    @Delete("delete from users where id = #{id}")
    public Integer deleteUser(String id);

    @Update({
            "<script>",
            "UPDATE users",
            "<set>",
            "<if test=\"username != null\"> username = #{username},</if>",
            "<if test=\"password != null\"> password = #{password},</if>",
            "<if test=\"max_borrow != null\"> max_borrow = #{max_borrow},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    public Integer updateUser(String username, String password, String id,Integer max_borrow);

    @Update("update users set email = #{email} where id = #{id}")
    public Integer updateEmail(String id, String email);

    //社交功能区

    @Insert("INSERT INTO library.follows (follower_id,followed_id) VALUES (#{myid},#{toid})")
    public Integer follow(String myid,String toid);

    @Select("select follower_id from follows where followed_id = #{id}")
    public List<String> follows(String id);

    @Select("select followed_id from follows where follower_id = #{id}")
    public List<String> following(String id);

    @Delete("DELETE from follows where follower_id = #{follower_id} and followed_id = #{followed_id}")
    public Integer deleteFollow(String follower_id,String followed_id);

    @Insert("insert into library.daily (user_id,id,content,uptime) values (#{user_id},#{daily_id},#{content},#{time})")
    public Integer updaily(String user_id, String daily_id, String content, Date time);

    @Select("select * from daily where user_id = #{id}")
    public List<Daily> getdaily(String id);

    @Select("select * from daily where id = #{id}")
    public Daily getOneDailyById(String id);

    @Select("select * from comments where aim_id=#{id}")
    public List<Comment> getComments(String id);

    @Select("select * from child_comments where aim_id=#{id}")
    public List<Comment> getChild_Comments(String id);

    @Delete("delete from comments where id = #{id}")
    public void delComments(String id);

    @Delete("delete from child_comments where id = #{id}")
    public void delChild_Comments(String id);


    @Select("select * from comments where id=#{id}")
    public Comment getOneCommentsById(String id);

    @Select("select * from child_comments where id=#{id}")
    public Comment getOneChild_CommentsById(String id);


    @Select("select * from child_comments_book where aim_id=#{id}")
    public List<Comment> getChild_Comments_Book(String id);

    @Select("select * from child_comments_book where id=#{id}")
    public Comment getChild_Comments_BookById(String id);

    @Delete("delete from daily where id = #{id}")
    public void delDailys(String id);

    @Insert("INSERT into comments (aim_id,user_id,content,created_at,id) values (#{aim_id},#{user_id},#{content},#{time},#{id})")
    public void addComment(String aim_id, String user_id, String content, Date time, String id);

    @Insert("INSERT into child_comments (aim_id,user_id,content,created_at,id) values (#{aim_id},#{user_id},#{content},#{time},#{id})")
    public void addChild_Comment(String aim_id, String user_id, String content, Date time, String id);

    @Update("update users set introduction = #{introduction} where id = #{user_id}")
    public Integer updateIntroduction(String user_id,String introduction);

    //图书区

    @Insert("INSERT into library.book (id,name,author,ISBN,publishedDate,label,publisher,num) values (#{id},#{name},#{author},#{ISBN},#{publishedDate},#{label},#{publisher},#{num})")
    public void addBook(String id, String name, String author, String ISBN, Date publishedDate, String label, String publisher,Integer num);

    @Delete("delete from book where id = #{id}")
    public Integer deleteBook(String id);

    @Select("select * from book order by ${standard} ${rule} limit 5 offset #{offset}")
    public List<Book> getBooklist(Integer offset, String standard, String rule);

    @Select("select * from book where id = #{id}")
    public Book getBookById(String id);

    @Update("<script>" +
            "UPDATE book " +
            "set " +
            "id = id, " +
            "<if test=\"name != null\"> name = #{name},</if>" +
            "<if test=\"author != null\">author = #{author},</if>" +
            "<if test=\"ISBN != null\">ISBN = #{ISBN},</if>" +
            "<if test=\"publishedDate != null\">publishedDate = #{publishedDate},</if>" +
            "<if test=\"label != null\">label = #{label},</if>" +
            "<if test=\"publisher != null\">publisher = #{publisher},</if>" +
            "<if test=\"num != null\">num = #{num},</if>" +
            "id = id " +
            "WHERE id = #{id}" +
            "</script>")
    public Integer update(Map<String, Object> params);

    @Select("<script>" +
            "SELECT b.* " +
            "FROM book b " +
            "<if test='tag != null'>JOIN booktag bt ON b.id = bt.book_id </if> " +
            "WHERE 1=1 " +
            "<if test='id != null'>AND id = #{id} </if> " +
            "<if test='name != null'>AND name LIKE CONCAT('%', #{name}, '%') </if> " +
            "<if test='author != null'>AND author LIKE CONCAT('%', #{author}, '%') </if> " +
            "<if test='ISBN != null'>AND ISBN = #{ISBN} </if> " +
            "<if test='starDate != null'>AND publishedDate &gt;= #{starDate} </if> " +
            "<if test='endDate != null'>AND publishedDate &lt;= #{endDate} </if> " +
            "<if test='starScore != null'>AND score &gt;= #{starScore} </if> " +
            "<if test='endScore != null'>AND score &lt;= #{endScore} </if> " +
            "<if test='label != null'>AND label LIKE CONCAT('%', #{label}, '%') </if> " +
            "<if test='publisher != null'>AND publisher LIKE CONCAT('%', #{publisher}, '%') </if> " +
            "<if test='tag != null'>AND bt.content LIKE CONCAT('%', #{tag}, '%') </if>" +
            "</script>")
    List<Book> advGetBooks(Map<String, Object> params);

    @Select("select label,count(*) as count from book group by label order by count")
    public List<ImmutablePair<Object,Long>> bookCountByLabel();

    @Select("select YEAR(publishedDate) as year,count(*) as count from book group by year order by year")
    public List<ImmutablePair<Object,Long>> bookCountByYear();

    @Select("select book_id,count(*) as cnt from borrowing group by book_id order by cnt desc LIMIT 3")
    public List<ImmutablePair<Object,Long>> getMostBorrowBook();

    @Select("select user_id,count(*) as cnt from borrowing group by user_id order by cnt desc LIMIT 3")
    public List<ImmutablePair<Object,Long>> getMostUserBorrowBook();

    @Select("select aim_id,count(*) as cnt from comments_book group by aim_id order by cnt desc limit 3")
    public List<ImmutablePair<Object,Long>> getMostCommentBook();

    @Select("select id,score from book order by score desc limit 3")
    public List<ImmutablePair<Object, Float>> getHighScoreBook();

    @Select("select id,YEAR(publishedDate) as year from book order by year desc limit 3")
    public List<ImmutablePair<Object,Long>> getNewestBook();

    @Insert("insert into library.bookfavor (user_id,book_id) values (#{user_id},#{book_id})")
    public Integer addFavor(String user_id, String book_id);

    @Delete("delete from bookfavor where user_id = #{user_id} and book_id = #{book_id}")
    public Integer clFavor(String user_id,String book_id);

    @Select("SELECT bookfavor.book_id, book.name FROM bookfavor JOIN book ON bookfavor.book_id = book.id WHERE bookfavor.user_id = #{user_id}")
    public List<ImmutablePair<Object,Long>> getFavor(String user_id);

    @Select("select book.label,count(*) as count from bookfavor join book on book.id = bookfavor.book_id where bookfavor.user_id = #{user_id} GROUP BY book.label ORDER BY count ASC")
    public List<ImmutablePair<String,Long>> reco_label(String user_id);

    @Select("SELECT book.id, book.name " +
            "FROM book " +
            "LEFT JOIN bookfavor ON book.id = bookfavor.book_id AND bookfavor.user_id = #{user_id} " +
            "WHERE book.label = #{label}" +
            "AND bookfavor.book_id IS NULL;")
    public List<ImmutablePair<String,Long>> reco_book(String label,String user_id);

    @Select("select id, name from book")
    public List<ImmutablePair<String,Long>> reco_random();

    @Insert("insert into booktag (book_id,content) values (#{book_id},#{content})")
    public Integer addtag(String book_id,String content);

    @Select("select content from booktag where book_id = #{book_id}")
    public List<String> gettags(String book_id);

    @Insert("INSERT into comments_book (aim_id,user_id,content,created_at,id,score) values (#{aim_id},#{user_id},#{content},#{time},#{id},#{score})")
    public void addComment_book(String aim_id, String user_id, String content, Date time, String id,Integer score);


    @Insert("INSERT into child_comments_book (aim_id,user_id,content,created_at,id) values (#{aim_id},#{user_id},#{content},#{time},#{id})")
    public void addChild_Comment_book(String aim_id, String user_id, String content, Date time, String id);


    @Update("update book set score = #{score} where id = #{book_id}")
    public Integer updateScore(String book_id, Float score);

    @Select("SELECT AVG(score) FROM comments_book")
    public Float getAvgScore();

    @Select("select * from comments_book where id = #{id}")
    public Comment_book getComment_book(String id);

    @Select("select * from comments_book where aim_id = #{aim_id}")
    public List<Comment_book> getComment_books(String aim_id);

    @Delete("delete from comments_book where id = #{id}")
    public Integer delComment_book(String id);

    @Delete("delete from child_comments_book where id = #{id}")
    public Integer delChild_Comment_book(String id);

    @Insert("insert into borrowing (book_id,user_id,endtime) VALUES (#{book_id},#{user_id},#{endtime})")
    public Integer borrow_book(String book_id, String user_id,LocalDateTime endtime);

    @Select("select book_id,endtime from borrowing where book_id = #{book_id} and user_id = #{user_id}")
    public ImmutablePair<String,Object> getBorrow_book(String book_id, String user_id);

    @Delete("delete from borrowing where book_id = #{book_id} and user_id = #{user_id}")
    public Integer returnBook(String book_id,String user_id);

    @Select("select book_id,endtime from borrowing where user_id = #{user_id}")
    public List<ImmutablePair<String,Object>> getBorrow_books(String user_id);

    @Select("select * from users where email IS NOT NULL")
    public List<User> adv_getUsers();
}