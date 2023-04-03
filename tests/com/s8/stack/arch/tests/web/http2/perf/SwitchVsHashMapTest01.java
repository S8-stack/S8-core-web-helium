package com.s8.stack.arch.tests.web.http2.perf;

import java.util.HashMap;
import java.util.Map;


public class SwitchVsHashMapTest01 {



	public static void main(String[] args) {
		String[] sample = buildSample(10000);
		for(int i=0; i<100; i++) {
			testMap(sample);
			testSwitch(sample);	
		}
	}


	private final static int N_LOOKUPS = 1000000;

	private static void testMap(String[] sample) {
		Map<String, Integer> map = new HashMap<>(KEYWORDS.length);
		int value = 0;
		for(String keyword : KEYWORDS) {
			map.put(keyword, value);
			value++;
		}

		// use it 
		int index=0, l=sample.length;
		value = 0;
		long time = System.nanoTime();
		for(int i=0; i<N_LOOKUPS; i++) {
			value+=map.get(sample[index]);
			index = (index+1)%l;
		}
		time = System.nanoTime() - time;
		System.out.println("map time "+time/N_LOOKUPS+" val:"+value);
	}

	private static void testSwitch(String[] sample) {

		// use it 
		int index=0, l=sample.length;
		int value = 0;

		long time = System.nanoTime();
		for(int i=0; i<N_LOOKUPS; i++) {
			value+=getFromSwitch(sample[index]);
			index = (index+1)%l;
		}
		time = System.nanoTime() - time;

		System.out.println("switch time "+time/N_LOOKUPS+" val:"+value);
	}







	private static String[] buildSample(int n) {
		String[] sample = new String[n];
		int l = KEYWORDS.length;
		for(int i=0; i<n; i++) {
			sample[i] = KEYWORDS[(int) (Math.random()*l)];
		}
		return sample;
	}





	public final static String[] KEYWORDS = new String[] {
			"fgrt",
			"ksduh",
			"jkh",
			"jkns",
			"sqjq-ej",
			"skhs-8",
			"sgtqy-zl",
			"kqjsopqoj",
			"nqkjsnoiqjsoij-jnq",
			"ap<lqn",
			"qp<oikd",
			"jpâfqp",
			"7808-a",
			"kq-&&é1",
			"ojoiq",
			"qojam<",
			"09-akjhz",
			"apwtyfhd",
			"trt-kqpa",
			"jqijzojke-8978897897",
			"jsjiziizoa",
			
			"01_fgrt",
			"01_ksduh",
			"01_jkh",
			"01_jkns",
			"01_sqjq-ej",
			"01_skhs-8",
			"01_sgtqy-zl",
			"01_kqjsopqoj",
			"01_nqkjsnoiqjsoij-jnq",
			"01_ap<lqn",
			"01_qp<oikd",
			"01_jpâfqp",
			"01_7808-a",
			"01_kq-&&é1",
			"01_ojoiq",
			"01_qojam<",
			"01_09-akjhz",
			"01_apwtyfhd",
			"01_trt-kqpa",
			"01_jqijzojke-8978897897",
			"01_jsjiziizoa"
	};


	private static int getFromSwitch(String key) {
		switch(key) {
		case "fgrt" : return 0;
		case "ksduh" : return 1;
		case "jkh" : return 2;
		case "jkns" : return 3;
		case "sqjq-ej" : return 4;
		case "skhs-8" : return 5;
		case "sgtqy-zl" : return 6;
		case "kqjsopqoj" : return 7;
		case "nqkjsnoiqjsoij-jnq" : return 8;
		case "ap<lqn" : return 9;
		case "qp<oikd" : return 10;
		case "jpâfqp" : return 11;
		case "7808-a" : return 12;
		case "kq-&&é1" : return 13;
		case "ojoiq" : return 14;
		case "qojam<" : return 15;
		case "09-akjhz" : return 16;
		case "apwtyfhd" : return 17;
		case "trt-kqpa" : return 18;
		case "jqijzojke-8978897897" : return 19;
		case "jsjiziizoa": return 20;
		
		case "01_fgrt" : return 0;
		case "01_ksduh" : return 1;
		case "01_jkh" : return 2;
		case "01_jkns" : return 3;
		case "01_sqjq-ej" : return 4;
		case "01_skhs-8" : return 5;
		case "01_sgtqy-zl" : return 6;
		case "01_kqjsopqoj" : return 7;
		case "01_nqkjsnoiqjsoij-jnq" : return 8;
		case "01_ap<lqn" : return 9;
		case "01_qp<oikd" : return 10;
		case "01_jpâfqp" : return 11;
		case "01_7808-a" : return 12;
		case "01_kq-&&é1" : return 13;
		case "01_ojoiq" : return 14;
		case "01_qojam<" : return 15;
		case "01_09-akjhz" : return 16;
		case "01_apwtyfhd" : return 17;
		case "01_trt-kqpa" : return 18;
		case "01_jqijzojke-8978897897" : return 19;
		case "01_jsjiziizoa": return 20;
		}
		return 0;
	}


}
