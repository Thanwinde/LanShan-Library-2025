package com.LanShan.Library.service;

import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.webjars.NotFoundException;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class GlobalExceptionHandler {

//全局异常处理器
//友情鸣谢GPT帮忙翻译列出异常
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ex.printStackTrace();
        String errorMessage = String.format(
                "参数错误！参数 '%s' 的值 '%s' 无法转换为类型 '%s'。",
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知类型"
        );

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(NoResourceFoundException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<String> handleBadSqlGrammarException(BadSqlGrammarException ex) {
        // 获取异常中的详细信息
        String errorMessage = String.format(
                "SQL语法错误：%s。错误SQL：%s。",
                ex.getMessage(),
                ex.getSql()
        );

        // 日志记录（推荐记录完整异常堆栈）
        ex.printStackTrace();

        // 返回用户友好的错误信息
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("数据完整性约束冲突，请检查数据！", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("请求参数缺失: " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<String> handleTimeoutException(TimeoutException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("请求超时，请稍后重试。", HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<String> handleTimeoutException(InsufficientAuthenticationException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNoHandlerFound(NoHandlerFoundException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("请求路径不存在，请检查URL是否正确！", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> handleBindException(BindException ex) {
        StringBuilder errorMessage = new StringBuilder("绑定失败: ");
        ex.printStackTrace();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
        }

        return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("无效的请求参数：" + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleIllegalArgumentException(BadRequestException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
        }
        ex.printStackTrace();
        return new ResponseEntity<>(errorMessage.toString(), HttpStatus.BAD_REQUEST);
    }

    // 处理空指针异常
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("发生空指针错误，请检查参数！", HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("找不到所需的页面！", HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleNotFoundException(MaxUploadSizeExceededException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("上传的图像不能大于1MB！", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleNotFoundException(MultipartException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("上传的图像不能为空！", HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ex.printStackTrace();
        return new ResponseEntity<>("请求方法不支持!" + ex.getMethod(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    // 你可以处理多个异常，并根据不同的类型给出不同的响应
    // 处理特定的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        // 记录日志
        ex.printStackTrace();
        // 返回自定义的错误信息
        return new ResponseEntity<>("发生了一个未知错误，请稍后重试。", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
