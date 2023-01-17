package com.moon.exchange.counter.exception;

import com.moon.exchange.counter.common.UnifyResponse;
import com.moon.exchange.counter.exception.bussness.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@ControllerAdvice
public class GlobalExceptionAdvice {

    @Autowired
    private ExceptionCodeConfiguration configuration;

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public UnifyResponse<Object> handleException(Exception e, HttpServletRequest request) {
        String requestMessage = this.getRequestMessage(request);
        String message = this.configuration.getMessage(9999);
        e.printStackTrace();
        return UnifyResponse.fail(9999, message, requestMessage);
    }

    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<UnifyResponse<Object>> handleHttpException(HttpServletRequest request, BusinessException e) {
        // ResponseEntity 要接收三个参数:UnifyResponse, HttpHeaders, HttpStatus.

        String requestMessage = this.getRequestMessage(request);
        UnifyResponse<Object> unifyResponse = UnifyResponse.fail(e.getCode(),
                this.configuration.getMessage(e.getCode()),
                requestMessage);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpStatus httpStatus = HttpStatus.resolve(e.getHttpStatusCode());
        assert httpStatus != null;
        return new ResponseEntity<>(unifyResponse, headers, httpStatus);
    }

//    @ExceptionHandler(value = MethodArgumentNotValidException.class)
//    @ResponseBody
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public UnifyResponse handleMethodArgumentNotValidException(HttpServletRequest request,
//                                                               MethodArgumentNotValidException e) {
//        String requestMessage = this.getRequestMessage(request);
//
//        List<ObjectError> errors = e.getBindingResult().getAllErrors();
//        String message = this.formatAllErrorMessages(errors);
//
//        return new UnifyResponse(10001, message, requestMessage);
//    }

//    @ExceptionHandler(value = HttpMessageNotReadableException.class)
//    @ResponseBody
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public UnifyResponse handleHttpMessageNotReadableException(HttpServletRequest request,
//                                                               HttpMessageNotReadableException e) {
//        String requestMessage = this.getRequestMessage(request);
//        return new UnifyResponse(10000, this.configuration.getMessage(10000), requestMessage);
//    }

//    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
//    @ResponseBody
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public UnifyResponse handleHttpMessageNotReadableException(HttpServletRequest request,
//                                                               HttpRequestMethodNotSupportedException e) {
//        String requestMessage = this.getRequestMessage(request);
//        return new UnifyResponse(10006, this.configuration.getMessage(10006), requestMessage);
//    }

    /**
     * 拼接多个body参数校验异常消息
     */
    private String formatAllErrorMessages(List<ObjectError> errors) {
        StringBuffer errorMsg = new StringBuffer();
        errors.forEach(error ->
                errorMsg.append(error.getDefaultMessage()).append(';')
        );
        return errorMsg.toString();
    }

    private String getRequestMessage(HttpServletRequest request) {
        String method = request.getMethod();
        String url = request.getRequestURI();
        return method + " " + url;
    }
}
