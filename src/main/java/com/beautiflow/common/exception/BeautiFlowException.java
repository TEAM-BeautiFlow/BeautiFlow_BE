package com.beautiflow.common.exception;

import com.beautiflow.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BeautiFlowException extends RuntimeException {

	private final ErrorCode errorCode;
}
