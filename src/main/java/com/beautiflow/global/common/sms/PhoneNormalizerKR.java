package com.beautiflow.global.common.sms;

public final class PhoneNormalizerKR {
	private PhoneNormalizerKR() {}

	public static String toE164(String raw) {
		if (raw == null) return null;
		String trimmed = raw.trim();
		String digits = trimmed.replaceAll("\\D+", ""); // 숫자만
		if (digits.isEmpty()) return null;

		// '+'로 시작한 경우에도 하이픈 등 제거 후 +digits로 통일
		if (trimmed.startsWith("+")) {
			return "+" + digits; // 예: "+82-10-1234-5678" -> "+821012345678"
		}

		// "82..." 국제형(한국) 입력을 +82로
		if (digits.startsWith("82")) {
			String rest = digits.substring(2);
			if (rest.startsWith("0")) rest = rest.substring(1); // "82010..." 보정
			return "+82" + rest;
		}

		// 국내형 "0..." 은 +82로 치환
		if (digits.startsWith("0")) {
			return "+82" + digits.substring(1);
		}

		// 그 외는 그냥 국제형으로 가정
		return "+" + digits;
	}
}