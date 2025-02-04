package com.LanShan.Library.controllers;

import com.LanShan.Library.pojo.*;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.LanShan.Library.mapper.UserMapper;
import com.LanShan.Library.utils.SnowFlakeGenerator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/book")
//图书管理区
public class BookController {

    @Autowired
    private UserMapper userMapper;

    //添加图书，有字段检测，日期按照xxxx/xx/xx输入
    @PostMapping("/add")
    public JSONObject addBook(
     String name, String author, String ISBN,Date publishedDate,String label,String publisher,Integer num) throws BadRequestException {
        JSONObject json = new JSONObject();
        if( ISBN == null || author == null || name == null || publishedDate == null || publisher == null || label == null || num == null
                ||ISBN.isEmpty() || author.isEmpty() || name.isEmpty()  || label.isEmpty()) {
            throw new BadRequestException("书籍信息不不可为空，请补齐!(name,author,ISBN,publishedDate,label,publisher,num)");
        }
        if (!userMapper.advGetBooks(Map.of("ISBN", ISBN)).isEmpty()) {
            throw new BadRequestException("已经存在同一ISBN的书籍!");
        }

        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        userMapper.addBook(id, name, author, ISBN, publishedDate, label, publisher,num);
        json.put("id",id);
        json.put("name",name);
        json.put("author",author);
        json.put("ISBN",ISBN);
        json.put("publishedDate",publishedDate);
        json.put("label",label);
        json.put("publisher",publisher);
        json.put("num",num);
        return json;
    }

    //删除图书
    @DeleteMapping("/delbook")
    public JSONObject delBook(@RequestParam List<String> id) throws BadRequestException {
        JSONObject json = new JSONObject();
        int cnt = 1;
        for(String book_id:id){
            JSONObject result = new JSONObject();
            Book book = userMapper.getBookById(book_id);
            if(book == null){
                throw new NoResourceFoundException("ID: " + book_id +" 书籍不存在!");
            }
            if(userMapper.deleteBook(book_id) != 1)
                throw new BadRequestException("ID: " + book_id +" 删除失败!");
            result.put("id",book_id);
            result.put("name",book.getName());
            json.put("deletedBook "+cnt,result);
            cnt++;
        }
        return json;
    }

