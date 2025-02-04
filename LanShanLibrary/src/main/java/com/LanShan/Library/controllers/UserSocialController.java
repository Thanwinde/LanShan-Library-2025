package com.LanShan.Library.controllers;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.LanShan.Library.mapper.UserMapper;
import com.LanShan.Library.pojo.Comment;
import com.LanShan.Library.pojo.Daily;
import com.LanShan.Library.pojo.MyUserDetails;
import com.LanShan.Library.pojo.User;
import com.LanShan.Library.utils.SnowFlakeGenerator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/users")
//社交功能区，实现了头像上传与下载，发布动态与评论，关注，自我介绍等功能
public class UserSocialController {
    @Autowired
    private UserMapper userMapper;

    private static final String UPLOAD_DIR = "/usr/local/headimg/"; // 头像存储路径
    //private static final String UPLOAD_DIR = "D://headimg/"; // 头像存储路径

    //上传头像，图片名默认是用户ID
    @PostMapping("/upheadimg")
    public JSONObject uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("文件不能为空!");
        }
        if(file.getSize() > 1024 * 1024)
            throw new BadRequestException("图片不能超过1M!");

        if(!file.getContentType().equals("image/jpeg"))
            throw new BadRequestException("只能上传JPG格式的图片!");

        JSONObject json = new JSONObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String myid = userDetails.getId();
        File dest = new File(UPLOAD_DIR + myid +".jpg");
        file.transferTo(dest);
        json.put("message","头像已上传！");
        return json;
    }

    //获取头像，根据用户ID获取
    @GetMapping("/headimg")
    public ResponseEntity<Resource> getAvatar(@RequestParam String user_id) throws MalformedURLException {
        User user = userMapper.getUserById(user_id);
        if (user == null) {
            throw new NoResourceFoundException("用户不存在!");
        }
        Path filePath = Paths.get(UPLOAD_DIR, user_id + ".jpg");
        Path default_img = Paths.get(UPLOAD_DIR, "default" + ".jpg");

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            resource = new UrlResource(default_img.toUri());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(resource);
    }

    //获取用户的账号详情，比如动态和简自我介绍
    @GetMapping("/profile")
    public JSONObject profile(@RequestParam String user_id) throws BadRequestException {
        if(user_id == null || user_id.isEmpty())
            throw new BadRequestException("id信息不可为空，请补齐!");
        JSONObject json = new JSONObject();
        User user = userMapper.getUserById(user_id);
        json.put("id",user.getId());
        json.put("username",user.getUsername());
        json.put("authority" ,user.getAuthority());
        json.put("introduction",user.getIntroduction());
        json.put("favorite books", userMapper.getFavor(user_id));
        json.put("maxBorrow",user.getMax_borrow());
        List<ImmutablePair<String, Object>> borrowed = userMapper.getBorrow_books(user_id);
        json.put("borrowed_cnt",borrowed.size());
        json.put("Borrowed",borrowed);
        return json;
    }

    //上传自我介绍
    @PutMapping("/updateinfo")
    public JSONObject editSelf(@RequestParam String introduction) throws BadRequestException {
        if(introduction == null || introduction.isEmpty())
            throw new BadRequestException("介绍信息不可为空，请补齐!");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String myid = userDetails.getId();
        JSONObject json = new JSONObject();
        if(userMapper.updateIntroduction(myid,introduction) != 1)
            throw new BadRequestException("更新介绍失败！");
        json.put("message", "成功更新简介为: " + introduction);
        return json;
    }

    //关注某人，传入用户ID
    @PostMapping("/follow")
    public JSONObject follow(String user_id) throws BadRequestException {
        if(user_id == null || user_id.isEmpty())
            throw new BadRequestException("id信息不可为空，请补齐!");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String myid = userDetails.getId();
        if(myid.equals(user_id)) {
            throw new NoResourceFoundException("不能关注自己！");
        }
        JSONObject json = new JSONObject();
        List<String> follows = userMapper.follows(user_id);
        for (String follow : follows) {
            if(follow.equals(myid)){
                userMapper.deleteFollow(myid,user_id);
                json.put("message","已取关！");
                return json;
            }
        }
        User user = userMapper.getUserById(user_id);
        if(user == null){
            throw new NoResourceFoundException("找不到用户，请检查ID!");
        }
        Integer i = userMapper.follow(myid, user_id);
        if(i == 0){
            throw new NoResourceFoundException("关注失败!");
        }
        json.put("message",myid + "已关注"+ user_id);
        return json;
    }

    //谁关注了{id}
    @GetMapping("/follows")
    public JSONObject follows(@RequestParam String user_id) throws BadRequestException {
        if(user_id == null || user_id.isEmpty())
            throw new BadRequestException("id信息不可为空，请补齐!");
        JSONObject json = new JSONObject();
        List<String> follows = userMapper.follows(user_id);
        json.put("follows",follows);
        return json;
    }

    //{id}关注了谁
    @GetMapping("/following")
    public JSONObject following(@RequestParam String user_id) throws BadRequestException {
        if(user_id == null || user_id.isEmpty())
            throw new BadRequestException("id信息不可为空，请补齐!");
        JSONObject json = new JSONObject();
        List<String> follows = userMapper.following(user_id);
        json.put("following",follows);
        return json;
    }

    //上传动态
    @PostMapping("/updaily")
    public JSONObject updaily(String content) throws BadRequestException {
        if(content == null || content.isEmpty())
            throw new BadRequestException("动态内容不可为空，请补齐!");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        JSONObject json = new JSONObject();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String daily_id = IDgeter.generateNextId();
        Date time = new Date();
        Integer i = userMapper.updaily(user_id,daily_id,content,time);
        if(i == 0){
            throw new NoResourceFoundException("上传失败!");
        }
        json.put("message","动态成功上传,动态id为 " + daily_id);
        return json;
    }

    @GetMapping("/getdaily")
    public JSONObject getdaily(@RequestParam String user_id) throws BadRequestException, NoResourceFoundException {
        if (user_id == null || user_id.isEmpty()) {
            throw new BadRequestException("id信息不可为空，请补齐!");
        }
        JSONObject result = new JSONObject();
        User user = userMapper.getUserById(user_id);
        if (user == null) {
            throw new NoResourceFoundException("找不到用户，请检查ID!");
        }
        List<Daily> dailys = userMapper.getdaily(user_id);
        if (dailys == null || dailys.isEmpty()) {
            throw new NoResourceFoundException("该用户没有发布过动态!");
        }
        for (int i = 0; i < dailys.size(); i++) {
            Daily dailyRecord = dailys.get(i);
            JSONObject daily = new JSONObject();
            daily.put("content", dailyRecord);
            List<Comment> comments = userMapper.getComments(dailyRecord.getId());
                JSONArray commentsArray = new JSONArray();
                for (int j = 0; j < comments.size(); j++) {
                    Comment commentObj = comments.get(j);
                    JSONObject commentJson = new JSONObject();
                    commentJson.put("comment "+j, commentObj);
                    List<Comment> child_comments = userMapper.getChild_Comments(commentObj.getId());
                        JSONArray childCommentsArray = new JSONArray();
                        for (int k = 0; k < child_comments.size(); k++) {
                            childCommentsArray.add(JSONObject.of("child_comments " + k,child_comments.get(k)));
                        }
                        commentJson.put("child_comments of " + j, childCommentsArray);
                    commentsArray.add(commentJson);
                }
                daily.put("comments", commentsArray);
            result.put("daily" + i, daily);
        }
        return result;
    }

    //删除内容（动态或评论），给出动态或评论的id，并可批量删除
    @DeleteMapping("/delcontents")
    public JSONObject delcontents(@RequestParam("aim_id") List<String> id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        JSONObject json = new JSONObject();
        List<String> result = new ArrayList<>();
        Comment comment;
        Comment child_comment;
        Daily daily;
        for(String a:id){
            String own_id = "";
            child_comment = userMapper.getOneChild_CommentsById(a);
            comment  = userMapper.getOneCommentsById(a);
            daily = userMapper.getOneDailyById(a);
            if(comment != null){
                own_id = comment.id;
            }
            if(daily != null){
                own_id = daily.id;
            }
            if(child_comment != null){
                own_id = child_comment.id;
            }
            if(own_id == null){
                result.add("未找到对应的评论或日常数据, id: " + a);
                continue;
            }
            if(!own_id.equals(user_id)){
                if(!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("admin"))){
                    result.add("无权限删除此内容!");
                    continue;
                }
            }
            userMapper.delDailys(a);
            userMapper.delComments(a);
            userMapper.delChild_Comments(a);
            result.add("已删除,id:" + a);
        }
        json.put("message",result);
        return json;
    }//id为目标的id,管理员能删除所有，其余只能删除自己发的

    //进行评论
    @PostMapping("/addcomment")
    public JSONObject add_comment(String aim_id,String content) throws BadRequestException {
        if(aim_id == null || aim_id.isEmpty() || content == null || content.isEmpty())
            throw new BadRequestException("目标id或评论内容不可为空，请补齐!");
        Daily daily = userMapper.getOneDailyById(aim_id);
        Comment comment = userMapper.getOneCommentsById(aim_id);

        if(daily == null && comment == null){
            throw new NoResourceFoundException("找不到目标！请检查ID！");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        String user_id = userDetails.getId();
        SnowFlakeGenerator IDgeter = new SnowFlakeGenerator(0, 0);
        String id = IDgeter.generateNextId();
        JSONObject json = new JSONObject();
        if(daily != null) {
            userMapper.addComment(aim_id, user_id, content, new Date(), id);
            json.put("message", "发布成功，评论id为 " + id);
        }
        else{
            userMapper.addChild_Comment(aim_id, user_id, content, new Date(), id);
            json.put("message", "发布成功，评论id为 " + id);
        }
        return json;
    }
}
