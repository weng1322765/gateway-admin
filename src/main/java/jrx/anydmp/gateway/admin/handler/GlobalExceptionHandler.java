package jrx.anydmp.gateway.admin.handler;

import jrx.anytxn.common.data.TxnRespResult;
import jrx.anytxn.common.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {
	Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


	/**
	 * 对不能单独处理的异常进行统一处理
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public TxnRespResult txnExceptionHandler(Exception e) {
		Long errId = System.currentTimeMillis();
		logger.error("errId:"+errId+"服务器内部错误", e);
		//update by zwg 20181120 不输出message内容，message内容中包含sql语句内容
		return new TxnRespResult<>().getFail(0, "errId:"+errId+ "errClass:"+e.getClass().getName());
	}

	/**
	 * 对服务异常处理
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = TxnException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public TxnRespResult txnExceptionHandler(TxnException e) {
		return new TxnRespResult<>().getFail(e.getErrCode(), e.getErrMsg());
	}

	/**
	 * 对拒绝异常处理
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = TxnForbiddenException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public TxnRespResult txnForbiddenExceptionHandler(TxnForbiddenException e) {
		return new TxnRespResult<>().getFail(e.getErrCode(), e.getErrMsg());
	}


	/**
	 * 对未授权异常处理
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = TxnUnauthorizedException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public TxnRespResult txnUnauthorizedExceptionHandler(TxnUnauthorizedException e) {
		return new TxnRespResult<>().getFail(e.getErrCode(), e.getErrMsg());
	}

	/**
	 * 对未发现异常处理
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = TxnNotFoundException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public TxnRespResult txnNotFoundExceptionHandler(TxnNotFoundException e) {
		return new TxnRespResult<>().getFail(e.getErrCode(), e.getErrMsg());
	}

	/**
	 * 对错误的请求处理 包括参数检查错误
	 * @param e
	 * @return
	 */
	@ExceptionHandler(value = {TxnArgumentException.class,MethodArgumentNotValidException.class,HttpMessageNotReadableException.class})
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public TxnRespResult txnArgumentExceptionHandler(Exception e) {
		if(e instanceof TxnArgumentException){
			TxnArgumentException ex = (TxnArgumentException) e;
			return new TxnRespResult<>().getFail(ex.getErrCode(), ex.getErrMsg());
		}else if (e instanceof MethodArgumentNotValidException){
			return methodArgumentNotValidExceptionHandler((MethodArgumentNotValidException) e);
		}else{
			logger.error("参数错误，请检查",e);
			return new TxnRespResult<>().getFail(0, "参数错误，请检查");
		}
	}

	private TxnRespResult methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex){
		TxnRespResult result = new TxnRespResult();
		// 参数校验异常
		List<ObjectError> errors = ex.getBindingResult().getAllErrors();
		if(!errors.isEmpty()) {
			// 获取所有出错字段及错误信息，FieldName -> ErrorMessage (字段名称 -> 对应的错误信息)
			ArrayList<Map<String, String>> errorMsgList = new ArrayList<>();
			for (ObjectError error : errors) {
				if(error instanceof FieldError) {
					FieldError fieldError = (FieldError) error;

					Map<String, String> fieldErrorMap = new HashMap<>(1);
					fieldErrorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
					errorMsgList.add(fieldErrorMap);

					logger.warn("参数校验失败, field={}, errorMsg={}, fieldValue={}",
							fieldError.getField(), fieldError.getDefaultMessage(), fieldError.getRejectedValue());
				}
			}

			// 将所有校验失败的字段及信息返回
			result.setData(errorMsgList);
			// 将第一个校验失败的字段的错误信息放到result的message中
			if (!errorMsgList.isEmpty()) {
				String firstErrorFieldMsg = "";
				Map<String, String> firstErrorField = errorMsgList.get(0);
				for (Map.Entry<String, String> field : firstErrorField.entrySet()) {
					firstErrorFieldMsg = field.getValue();
					break;
				}
				result.getFail( 0, firstErrorFieldMsg);
			}
		}
		return result;
	}
}