    //更新图书，图书id必须给出，其余给什么更新什么
    @PutMapping("/update")
    public JSONObject updateBook(
            @RequestParam String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String ISBN,
            @RequestParam(required = false) Date publishedDate,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) Integer num
    ) throws BadRequestException {
        JSONObject json = new JSONObject();
        if (id == null || id.trim().isEmpty()) {
            throw new BadRequestException("书籍ID不能为空,请补全!");
        }
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("id", id.trim());
        paramMap.put("name", (name != null && !name.trim().isEmpty()) ? name.trim() : null);
        paramMap.put("author", (author != null && !author.trim().isEmpty()) ? author.trim() : null);
        paramMap.put("ISBN", (ISBN != null && !ISBN.trim().isEmpty()) ? ISBN.trim() : null);
        paramMap.put("publishedDate", publishedDate);
        paramMap.put("label", (label != null && !label.trim().isEmpty()) ? label.trim() : null);
        paramMap.put("publisher", (publisher != null && !publisher.trim().isEmpty()) ? publisher.trim() : null);
        paramMap.put("num", num);
        int updateCount = userMapper.update(paramMap);
        if (updateCount > 0) {
            json.put("message", "更新成功");
        } else {
            throw new NoResourceFoundException("书籍不存在或未做任何更新!");
        }
        return json;
    }


    //获取图书列表，不会给出详细信息,自定义排序规则，页数，按什么排序，一页5本
    @GetMapping("/list")
    public JSONObject listBook(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "name") String standard,@RequestParam(defaultValue = "ASC") String rule) throws BadRequestException {
        if(standard == null || standard.isEmpty() || rule == null || rule.isEmpty())
            throw new BadRequestException("排序标准或排序规则(asc,desc)不可为空，请补齐!");
        JSONObject json = new JSONObject();
        Integer offset = (page - 1) * 5;
        List<Book> books = userMapper.getBooklist(offset,standard,rule);
        int cnt = (page-1) * 5 + 1;
        for(Book book:books){
            JSONObject result = new JSONObject();
            result.put("detail",book);
            result.put("tags",userMapper.gettags(book.getId()));
            json.put("book "+cnt ,result);
            cnt++;
        }
        json.put("排序标准:",standard);
        json.put("排序规则:",rule);
        json.put("当前页数:",page);
        return json;
    }

    //获取图书的详细信息，会给出tag和评论
    @GetMapping("/getbook")
    public JSONObject getBook(@RequestParam String id) throws BadRequestException {
        if(id == null || id.isEmpty())
            throw new BadRequestException("书籍id不可为空，请补齐!");
        JSONObject json = new JSONObject();
        Book book = userMapper.getBookById(id);
        if(book == null)
            throw new NoResourceFoundException("书籍不存在!");
        json.put("book", book);
        json.put("tags", userMapper.gettags(book.getId()));
        List<Comment_book> comments = userMapper.getComment_books(id);
        JSONArray commentsArray = new JSONArray();
        for (int i = 0; i < comments.size(); i++) {
            Comment_book comment_book = comments.get(i);
            JSONObject commentJson = new JSONObject();
            commentJson.put("comment_" + i, comment_book);  // 显示评论序号
            // 获取子评论
            List<Comment> childComments = userMapper.getChild_Comments_Book(comment_book.getId());
            if (childComments != null && !childComments.isEmpty()) {
                JSONArray childCommentsArray = new JSONArray();
                for (int j = 0; j < childComments.size(); j++) {
                    Comment childComment = childComments.get(j);
                    JSONObject childCommentJson = new JSONObject();
                    childCommentJson.put("child_comment_" + j, childComment);  // 显示子评论序号
                    childCommentsArray.add(childCommentJson);
                }
                commentJson.put("child_comments of "+i, childCommentsArray);
            }
            commentsArray.add(commentJson);
        }
        json.put("comments", commentsArray);
        return json;
    }



    //高级搜索，给什么就按照什么搜
    @PostMapping("/advget")
    public JSONObject advgetBook(
            String id, String name, String author, String ISBN,
            Date starDate, Date endDate, String label, String publisher, String tag,
            Float starScore, Float endScore) {
        Map<String, Object> params = new HashMap<>();
        if (id != null && !id.isEmpty())
            params.put("id", id);
        if (name != null && !name.isEmpty())
            params.put("name", name);
        if (author != null && !author.isEmpty())
            params.put("author", author);
        if (ISBN != null && !ISBN.isEmpty())
            params.put("ISBN", ISBN);
        if (starDate != null)
            params.put("starDate", starDate);
        if (endDate != null)
            params.put("endDate", endDate);
        if (label != null && !label.isEmpty())
            params.put("label", label);
        if (publisher != null && !publisher.isEmpty())
            params.put("publisher", publisher);
        if (tag != null && !tag.isEmpty())
            params.put("tag", tag);
        if (starScore != null)
            params.put("starScore", starScore);
        if (endScore != null)
            params.put("endScore", endScore);

        List<Book> books = userMapper.advGetBooks(params);
        JSONObject json = new JSONObject();
        int cnt = 1;
        for(Book book:books){
            JSONObject result = new JSONObject();
            result.put("book",book);
            result.put("tags",userMapper.gettags(book.getId()));
            cnt++;
            json.put("book "+cnt ,result);
        }
        return json;
    }


    //图书统计功能
    @GetMapping("/statistics")
    public JSONObject statistics(){
        List<ImmutablePair<Object, Long>> te;
        JSONObject json = new JSONObject();

        Map<Object, Long> labelResult = new HashMap<>();
        te = userMapper.bookCountByLabel();
        Long sum = 0L;
        for(Pair<Object, Long> pair : te){
            labelResult.put(pair.getLeft(), pair.getRight());
            sum += pair.getRight();
        }

        json.put("标签总数", sum);
        json.put("书类数", (long) te.size());
        json.put("按种类分类", new HashMap<>(labelResult));

        Map<Object, Long> yearResult = new HashMap<>();
        te = userMapper.bookCountByYear();
        for(Pair<Object, Long> pair : te){
            yearResult.put(pair.getLeft(), pair.getRight());
        }

        json.put("年份类数", (long) te.size());
        json.put("按年份分类", new HashMap<>(yearResult));

        return json;
    }

    //添加图书到收藏夹
    @PostMapping("/addfavor")
    public JSONObject add_favor(String book_id) throws BadRequestException {
        if(book_id == null || book_id.isEmpty())
            throw new BadRequestException("书籍id不可为空，请补齐!");
        Book book = userMapper.getBookById(book_id);
        if(book == null)
            throw new NoResourceFoundException("书籍不存在！检查ID！");
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        if(userMapper.clFavor(user_id,book_id) >= 1){
            json.put("message","成功取消收藏");
            return json;
        }
        if(userMapper.addFavor(user_id,book_id) == 1)
            json.put("message","成功添加到收藏");
        else
            throw new BadRequestException("添加收藏失败!");
        return json;
    }

    //简单的推荐功能，按照收藏夹里面最多的类型（label）进行随机推荐，没有的话就随机一个
    @RequestMapping("/recommendations")
    public JSONObject recommendations(){
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        List<ImmutablePair<String,Long>> te = userMapper.reco_label(user_id);
        if(te == null || te.isEmpty()){
            json.put("message","你还没收藏任何书呢");
            te = userMapper.reco_random();
            if(te == null)
                throw new NoResourceFoundException("暂时无书籍推荐!");
            Random random = new Random();
            int randomIndex = random.nextInt(te.size());  // 随机生成一个索引
            json.put("向你推荐",te.get(randomIndex));
            return json;
        }
        String label = te.getFirst().getLeft();
        te = userMapper.reco_book(label,user_id);
        Random random = new Random();
        int randomIndex = random.nextInt(te.size());  // 随机生成一个索引
        json.put("你最感兴趣的类型是:",label);
        json.put("向你推荐:",te.get(randomIndex));
        return json;
    }

    //添加标签
    @PostMapping("addtag")
    public JSONObject addtag(String book_id,String tag) throws BadRequestException {
        if(book_id == null || book_id.isEmpty() || tag == null || tag.isEmpty())
            throw new BadRequestException("书籍id或标签不可为空，请补齐!");
        Book book = userMapper.getBookById(book_id);
        if(book == null)
            throw new NoResourceFoundException("书籍不存在!");
        JSONObject json = new JSONObject();
        userMapper.addtag(book_id,tag);
        json.put("message","增加标签" + tag);
        return json;
    }

    //添加图书的评论，可以给出分数也可以不给
    @PostMapping("/addcomment")
    public JSONObject addcomment(String aim_id, String content, Integer score) throws BadRequestException {
        if(aim_id == null || aim_id.isEmpty() || content == null || content.isEmpty())
            throw new BadRequestException("评论内容或目标id不可为空，请补齐!");
        Book book = userMapper.getBookById(aim_id);
        Comment_book comment = userMapper.getComment_book(aim_id);
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        if(comment == null && book == null){
            throw new NoResourceFoundException("评论目标不存在!");
        }
        if (book != null) {

        userMapper.addComment_book(aim_id,user_id,content,new Date(),id,score);
        if(book.score == null)
            book.score = Float.valueOf(score);
        else
            book.score = userMapper.getAvgScore();

        if(userMapper.updateScore(aim_id,book.score) != 1)
            throw new BadRequestException("更新分数失败！");

        json.put("message","发布书籍评论成功，评论id为 " + id);
        }
        else{
            userMapper.addChild_Comment_book(aim_id,user_id,content,new Date(),id);
            json.put("message","发布子评论成功，评论id为 " + id);
        }
        return json;
    }

    @DeleteMapping("/delcomment")
    public JSONObject delcomment(@RequestParam("id") List<String> ids) throws BadRequestException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        JSONObject json = new JSONObject();
        List<String> result = new ArrayList<>();
        for(String id : ids) {
            if (id == null || id.isEmpty()){
                result.add(id  + " 无法找到，删除失败!");
                continue;
            }

            Comment_book comment = userMapper.getComment_book(id);
            Comment child = userMapper.getChild_Comments_BookById(id);
            if (comment == null && child == null) {
                result.add(id + " 无法找到，删除失败!");
                continue;
            }
            if(comment != null) {
                if (!comment.getUser_id().equals(user_id)) {
                    if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("admin"))) {
                        throw new InsufficientAuthenticationException("无权限删除此内容!");
                    }
                }
                if (userMapper.delComment_book(id) != 1)
                    throw new BadRequestException("删除失败!");

                Float score = userMapper.getAvgScore();
                if (userMapper.updateScore(comment.aim_id, score) != 1)
                    throw new BadRequestException("更新分数失败！");
                result.add(id + "书籍评论删除成功!");
            }else{
                userMapper.delChild_Comment_book(id);
                result.add(id + "子评论删除成功!");
            }
        }
        json.put("result",result);
        return json;
    }

    //借阅图书
    @PostMapping("/borrowing")
    public JSONObject borrowing(@RequestParam String id) throws BadRequestException {
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        User user = userMapper.getUserById(user_id);
        if(id == null || id.isEmpty())
            throw new BadRequestException("书籍id不可为空，请补齐!");
        Book book = userMapper.getBookById(id);
        if(book == null)
            throw new NoResourceFoundException("书籍不存在!");

        ImmutablePair<String,Object> reco = userMapper.getBorrow_book(id,user_id);
        if(reco != null)
            throw new BadRequestException("你已借过这本书!");

        if(book.num == 0)
            throw new NoResourceFoundException("书籍已全部借出!");

        List<ImmutablePair<String,Object>> borrowed = userMapper.getBorrow_books(user_id);
        if(borrowed.size() >= user.getMax_borrow())
            throw new BadRequestException("你已到最大借阅数!");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endtime = now.plusMonths(4);
        if(userMapper.borrow_book(book.id,user_id,endtime) != 1)
            throw new BadRequestException("借阅失败！");
        userMapper.update(Map.of("id",id
                                ,"num",book.num-1));
        json.put("message","成功借阅图书！");
        return json;
    }

    //归还图书
    @PostMapping("/return")
    public JSONObject returnBook(String id) throws BadRequestException {
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        if(id == null || id.isEmpty())
            throw new BadRequestException("书籍id不可为空，请补齐!");
        Book book = userMapper.getBookById(id);
        if(book == null)
            throw new NoResourceFoundException("书籍不存在!");
        ImmutablePair<String,Object> reco = userMapper.getBorrow_book(id,user_id);
        if(reco == null)
            throw new BadRequestException("你并没有借过这本书!");
        if(userMapper.returnBook(book.id, user_id) != 1)
            throw new BadRequestException("归还失败!");
        userMapper.update(Map.of("id",id
                                ,"num",book.num+1));
        json.put("message","归还成功!");
        return json;
    }

    //看本账户借阅情况
    @GetMapping("/checkBorrow")
    public JSONObject checkBorrow(){
        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        List<ImmutablePair<String,Object>> books = userMapper.getBorrow_books(user_id);
        if(books == null || books.isEmpty())
            json.put("message","你并没有借过任何书!");
        else
            json.put("books",books);
        return json;
    }


}
